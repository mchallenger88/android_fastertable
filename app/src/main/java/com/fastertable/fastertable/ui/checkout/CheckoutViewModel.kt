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
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
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


    val settings: Settings? = loginRepository.getSettings()
    val user: OpsAuth? = loginRepository.getOpsUser()

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
        user?.let {
            if (user.userClock.checkout == true){
                _checkoutComplete.postValue("You have completed your checkout.")
            }
        }
    }

    fun getEmployeeCheckout(){
        viewModelScope.launch {
            _activeDate.value?.let { date ->
                val rollingMidnight = GlobalUtils().unixMidnight(date)
                settings?.let { s ->
                    user?.let { u ->
                        if (midnight == rollingMidnight){
                            val request = CheckoutRequest(
                                companyId = s.companyId,
                                locationId = s.locationId,
                                userId = u.employeeId,
                                midnight = midnight,
                                clockInTime = u.userClock.clockInTime
                            )

                            _checkout.postValue(getCheckout.getCheckout(request))
                        }else{
                            val request = CheckoutRequest(
                                companyId = s.companyId,
                                locationId = s.locationId,
                                userId = u.employeeId,
                                midnight = rollingMidnight,
                                clockInTime = null
                            )
                            _checkout.postValue(getCheckout.getCheckout(request))
                        }
                    }
                }
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

            ce.orderTotal =  ce.allTickets.sumOf { it.paymentTotal }
            ce.paymentTotal = ce.paidTickets.sumOf { it.paymentTotal }
            ce.cashSalesTotal = getCashSales(ce.allTickets)
            ce.creditSalesTotal = getCreditSales(ce.allTickets)
            ce.creditTips = getCreditGratuity(ce.allTickets)
            ce.tipDiscount?.let{ tp ->
                ce.creditTips = ce.creditTips.minus(tp.div(100)).round(2)
            }

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
                payment?.tickets?.find{it.id == approval.ticketId}?.let { ticket ->
                    val at = ApprovalTicket(
                        approval = approval,
                        ticket = ticket
                    )
                    listApprovalPayment.add(at)
                }

            }

            val voidTickets = listApprovalPayment.filter{ it.approval.approvalType == "Void Ticket" && it.approval.approved == true}
            val voidItems = listApprovalPayment.filter{ it.approval.approvalType == "Void Item" && it.approval.approved == true}
            val discountTickets = listApprovalPayment.filter{ it.approval.approvalType == "Discount Ticket" && it.approval.approved == true}
            val discountItems = listApprovalPayment.filter{ it.approval.approvalType == "Discount Item" && it.approval.approved == true}

            val voidTicketTotal = voidTickets.sumOf { it.ticket.getTicketSubTotal() }
            val voidItemTotal = returnSum(voidItems)
            val discountItemTotal = returnSum(discountItems)
            val discountTicketTotal = discountTickets.sumOf { it.ticket.getTicketSubTotal() }

            ce.voidTotal = voidTicketTotal.plus(voidItemTotal)
            ce.discountTotal = discountItemTotal.plus(discountTicketTotal)

            val orderItems = returnAllOrderItems(ce.orders)
            //TODO: Not precise because an item might be discounted in the Payment
            val barItems = orderItems.filter{it.salesCategory == "Bar"}
            val barSales = barItems.sumOf { it.menuItemPrice.price }
            settings?.let {
                ce.busShare = ce.orderTotal.times(settings.tipShare.busboy.div(100))
                ce.barShare = barSales.times(settings.tipShare.bartender.div(100))
            }

        }else{
            _checkoutNull.value = true
        }
    }

    private fun returnSum(list: List<ApprovalTicket>): Double{
        var sum = 0.00
        if (list.isNotEmpty()){
            for (item in list){
                item.approval.newItemPrice?.let {
                    sum = sum.plus(it)
                }
            }
        }
        return sum.round(2)
    }

    private fun returnAllOrderItems(list: List<Order>): List<OrderItem>{
       val oiList = mutableListOf<OrderItem>()
        for (order in list){
            order.getAllOrderItems()?.let { oiList.addAll(it) }
        }
        return oiList as List<OrderItem>
    }

    private fun getCashSales(list: List<Ticket>): Double{
        val payments = mutableListOf<Double>()
        val paymentsList = mutableListOf<TicketPayment>()
        for (ticket in list) {
            if (ticket.paymentList != null) {
                ticket.paymentList?.let { it ->
                    for (p in it) {
                        if (p.paymentType == "Cash" && !p.canceled) {
                            paymentsList.add(p)
                            payments.add(p.ticketPaymentAmount)
                        }
                    }
                }

            } else {
                if (ticket.paymentType == "Cash") {
                    payments.add(ticket.paymentTotal)
                }
            }
        }

        return payments.sumOf{it}
    }

    private fun getCreditSales(list: List<Ticket>): Double{
        val payments = mutableListOf<Double>()
        val paymentsList = mutableListOf<TicketPayment>()
        for (ticket in list){
            if (ticket.paymentList != null){
                ticket.paymentList?.let {  it ->
                    for (p in it){
                        if (p.paymentType == "Credit" || p.paymentType == "Manual Credit" && !p.canceled){
                            paymentsList.add(p)
                            payments.add(p.ticketPaymentAmount)
                        }
                    }
                }
            }else{
                if (ticket.paymentType == "Credit" || ticket.paymentType == "Manual Credit"){
                    payments.add(ticket.paymentTotal)
                }

            }

        }
        return payments.sumOf{it}
    }

    private fun getCreditGratuity(list: List<Ticket>): Double{
        val payments = mutableListOf<Double>()
        val paymentsList = mutableListOf<TicketPayment>()
        for (ticket in list){
            if (ticket.paymentList != null){
                ticket.paymentList?.let { it ->
                    for (p in it){
                        if (p.paymentType == "Credit" || p.paymentType == "Manual Credit" || p.paymentType == "Gift"){
                            paymentsList.add(p)
                            payments.add(p.gratuity)
                        }
                    }
                }

            }else{
                if (ticket.paymentType == "Credit" || ticket.paymentType == "Manual Credit" || ticket.paymentType == "Gift"){
                    payments.add(ticket.gratuity)
                }
            }

        }
        return payments.sumOf{it}
    }

    fun setActiveTicket(ticket: Ticket){
        _activePayment.value?.tickets?.forEach { t ->
            t.uiActive = t.id == ticket.id
        }
        _activePayment.value = _activePayment.value
    }

    fun dateForward(){
        _activeDate.value?.let {
            if (it.plusDays(1).atStartOfDay() <= LocalDate.now().atStartOfDay()){
                _activeDate.value = _activeDate.value?.plusDays(1)
                getEmployeeCheckout()
            }
        }
    }

    fun dateBack(){
        _activeDate.value?.let {
            val epoch = GlobalUtils().unixMidnight(it.minusDays(1))
            //If we were going to figure daylight savings time
            val lastChange = GlobalUtils().getPreviousAdjustmentDay(epoch)
            _activeDate.value = _activeDate.value?.minusDays(1)
            getEmployeeCheckout()
        }

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
            ticketPayment.creditCardTransactions?.let {
                val ct = it.first()
                settings?.let {
                    val request = AdjustTipTest(
                        MerchantName = settings.merchantCredentials.MerchantName,
                        MerchantSiteId = settings.merchantCredentials.MerchantSiteId,
                        MerchantKey = settings.merchantCredentials.MerchantKey,
                        Token = ct.creditTransaction?.Token ?: "",
                        Amount = ticketPayment.gratuity.toString())
                    val transaction: TransactionResponse45 = adjustTip.adjustTip(request) as TransactionResponse45
                    ct.tipTransaction = transaction

                    if (transaction.ApprovalStatus == "APPROVED"){
                        savePaymentToCloud()
                    }
                }
            }
        }
    }

    fun cancelTip(){
        _navToTip.value = "Checkout"
    }

    fun captureTickets(){
        viewModelScope.launch {
            val checkout = _checkout.value
            val epoch = GlobalUtils().unixMidnight(_activeDate.value ?: LocalDate.now())
            checkout?.let {
               if (checkout.checkoutDate == epoch && settings != null){
                   if (!checkout.openOrders){
                       checkout.payTickets.forEach { item ->
                           item.ticket?.let { ticket ->
                               ticket.paymentList?.let{ payList ->
                                   for (payment in payList){
                                       val creditTransaction = payment.creditCardTransactions?.find{ cc -> cc.creditTransaction?.AmountApproved?.toDouble() ==
                                           payment.ticketPaymentAmount}
                                       if (creditTransaction != null) {
                                           val capture = Capture(
                                               Token = creditTransaction.creditTransaction?.Token ?: "",
                                               Amount = payment.ticketPaymentAmount.plus(payment.gratuity),
                                               InvoiceNumber = item.order.orderNumber.toString(),
                                               RegisterNumber = "",
                                               MerchantTransactionId = item.payment?.id + "_" + ticket.id,
                                               CardAcceptorTerminalId = null
                                           )
                                           val captureRequest = CaptureRequest(
                                               Credentials = settings.merchantCredentials,
                                               Capture = capture)
                                           if (creditTransaction.voidTotal == null && creditTransaction.refundTotal == null && creditTransaction.captureTotal == null){
                                               val res: TransactionResponse45 = captureTicket.capture(captureRequest)
                                               if (res.ApprovalStatus == "APPROVED"){
                                                   creditTransaction.captureTotal = res.Amount
                                                   creditTransaction.captureTransaction = adjustResponse(res)
                                                   item.payment?.let { payment ->
                                                       saveCapturedPaymentToCloud(payment)
                                                   }
                                               }
                                           }
                                       }
                                   }
                               }
                           }
                       }

                       updateUserClock(checkout.checkoutDate)
                       _checkoutComplete.postValue("Your checkout is complete.")
                   }else{
                       _checkoutComplete.postValue("You must close all open orders before checking out.")
                   }
               }
            }
        }
    }

    private fun updateUserClock(day: Long){
        viewModelScope.launch {
            val user = loginRepository.getOpsUser()
            if (user != null && settings != null) {
                val cc = CheckoutCredentials(
                    employeeId = user.employeeId,
                    companyId = settings.companyId,
                    locationId = settings.locationId,
                    checkout = true,
                    midnight = day
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
            _activePayment.value?.let {
                val p: Payment = updatePayment.savePayment(it)
                _activePayment.postValue(p)
            }
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
            if (user != null && settings != null){
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