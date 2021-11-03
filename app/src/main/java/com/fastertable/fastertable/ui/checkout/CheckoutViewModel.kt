package com.fastertable.fastertable.ui.checkout

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
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
    val user: OpsAuth = loginRepository.getOpsUser()!!

    private val _activeDate = MutableLiveData<LocalDate>()
    val activeDate: LiveData<LocalDate>
        get() = _activeDate

    private val _checkout = MutableLiveData<ConfirmEmployee?>()
    val checkout: LiveData<ConfirmEmployee?>
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
        getEmployeeCheckout()
        _showOrders.value = true
    }

    fun activated(){
        if (user.userClock.checkout == true){
            _checkoutComplete.postValue("You have completed your checkout.")
        }
    }

    fun getEmployeeCheckout(){
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
//            ce.cashSales = ce.allTickets.filter{ it.paymentType == "Cash"}
//            ce.creditSales = ce.allTickets.filter{ it.paymentType == "Credit" || it.paymentType == "Manual Credit"}

            ce.orderTotal =  ce.allTickets.sumOf { it.paymentTotal }
            ce.paymentTotal = ce.paidTickets.sumOf { it.paymentTotal }
            ce.cashSalesTotal = getCashSales(ce.allTickets)
            ce.creditSalesTotal = getCreditSales(ce.allTickets)
            ce.creditTips = getCreditGratuity(ce.allTickets)
            ce.creditTips = ce.creditTips.minus(ce.tipDiscount?.div(100)!!).round(2)

            if (ce.tipSettlementPeriod == "Daily"){
                ce.totalOwed = ce.cashSalesTotal.minus(ce.creditTips)
            }

            if (ce.tipSettlementPeriod === "Weekly"){
                ce.totalOwed = ce.cashSalesTotal
            }

            if (ce.totalOwed < 0){
                ce.totalOwed = abs(ce.totalOwed)
                ce.totalNegative = true
            }else{
                ce.totalNegative = false
            }

//            val listApprovalItems = ce.approvals.flatMap{it.approvalItems}
            val listApprovalPayment = mutableListOf<ApprovalTicket>()
            for (approval in ce.approvals){
                val payment = ce.payments?.find{it.id == approval.id.replace("A", "P")}
                val at = ApprovalTicket(
                    approval = approval,
                    ticket = payment?.tickets?.find{it.id == approval.ticketId}!!
                )
                listApprovalPayment.add(at)
            }

            val voidTickets = listApprovalPayment.filter{ it.approval.approvalType == "Void Ticket" && it.approval.approved == true}
            val voidItems = listApprovalPayment.filter{ it.approval.approvalType == "Void Item" && it.approval.approved == true}
            val discountTickets = listApprovalPayment.filter{ it.approval.approvalType == "Discount Ticket" && it.approval.approved == true}
            val discountItems = listApprovalPayment.filter{ it.approval.approvalType == "Discount Item" && it.approval.approved == true}

            val voidTicketTotal = voidTickets.sumOf { it.ticket.getTicketSubTotal() }
            val voidItemTotal = voidItems.sumOf { it.approval.newItemPrice!! }
            val discountItemTotal = discountItems.sumOf { it.approval.newItemPrice!! }
            val discountTicketTotal = discountTickets.sumOf { it.ticket.getTicketSubTotal() }

            ce.voidTotal = voidTicketTotal.plus(voidItemTotal)
            ce.discountTotal = discountItemTotal.plus(discountTicketTotal)

            val listGuests = ce.orders.flatMap { it.guests!! }
            val orderItems = listGuests.flatMap { it.orderItems!! }
            //TODO: Not precise because an item might be discounted in the Payment
            val barItems = orderItems.filter{it.salesCategory == "Bar"}
            val barSales = barItems.sumOf { it.menuItemPrice.price }

            ce.busShare = ce.orderTotal.times(settings.tipShare.busboy.div(100))
            ce.barShare = barSales.times(settings.tipShare.bartender.div(100))
        }else{
            _checkoutNull.value = true
        }

    }

    private fun getCashSales(list: List<Ticket>): Double{
        val payments = mutableListOf<Double>()
        val paymentsList = mutableListOf<TicketPayment>()
        for (ticket in list){
            if (ticket.paymentList != null){
                for (p in ticket.paymentList!!){
                    if (p.paymentType == "Cash"){
                        paymentsList.add(p)
                        payments.add(p.ticketPaymentAmount)
                    }
                }
            }else{
                if (ticket.paymentType == "Cash"){
                    payments.add(ticket.paymentTotal)
                }
            }

        }
//            return paymentsList.sumOf{it.ticketPaymentAmount}
        return payments.sumOf{it}
    }

    private fun getCreditSales(list: List<Ticket>): Double{
        val payments = mutableListOf<Double>()
        val paymentsList = mutableListOf<TicketPayment>()
        for (ticket in list){
            if (ticket.paymentList != null){
                for (p in ticket.paymentList!!){
                    if (p.paymentType == "Credit" || p.paymentType == "Manual Credit"){
                        paymentsList.add(p)
                        payments.add(p.ticketPaymentAmount)
                    }
                }
            }else{
                if (ticket.paymentType == "Credit" || ticket.paymentType == "Manual Credit"){
                    payments.add(ticket.paymentTotal)
                }

            }

        }
        return payments.sumOf{it}
//            return paymentsList.sumOf{it.ticketPaymentAmount}
    }

    private fun getCreditGratuity(list: List<Ticket>): Double{
        val payments = mutableListOf<Double>()
        val paymentsList = mutableListOf<TicketPayment>()
        for (ticket in list){
            if (ticket.paymentList != null){
                for (p in ticket.paymentList!!){
                    if (p.paymentType == "Credit" || p.paymentType == "Manual Credit"){
                        paymentsList.add(p)
                        payments.add(p.gratuity)
                    }
                }
            }else{
                if (ticket.paymentType == "Credit" || ticket.paymentType == "Manual Credit"){
                    payments.add(ticket.gratuity)
                }
            }

        }
        return payments.sumOf{it}
//            return paymentsList.sumOf{it.gratuity}
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
            getEmployeeCheckout()
        }
    }

    fun dateBack(){
        _activeDate.value = _activeDate.value?.minusDays(1)
        getEmployeeCheckout()
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

    fun addTipNew(tp: TicketPayment){
        viewModelScope.launch {
            val payment = _activePayment.value
            if (payment != null){
                val ticket = payment.tickets?.find{ it.uiActive }
                if (ticket != null){
                    val tp1 = ticket.paymentList?.find { it.id == tp.id }
                    if (tp1?.paymentType == "Cash"){
                        savePaymentToCloud()
                    }

                    if (tp1?.paymentType == "Credit" || tp1?.paymentType == "Manual Credit"){
                        processCreditTip(ticket, tp1)
                    }
                }
            }
        }
    }

    private fun processCreditTip(ticket: Ticket, ticketPayment: TicketPayment){
        viewModelScope.launch {
            val ct = ticketPayment.creditCardTransactions!!.first()
            val request = AdjustTipTest(
                MerchantName = settings.merchantCredentials.MerchantName,
                MerchantSiteId = settings.merchantCredentials.MerchantSiteId,
                MerchantKey = settings.merchantCredentials.MerchantKey,
                Token = ct.creditTransaction?.Token!!,
                Amount = ticketPayment.gratuity.toString())
            val transaction: TransactionResponse45 = adjustTip.adjustTip(request) as TransactionResponse45
            ct.tipTransaction = transaction

            if (transaction.ApprovalStatus == "APPROVED"){
                savePaymentToCloud()
            }
        }
    }

    fun cancelTip(){
        _navToTip.value = "Checkout"
    }

    fun captureTickets(){
        viewModelScope.launch {
            if (!checkout.value?.openOrders!!){
                for (item in checkout.value?.payTickets!!) {
                    for (payment in item.ticket!!.paymentList!!){
                        val creditTransaction = payment.creditCardTransactions?.find{ cc -> cc.creditTransaction?.AmountApproved?.toDouble() == payment.ticketPaymentAmount.minus(
                            payment.gratuity)}

                        if (creditTransaction != null) {
                            val capture = Capture(
                                Token = creditTransaction.creditTransaction?.Token!!,
                                Amount = payment.ticketPaymentAmount,
                                InvoiceNumber = "",
                                RegisterNumber = "",
                                MerchantTransactionId = item.payment!!.id + "_" + item.ticket!!.id,
                                CardAcceptorTerminalId = null
                            )
                            val captureRequest = CaptureRequest(
                                Credentials = settings.merchantCredentials,
                                Capture = capture)
                            if (creditTransaction.voidTotal == null && creditTransaction.refundTotal == null && creditTransaction.captureTotal == null){
                                val res: TransactionResponse45 = captureTicket.capture(captureRequest)
                                if (res.ApprovalStatus == "APPROVED"){
                                    println("Captured: ${res.Amount}")
                                    creditTransaction.captureTotal = res.Amount
                                    creditTransaction.captureTransaction = adjustResponse(res)

//                                    val ticket = item.payment!!.tickets!!.find{ t -> t.id == item.ticket!!.id}
//                                    var ctNew = payment.creditCardTransactions?.find{ it -> it.creditTotal == creditTransaction.creditTotal}
//                                    ctNew = creditTransaction
                                    saveCapturedPaymentToCloud(item.payment!!)
                                }
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

    private fun updateUserClock(){
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

    private fun adjustResponse(response45: TransactionResponse45): CaptureResponse{
        return CaptureResponse(
            ApprovalStatus = response45.ApprovalStatus,
            Token = response45.Token,
            AuthorizationCode = response45.AuthorizationCode,
            TransactionDate = response45.TransactionDate,
            Amount = response45.Amount
        )
    }

    private fun savePaymentToCloud(){
        viewModelScope.launch {
            val p: Payment = updatePayment.savePayment(activePayment.value!!)
                _activePayment.postValue(p)
            }
    }

    private fun saveCapturedPaymentToCloud(payment: Payment){
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