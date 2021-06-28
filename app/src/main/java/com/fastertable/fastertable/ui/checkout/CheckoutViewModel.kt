package com.fastertable.fastertable.ui.checkout

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fastertable.fastertable.api.CheckoutUseCase
import com.fastertable.fastertable.common.base.BaseViewModel
import com.fastertable.fastertable.data.models.*
import com.fastertable.fastertable.data.repository.*
import com.fastertable.fastertable.utils.GlobalUtils
import com.fastertable.fastertable.utils.round
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject
import kotlin.math.abs

@HiltViewModel
class CheckoutViewModel @Inject constructor (
    private val loginRepository: LoginRepository,
    private val getCheckout: GetCheckout,
    private val updatePayment: UpdatePayment,
    private val checkoutUser: CheckoutUser,
    private val reopenCheckout: ReopenCheckout,
    private val captureTicket: CaptureTicketTransaction,
    private val adjustTip: AdjustTipTransaction,
        ): BaseViewModel() {


    val settings: Settings = loginRepository.getSettings()!!

    private val _activeDate = MutableLiveData<LocalDate>()
    val activeDate: LiveData<LocalDate>
        get() = _activeDate

    private val _checkout = MutableLiveData<ConfirmEmployee>()
    val checkout: LiveData<ConfirmEmployee>
        get() = _checkout

    private val _checkoutNull = MutableLiveData<Boolean>()
    val checkoutNull: LiveData<Boolean>
        get() = _checkoutNull

    private val _showOrders = MutableLiveData<Boolean>()
    val showOrders: LiveData<Boolean>
        get() = _showOrders

    private val _activePayment = MutableLiveData<Payment>()
    val activePayment: LiveData<Payment>
        get() = _activePayment

    private val _navToTip = MutableLiveData<String>()
    val navToTip: LiveData<String>
        get() = _navToTip

    private val _checkoutComplete = MutableLiveData<String>()
    val checkoutComplete: LiveData<String>
        get() = _checkoutComplete


    private val midnight: Long = GlobalUtils().getMidnight()

    init{
        _activeDate.value = LocalDate.now()
        getCheckout()
        _showOrders.value = true
    }

    fun getCheckout(){
        viewModelScope.launch {
            val user = loginRepository.getOpsUser()
            val rollingMidnight = GlobalUtils().unixMidnight(_activeDate.value!!)
            if (midnight == rollingMidnight){
                val request = CheckoutRequest(
                    companyId = settings.companyId,
                    locationId = settings.locationId,
                    userId = user?.employeeId!!,
                    midnight = midnight,
                    clockInTime = user.userClock.clockInTime
                )
                _checkout.postValue(getCheckout.getCheckout(request))
            }else{
                val request = CheckoutRequest(
                    companyId = settings.companyId,
                    locationId = settings.locationId,
                    userId = user?.employeeId!!,
                    midnight = rollingMidnight,
                    clockInTime = null
                )
                _checkout.postValue(getCheckout.getCheckout(request))
            }
        }
    }

    fun separateTickets(ce: ConfirmEmployee){
        if (ce.orders != null){
            _checkoutNull.value = false
            val listPayTicket = arrayListOf<PayTicket>()
            val listTickets = arrayListOf<Ticket>()
            ce.orders.forEach { o ->
                val pt = PayTicket(
                    order = o,
                    payment = null,
                    ticket = null
                )

                val p = ce.payments?.find{ it.id == o.id.replace("O_", "P_")}
                p?.tickets?.forEach {
                    pt.payment = p
                    pt.ticket = it
                    listTickets.add(it)
                }
                listPayTicket.add(pt)
            }

            ce.payTickets = listPayTicket
            ce.openOrders = ce.orders.any{ it.closeTime == null }
            ce.allTickets = listTickets
            ce.paidTickets = ce.allTickets.filter{ it.paymentTotal != 0.00}
            ce.cashSales = ce.allTickets.filter{ it.paymentType == "Cash"}
            ce.creditSales = ce.allTickets.filter{ it.paymentType == "Credit" || it.paymentType == "Manual Credit"}

            ce.orderTotal =  ce.allTickets.sumByDouble { it.paymentTotal }
            ce.paymentTotal = ce.paidTickets.sumByDouble { it.paymentTotal }
            ce.cashSalesTotal = ce.cashSales.sumByDouble { it.paymentTotal }
            ce.creditSalesTotal = ce.creditSales.sumByDouble { it.paymentTotal }
            ce.creditTips = ce.creditSales.sumByDouble { it.gratuity }
            ce.creditTips = ce.creditTips.minus(ce.tipDiscount?.div(100)!!).round(2)

            if (ce.tipSettlementPeriod == "Daily"){
                ce.totalOwed = ce.cashSalesTotal.minus(ce.creditTips)
            }

            if (ce.tipSettlementPeriod === "Weekly"){
                ce.totalOwed = ce.cashSalesTotal
            }

            if (ce.totalOwed < 0){
                ce.totalOwed = abs(ce.totalOwed);
                ce.totalNegative = true;
            }else{
                ce.totalNegative = false;
            }

            val listApprovalItems = ce.approvals.flatMap{it.approvalItems}

            val voidTickets = listApprovalItems.filter{ it.approvalType == "Void Ticket" && it.approved == true}
            val voidItems = listApprovalItems.filter{ it.approvalType == "Void Item" && it.approved == true}
            val discountTickets = listApprovalItems.filter{ it.approvalType == "Discount Ticket" && it.approved == true}
            val discountItems = listApprovalItems.filter{ it.approvalType == "Discount Item" && it.approved == true}

            val voidTicketTotal = voidTickets.sumByDouble { it.amount }
            val voidItemTotal = voidItems.sumByDouble { it.amount }
            val discountItemTotal = discountItems.sumByDouble { it.amount }
            val discountTicketTotal = discountTickets.sumByDouble { it.amount }

            ce.voidTotal = voidTicketTotal.plus(voidItemTotal)
            ce.discountTotal = discountItemTotal.plus(discountTicketTotal)

            val listGuests = ce.orders.flatMap { it.guests!! }
            val orderItems = listGuests.flatMap { it.orderItems!! }
            //TODO: Not precise because an item might be discounted in the Payment
            val barItems = orderItems.filter{it.salesCategory == "Bar"}
            val barSales = barItems.sumByDouble { it.menuItemPrice.price }

            ce.busShare = ce.orderTotal.times(settings.tipShare.busboy.div(100));
            ce.barShare = barSales.times(settings.tipShare.bartender.div(100))
        }else{
            _checkoutNull.value = true
        }

    }

    fun setActiveTicket(ticket: Ticket){
        _activePayment.value?.tickets?.forEach { t ->
            t.uiActive = t.id == ticket.id
        }
        _activePayment.value = _activePayment.value
    }

    fun dateForward(){
        if (_activeDate.value?.plusDays(1)?.atStartOfDay()!! <= LocalDate.now().atStartOfDay()){
            _activeDate.value = _activeDate.value?.plusDays(1)
            getCheckout()
        }
    }

    fun dateBack(){
        _activeDate.value = _activeDate.value?.minusDays(1)
        getCheckout()
    }

    fun showOrders(){
        _showOrders.value = true
    }

    fun setPaymentThenNav(orderId: String){
        val pid = orderId.replace("O_", "P_")
        _activePayment.value = checkout.value?.payments?.find{ it -> it.id == pid}
        if (_activePayment.value?.closed == true){
            _navToTip.value = "Tip"
            return
        }

        if (_activePayment.value == null || _activePayment.value?.closed == false){
            _navToTip.value = orderId
            return
        }

    }

    fun navigationEnd(){
        _navToTip.value = ""
    }

    fun addTip(tip: String){
        viewModelScope.launch {
            //find active ticket
            _activePayment.value?.tickets?.forEach {
                if (it.uiActive){
                    if (it.paymentType == "Cash"){
                        it.addTip(tip.toDouble(), activePayment.value?.taxRate!!)
                        _activePayment.value = _activePayment.value
                        savePaymentToCloud()
                    }

                    if (it.paymentType == "Credit"){
                        val ct = it.creditCardTransactions.find{cc -> cc.creditTransaction.AmountApproved.toDouble() == it.paymentTotal.minus(it.gratuity)}
                        if (ct != null){
                            val request = AdjustTipTest(
                                MerchantName = settings.merchantCredentials.MerchantName,
                                MerchantSiteId = settings.merchantCredentials.MerchantSiteId,
                                MerchantKey = settings.merchantCredentials.MerchantKey,
                                Token = ct.creditTransaction.Token,
                                Amount = tip)
                            val transaction: TransactionResponse45 = adjustTip.adjustTip(request) as TransactionResponse45
                            println(transaction)
                            if (transaction.ApprovalStatus == "APPROVED"){
                                it.addTip(tip.toDouble(), activePayment.value?.taxRate!!)
                                _activePayment.value = _activePayment.value
                                savePaymentToCloud()
                            }
                        }}}}
        }
    }

    fun cancelTip(){
        _navToTip.value = "Checkout"
    }

    fun captureTickets(){
        viewModelScope.launch {
            if (!checkout.value?.openOrders!!){
                checkout.value?.payTickets?.forEach {
                    val ct = it.ticket?.creditCardTransactions?.find{ cc -> cc.creditTransaction.AmountApproved.toDouble() == it.ticket!!.paymentTotal.minus(
                        it.ticket!!.gratuity)}
                    if (ct != null) {
                        val capture = Capture(
                            Token = ct.creditTransaction.Token,
                            Amount = it.ticket?.paymentTotal!!,
                            InvoiceNumber = it.payment?.orderNumber.toString(),
                            RegisterNumber = "",
                            MerchantTransactionId = it.payment!!.id + "_" + it.ticket!!.id,
                            CardAcceptorTerminalId = null
                        )
                        val captureRequest = CaptureRequest(
                            Credentials = settings.merchantCredentials,
                            Capture = capture)
                        if (ct.voidTotal == null && ct.refundTotal == null && ct.captureTotal == null){
                            val res: TransactionResponse45 = captureTicket.capture(captureRequest)
                            if (res.ApprovalStatus == "APPROVED"){
                                println("Captured: ${res.Amount}")
                                ct.captureTotal = res.Amount
                                ct.captureTransaction = adjustResponse(res)

                                val ticket = it.payment!!.tickets.find{ t -> t.id == it.ticket!!.id}
                                var ctNew = ticket?.creditCardTransactions?.find{ it -> it.creditTotal == ct.creditTotal}
                                ctNew = ct
                                saveCapturedPaymentToCloud(it.payment!!)
                            }
                        }
                    }
                }
                updateUserClock()
                _checkoutComplete.postValue("Your checkout is complete.")
            }else{
                _checkoutComplete.postValue("You must close all open orders before checking out.")
            }

        }
    }

    fun updateUserClock(){
        viewModelScope.launch {
            val user = loginRepository.getOpsUser()
            if (user != null) {
                val cc = CheckoutCredentials(
                    employeeId = user.employeeId,
                    companyId = settings.companyId,
                    locationId = settings.locationId,
                    checkout = true,
                    midnight = GlobalUtils().getMidnight()
                )
                checkoutUser.checkout(cc)
            }
        }
    }

    fun adjustResponse(response45: TransactionResponse45): CaptureResponse{
        return CaptureResponse(
            ApprovalStatus = response45.ApprovalStatus,
            Token = response45.Token,
            AuthorizationCode = response45.AuthorizationCode,
            TransactionDate = response45.TransactionDate,
            Amount = response45.Amount
        )
    }

    fun savePaymentToCloud(){
        viewModelScope.launch {
            val p: Payment = updatePayment.savePayment(activePayment.value!!)
                _activePayment.postValue(p)
            }
    }

    fun saveCapturedPaymentToCloud(payment: Payment){
        viewModelScope.launch {
            updatePayment.savePayment(payment)
        }
    }

    fun reopenCheckout(){
        viewModelScope.launch {
            val user = loginRepository.getOpsUser()
            if (user != null){
                val request = ReopenCheckoutRequest(
                    employeeId = user.employeeId,
                    companyId = settings.companyId,
                    locationId = settings.locationId,
                    midnight = GlobalUtils().getMidnight()
                )
                reopenCheckout.open(request)
            }
        }

    }

    fun setCheckoutComplete(value: String){
        _checkoutComplete.value = value
    }
}