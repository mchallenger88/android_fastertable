package com.fastertable.fastertable.ui.payment

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fastertable.fastertable.common.Constants.BAD_VALUE
import com.fastertable.fastertable.common.Constants.COMMUNICATION_ERROR
import com.fastertable.fastertable.common.Constants.DECLINED
import com.fastertable.fastertable.common.Constants.DECLINED_MORE_INFO
import com.fastertable.fastertable.common.Constants.DUPLICATE
import com.fastertable.fastertable.common.Constants.INSUFFICIENT_FUNDS
import com.fastertable.fastertable.common.Constants.SERVER_CANCEL
import com.fastertable.fastertable.common.Constants.STAGING_ERROR
import com.fastertable.fastertable.common.Constants.TERMINAL_ERROR
import com.fastertable.fastertable.common.Constants.TIMEOUT_ERROR
import com.fastertable.fastertable.common.Constants.USER_CANCEL
import com.fastertable.fastertable.common.base.BaseViewModel
import com.fastertable.fastertable.data.models.*
import com.fastertable.fastertable.data.repository.*
import com.fastertable.fastertable.services.ReceiptPrintingService
import com.fastertable.fastertable.utils.round
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import kotlin.math.absoluteValue


enum class ShowPayment{
    NONE,
    CASH,
    CREDIT,
    DISCOUNT,
    MODIFY_PRICE
}

enum class ShowCreditPayment{
    DEFAULT,
    MANUAL,
    PARTIAL
}

@HiltViewModel
class PaymentViewModel @Inject constructor (private val loginRepository: LoginRepository,
                                            private val approvalRepository: ApprovalRepository,
                                            private val getOrder: GetOrder,
                                            private val savePayment: SavePayment,
                                            private val updatePayment: UpdatePayment,
                                            private val getPayment: GetPayment,
                                            private val startCredit: StartCredit,
                                            private val cancelCredit: CancelCredit,
                                            private val manualCreditAuthorization: ManualCreditAuthorization,
                                            private val stageTransaction: StageTransaction,
                                            private val creditCardRepository: CreditCardRepository,
                                            private val initiateCreditTransaction: InitiateCreditTransaction,
                                            private val getPaymentApprovals: GetApprovalsById,
                                            private val saveApproval: SaveApproval,
                                            private val updateApproval: UpdateApproval,
                                            private val receiptPrintingService: ReceiptPrintingService,
                                            private val updateOrder: UpdateOrder,
                                            private val voidCreditTransaction: VoidCreditTransaction,
                                            private val paymentRepository: PaymentRepository): BaseViewModel() {

    //region Model Variables
    val user: OpsAuth? = loginRepository.getOpsUser()
    val settings: Settings? = loginRepository.getSettings()
    val terminal: Terminal? = loginRepository.getTerminal()
    val company: Company? = loginRepository.getCompany()

    private val _activeOrder = MutableLiveData<Order>()
    val activeOrder: LiveData<Order>
        get() = _activeOrder

    private val _activePayment = MutableLiveData<Payment?>()
    val activePayment: LiveData<Payment?>
        get() = _activePayment

    private val _cashAmount = MutableLiveData<Double>()
    val cashAmount: LiveData<Double>
        get() = _cashAmount

    private val _creditAmount = MutableLiveData<Double>()
    val creditAmount: LiveData<Double>
        get() = _creditAmount

    private val _amountOwed = MutableLiveData<Double>()
    val amountOwed: LiveData<Double>
        get() = _amountOwed

    private val _calculatingCash = MutableLiveData("")
    private val calculatingCash: LiveData<String>
        get() = _calculatingCash

    private val _calculatingCredit = MutableLiveData("")
    private val calculatingCredit: LiveData<String>
        get() = _calculatingCredit

    private val _paymentScreen = MutableLiveData(ShowPayment.NONE)
    val paymentScreen: LiveData<ShowPayment>
        get() = _paymentScreen

    private val _managePayment = MutableLiveData(false)
    val managePayment: LiveData<Boolean>
        get() = _managePayment

    private val _error = MutableLiveData<Boolean>()
    val error: LiveData<Boolean>
        get() = _error

    private val _splitEvenlyDialog = MutableLiveData(false)
    val splitEvenlyDialog: LiveData<Boolean>
        get() = _splitEvenlyDialog

    private val _splitAdhocDialog = MutableLiveData(false)
    val splitAdhocDialog: LiveData<Boolean>
        get() = _splitAdhocDialog

    private val _errorTitle = MutableLiveData<String>()
    val errorTitle: LiveData<String>
        get() = _errorTitle

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String>
        get() = _errorMessage

    private val _showTicketMore = MutableLiveData(false)
    val showTicketMore: LiveData<Boolean>
        get() = _showTicketMore

    private val _showTicketItemMore = MutableLiveData(false)
    val showTicketItemMore: LiveData<Boolean>
        get() = _showTicketItemMore

    private val _liveTicketItem = MutableLiveData<TicketItem>()
    val liveTicketItem: LiveData<TicketItem>
        get() = _liveTicketItem

    private val _discountType = MutableLiveData<String>()
    val discountType: LiveData<String>
        get() = _discountType

    private val _modifyPriceType = MutableLiveData<String>()
    val modifyPriceType: LiveData<String>
        get() = _modifyPriceType

    private val _navigateToPayment = MutableLiveData<Boolean>()
    val navigateToPayment: LiveData<Boolean>
        get() = _navigateToPayment

    private val _navigateToHome = MutableLiveData(false)
    val navigateToHome: LiveData<Boolean>
        get() = _navigateToHome

    private val _navigateToHomeFromVoid = MutableLiveData(false)
    val navigateToHomeFromVoid: LiveData<Boolean>
        get() = _navigateToHomeFromVoid

    private val _navigateToDashboardFromVoid = MutableLiveData(false)
    val navigateToDashboardFromVoid: LiveData<Boolean>
        get() = _navigateToDashboardFromVoid

    private val _whichCreditPayment = MutableLiveData(ShowCreditPayment.DEFAULT)
    val whichCreditPayment: LiveData<ShowCreditPayment>
        get() = _whichCreditPayment

    private val _showProgress = MutableLiveData(false)
    val showProgress: LiveData<Boolean>
        get() = _showProgress

    private val _voidPayment = MutableLiveData(false)
    val voidPayment: LiveData<Boolean>
        get() = _voidPayment

    private val _voidStart = MutableLiveData(false)
    val voidStart: LiveData<Boolean>
        get() = _voidStart

    private val _showCreditSpinner = MutableLiveData(false)
    val showCreditSpinner: LiveData<Boolean>
        get() = _showCreditSpinner

    private val _showVoidSpinner = MutableLiveData(false)
    val showVoidSpinner: LiveData<Boolean>
        get() = _showVoidSpinner

    private val _approvalSaved = MutableLiveData(false)
    val approvalSaved: LiveData<Boolean>
        get() = _approvalSaved

    private val _newModifyPrice = MutableLiveData<String>()
    val newModifyPrice : LiveData<String>
        get() = _newModifyPrice

    private val _homeOrderListUpdate = MutableLiveData(false)
    val homeOrderListUpdate: LiveData<Boolean>
        get() = _homeOrderListUpdate


    //endregion

    //region Pay Cash
    fun payCashNow(){
        val payment = _activePayment.value
        val ticket = payment?.tickets?.find{ it.uiActive}
        if (ticket != null && ticket.paymentTotal < ticket.total){
            cashAmount.value?.let{
                val amountBeingPaid = it.round(2)
                val amountOwed = ticket.total.minus(ticket.paymentTotal).minus(amountBeingPaid).round(2)
                if (amountOwed < 0){
                    createCashTicketPayment(ticket, ticket.total.minus(ticket.paymentTotal))
                }else{
                    createCashTicketPayment(ticket, amountBeingPaid)
                }

                if (amountOwed < 0){
                    _amountOwed.value = amountOwed.absoluteValue
                    ticket.calculatePaymentTotal(false)
                }

                if (amountOwed == 0.00){
                    ticket.calculatePaymentTotal(false)
                }

                if (amountOwed > 0){
                    ticket.calculatePaymentTotal(true)
                }

                if (payment.allTicketsPaid()){
                    payment.close()
                }
                _activePayment.value = payment
                savePaymentToCloud()
                _cashAmount.value = 0.00
            }
        }
    }

    private fun createCashTicketPayment(ticket: Ticket, amount: Double){
        val list = mutableListOf<TicketPayment>()
        var id = 0
        ticket.paymentList?.let {
            id = it.size
        }

        val ticketPayment = TicketPayment (
            id = id,
            paymentType = "Cash",
            ticketPaymentAmount = amount,
            gratuity = 0.00,
            creditCardTransactions = null,
            uiActive = true
        )
        if (ticket.paymentList.isNullOrEmpty()){
            if (!ticketPayment.canceled){
                list.add(ticketPayment)
                ticket.paymentList = list
            }

        }else{
            ticket.paymentList?.forEach{
                it.uiActive = false
            }
            if (!ticketPayment.canceled) {
                ticket.paymentList?.add(ticketPayment)
            }
        }
    }

    //endregion

    //region Cash Keypad

    fun setCashAmount(number: Int){
        _calculatingCash.value = _calculatingCash.value + number.toString()
        _calculatingCash.value?.let {
            _cashAmount.value = it.toDouble()
        }

    }

    fun addDecimal(){
        _calculatingCash.value?.let {
            if (!it.contains(".")){
                _calculatingCash.value = "$it."
                _cashAmount.value = _calculatingCash.value?.toDouble()
            }
        }
    }

    fun exactPayment(){
        _activePayment.value?.tickets?.forEach{ t ->
            if (t.uiActive){
                _cashAmount.value = t.total.minus(t.paymentTotal)
            }
        }
    }

    fun roundedPayment(number: Int){
        _cashAmount.value = number.toDouble()
    }

    fun clearCashAmount(){
        _calculatingCash.value = ""
        _cashAmount.value = 0.00
    }

    //endregion

    //region Credit Keypad

    fun setCreditAmount(number: Int){
        _calculatingCredit.value = _calculatingCredit.value + number.toString()
        _creditAmount.value = _calculatingCredit.value?.toDouble()
    }

    fun addCreditDecimal(){
        _calculatingCredit.value?.let {
            _calculatingCredit.value = "$it."
            _creditAmount.value = _calculatingCredit.value?.toDouble()
        }
    }

    fun clearCreditAmount(){
        _calculatingCredit.value = ""
        _creditAmount.value = 0.00
    }

    //endregion

    //region Payment Model Functions

    fun clearPayment(){
        paymentRepository.clearPayment()
        _activePayment.value = null
        _activePayment.value = _activePayment.value
    }


    private fun savePaymentToCloud(){
        viewModelScope.launch {
            _activePayment.value?.let {
                var p: Payment
                if (it._rid == ""){
                    p = savePayment.savePayment(it)
                    _activePayment.postValue(p)
                    printPaymentReceipt()
                }else{
                    p = updatePayment.savePayment(it)
                    p = updatePayment.savePayment(p)
                    //Had to add this second save because the first save although it returned the correct values was not actually saving to the database
                    _activePayment.postValue(p)
                    printPaymentReceipt()
                }
            }

        }
    }

    private fun updatePaymentToCloud(payment: Payment){
        viewModelScope.launch {
            updatePayment.savePayment(payment)
        }
    }

    private suspend fun saveOrderToCloud(order: Order){
        viewModelScope.launch {
            updateOrder.saveOrder(order)
            setHomeListUpdate(true)
        }
    }

    fun setHomeListUpdate(b: Boolean){
        _homeOrderListUpdate.value = b
    }

    fun setLivePayment(payment: Payment){
        _activePayment.value = payment
    }

    suspend fun getCloudPayment(id: String, lid: String){
        viewModelScope.launch {
            getPayment.getPayment(id, lid)
            val payment = paymentRepository.getPayment()
            if (payment != null){
                _activePayment.postValue(payment)
            }
        }
    }

    fun setActiveTicket(ticket: Ticket){
        _activePayment.value?.tickets?.forEach { t ->
            t.uiActive = t.id == ticket.id
            if (paymentScreen.value == ShowPayment.CREDIT){
                _creditAmount.value = t.total.minus(t.paymentTotal)
            }
        }
        _activePayment.value = _activePayment.value
    }

    fun setVoidPayment(b: Boolean){
        _voidPayment.value = b
    }

    fun setVoidStart(b: Boolean){
        _voidStart.value = b
    }

    //endregion

    //region Fragment Functions
        fun showCashView(){
        _whichCreditPayment.value = ShowCreditPayment.DEFAULT
            when (paymentScreen.value){
                ShowPayment.NONE -> _paymentScreen.value = ShowPayment.CASH
                ShowPayment.CASH -> _paymentScreen.value = ShowPayment.NONE
                ShowPayment.CREDIT -> _paymentScreen.value = ShowPayment.CASH
                ShowPayment.DISCOUNT -> _paymentScreen.value = ShowPayment.DISCOUNT
                ShowPayment.MODIFY_PRICE -> _paymentScreen.value = ShowPayment.MODIFY_PRICE
            }
        }

        fun showCreditView(){
            _whichCreditPayment.value = ShowCreditPayment.DEFAULT
            _creditAmount.value = _activePayment.value?.amountOwed()
            _paymentScreen.value = ShowPayment.CREDIT
        }

        fun showPartialCredit(){
            _whichCreditPayment.value = ShowCreditPayment.PARTIAL
        }

        fun showManualCredit(){
            _whichCreditPayment.value = ShowCreditPayment.MANUAL
        }

        fun showNone(){
            _paymentScreen.value = ShowPayment.NONE
        }
    //endregion

    //region Misc Functions

    private fun printPaymentReceipt(){
        viewModelScope.launch {
            val ticket = _activePayment.value?.activeTicket()
            val activePayment = ticket?.activePayment()
            settings?.let { s ->
                company?.let { c ->
                    terminal?.let { t ->
                        val printer = s.printers.find { it.printerName == t.defaultPrinter.printerName }
                        val location = c.getLocation(settings.locationId)
                        location?.let { it ->
                            activePayment?.let{ ticketPayment ->
                                if (ticketPayment.paymentType == "Cash" || ticketPayment.paymentType == "Gift"){
                                    if (printer != null){
                                        val ticketDocument = _activePayment.value?.getCashReceipt(printer, it)
                                        ticketDocument?.let { doc ->
                                            receiptPrintingService.printTicketReceipt(doc, printer, s)
                                        }

                                    }
                                }

                                if (ticketPayment.paymentType == "Credit" || ticketPayment.paymentType == "Manual Credit"){
                                    if (printer != null){
                                        val ticketDocument = _activePayment.value?.getCreditReceipt(printer, it)
                                        ticketDocument?.let { doc ->
                                            receiptPrintingService.printTicketReceipt(doc, printer, s)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun setCurrentPayment(id: String) {
        viewModelScope.launch {
            val o = getOrder.getOrder(id.replace("P", "O"), settings?.locationId ?: "")
            _activeOrder.postValue(o)
            getCloudPayment(id, settings?.locationId ?: "")
        }
    }

        fun clearError(){
            _error.value = false
        }

        private fun setError(title: String, message: String){
            _showCreditSpinner.value = false
            _errorTitle.value = title
            _errorMessage.value = message
            _error.value = true

        }

        fun setManagePayment(){
            _activePayment.value?.let { payment ->
                if (!payment.anyTicketsPaid()){
                    _managePayment.value?.let {
                        _managePayment.value = !it
                    }
                }else{
                    setError("Manage Payment", "Once a ticket has been paid, splitting tickets is no longer possible.")
                }
            }
        }

        fun splitByGuest(order: Order){
            if (order.guestCount == 1){
                setAdhocSplitDialog(true)
            }else{
                settings?.let { s ->
                    _activePayment.value?.splitByGuest(order, s.additionalFees)
                    _activePayment.value = _activePayment.value
                }

            }

        }

        fun noSplit(order: Order){
            settings?.let { s ->
                _activePayment.value?.createSingleTicket(order, s.additionalFees)
                _activePayment.value = _activePayment.value
            }
        }

        fun setSplitEvenlyDialog(b: Boolean){
            _splitEvenlyDialog.value = b
        }

        fun setAdhocSplitDialog(b: Boolean){
            _splitAdhocDialog.value = b
        }

        fun evenSplit(order: Order){
            if (order.guestCount == 1 ){
                setSplitEvenlyDialog(true)
            }else{
                settings?.let { s ->
                    _activePayment.value?.splitEvenly(order, s.additionalFees)
                    _activePayment.value = _activePayment.value
                }

            }
        }

        fun setEvenSplitTicketCount(ticketCount: Int){
            val order = _activeOrder.value
            order?.let{
                settings?.let { s ->
                    _activePayment.value?.splitEvenlyXTickets(ticketCount, order, s.additionalFees)
                    _activePayment.value = _activePayment.value
                }
            }
        }

        fun toggleTicketMore(){
            _showTicketItemMore.value?.let {
                _showTicketMore.value = !it
            }
        }

        fun toggleTicketItemMore(item: TicketItem){
            _liveTicketItem.value = item
            _showTicketItemMore.value?.let {
                _showTicketItemMore.value = !it
            }
        }

        fun setPaymentScreen(showPayment: ShowPayment, type: String?){
            when (type){
                "Discount Ticket" -> _discountType.value = "Discount Ticket"
                "Discount Item" -> _discountType.value = "Discount Item"
            }
            _paymentScreen.value = showPayment
        }

        fun goToPayment(){
            _navigateToPayment.value = true
        }

    fun setActiveOrder(order: Order){
        _activeOrder.value = order
    }


    fun returnHome(){
        setReturnHome(true)
    }

    fun returnHomeFromVoidStart(){
        setReturnHomeFromVoidStart(true)
    }

    fun setReturnHome(b: Boolean){
        _navigateToHome.value = b
    }

    fun setReturnHomeFromVoidStart(b: Boolean){
        _navigateToHomeFromVoid.value = b
    }

    fun returnDashboardFromVoid(){
        setReturnDashboardFromVoid(true)
    }

    fun setReturnDashboardFromVoid(b: Boolean){
        _navigateToDashboardFromVoid.value = b
    }

    fun printReceipt(){
        viewModelScope.launch {
            settings?.let { s ->
                terminal?.let { t ->
                    company?.let { c ->
                        val printer = s.printers.find { it.printerName == t.defaultPrinter.printerName }
                        val location = c.getLocation(s.locationId)
                        location?.let { loc ->
                            printer?.let {  p ->
                                val payment = _activePayment.value?.activeTicket()?.paymentList?.find{ !it.canceled }
                                if (payment == null){
                                    _activeOrder.value?.let { order ->
                                        _activePayment.value?.let { pay ->
                                            val ticketDocument = pay.getTicketReceipt(order, p, loc)
                                            ticketDocument.let { doc ->
                                                receiptPrintingService.printTicketReceipt(doc, p, settings)
                                            }
                                        }
                                    }
                                }
                                payment?.let { ticketPayment ->
                                    if (ticketPayment.paymentType == "Cash" || ticketPayment.paymentType == "Gift"){
                                        _activeOrder.value?.let { order ->
                                            _activePayment.value?.let { pay ->
                                                val ticketDocument = pay.getTicketReceipt(order, p, loc)
                                                ticketDocument.let { doc ->
                                                    receiptPrintingService.printTicketReceipt(doc, p, settings)
                                                }
                                            }
                                        }
                                    }

                                    if (ticketPayment.paymentType == "Credit" || ticketPayment.paymentType == "Manual Credit"){
                                        _activePayment.value?.let { pay ->
                                            val ticketDocument = pay.getCreditReceipt(p, loc)
                                            ticketDocument.let { doc ->
                                                receiptPrintingService.printTicketReceipt(doc, p, settings)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    //endregion

    //region Pay Credit Functions
    fun startCredit(order: Order){
        viewModelScope.launch {
            settings?.let { s ->
                terminal?.let { t ->
                    _activePayment.value?.let { payment ->
                        val activeTicket = payment.tickets?.find { it.uiActive }
                        activeTicket?.let { ticket ->
                            if (ticket.paymentTotal < ticket.total){
                                _showCreditSpinner.value = true
                                _creditAmount.value?.let { ca ->
                                    if (ca <= payment.amountOwed()) {
                                        try {
                                            val url =
                                                "http://" + t.ccEquipment.ipAddress + ":8080/pos?Action=StartOrder&Order=" + order.orderNumber.toString() + "&Format=JSON"
                                            val response: TerminalResponse =
                                                startCredit.startCreditProcess(url)
                                            if (response.Status == "Success") {
                                                creditStaging(order, s, t)
                                            } else {
                                                setError("Error Notification", TERMINAL_ERROR)
                                            }
                                        } catch (ex: Exception) {
                                            setError("Error Notification", TIMEOUT_ERROR)
                                        }
                                    } else {
                                        clearCreditAmount()
                                        _creditAmount.value = payment.amountOwed()
                                        setError(
                                            "Over Payment",
                                            "Credit payments cannot be more than what is owed on the ticket"
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private suspend fun creditStaging(order: Order, settings: Settings, terminal: Terminal){
        viewModelScope.launch {
            try{
                activePayment.value?.activeTicket()?.let { ticket ->
                    creditAmount.value?.let { ca ->
                        val transaction: CayanCardTransaction = creditCardRepository.createCayanTransaction(order, ticket, settings, terminal, ca)
                        val stageResponse: Any = stageTransaction.stageTransaction(transaction)
                        if (stageResponse is String){
                            cancelCredit()
                            setError("Error Notification", STAGING_ERROR)
                        }

                        if (stageResponse is StageResponse){
                            initiateTransaction(stageResponse, terminal)
                        }
                    }
                }
            }catch(ex: Exception){
                setError("Error Notification", "Did not Suspend")
            }
        }
    }

    @SuppressLint("DefaultLocale")
    suspend fun initiateTransaction(stageResponse: StageResponse, terminal: Terminal){
        viewModelScope.launch {
            val transportURL: String = "http://" + terminal.ccEquipment.ipAddress + ":8080/v2/pos?TransportKey=" + stageResponse.transportKey + "&Format=JSON"
            val cayanTransaction: Any = initiateCreditTransaction.initiateTransaction(transportURL)
            if (cayanTransaction is String){
                _showCreditSpinner.value = false
                cancelCredit()
                _error.postValue(true)
            }

            if (cayanTransaction is CayanTransaction){
                if (cayanTransaction.Status.uppercase(Locale.getDefault()) == "APPROVED"){
                    activePayment.value?.activeTicket()?.let {
                        processApproval(it, cayanTransaction)
                    }

                }else{
                    processDecline(cayanTransaction)
                }
            }
        }
    }

    //endregion

    //region Process Approval and Decline

    private fun processApproval(ticket: Ticket, cayanTransaction: CayanTransaction){
        val amountApproved = cayanTransaction.AmountApproved.toDouble()
        val cc: CreditCardTransaction = creditCardRepository.createCreditCardTransaction(ticket, cayanTransaction)

        if (cayanTransaction.PaymentType == "GIFT"){
            createCreditTicketPayment(ticket, "Gift", amountApproved, cc)
        }else{
            createCreditTicketPayment(ticket, "Credit", amountApproved, cc)
        }

        //Check for Partial Payment
        _activePayment.value?.let { payment ->
            creditAmount.value?.let { amountBeingPaid ->
                val amountOwed = payment.amountOwed().minus(amountBeingPaid).round(2)
                if (amountOwed < 0){
                    _creditAmount.value = 0.00
                    ticket.calculatePaymentTotal(false)
                    setError("Credit Card Process", "A tip was added to the payment. The Tip Amount is: $${ticket.gratuity}")
                }

                if (amountOwed == 0.00){
                    _creditAmount.value = 0.00
                    ticket.calculatePaymentTotal(false)
                    setError("Credit Card Process","The credit was approved.")
                }

                if (amountOwed > 0){
                    _creditAmount.value = amountOwed
                    ticket.calculatePaymentTotal(true)
                    setError("Credit Card Process","The credit was approved.")
                }

                if (payment.allTicketsPaid()){
                    payment.close()
                    _activePayment.value = payment
                }else{
                    _activePayment.value = payment
                }
                savePaymentToCloud()
            }
        }
    }

    @SuppressLint("DefaultLocale")
    fun processDecline(cayanTransaction: CayanTransaction){
        val status = cayanTransaction.Status.uppercase(Locale.getDefault())
        val error = cayanTransaction.ErrorMessage.lowercase(Locale.getDefault())

        if (status == "DECLINED"){
            if (error.contains("insufficient funds")){
                setError("Credit Declined", DECLINED)
            }
            if (error.contains("referral")){
                setError("Credit Declined", DECLINED_MORE_INFO)
            }
            if (error == ""){
                setError("Credit Declined", DECLINED_MORE_INFO)
            }
            if (error.contains("field format error")){
                setError("Credit Declined", BAD_VALUE)
            }
            if (error.contains("declined insufficient funds available")){
                setError("Credit Declined", INSUFFICIENT_FUNDS)
            }
        }

        if (status == "REFERRAL"){
            if (error.contains("referral")){
                setError("Credit Declined", DECLINED_MORE_INFO)
            }
        }

        if (status == "DECLINED_DUPLICATE"){
            setError("Credit Declined", DUPLICATE)
        }

        if (status == "FAILED"){
            setError("Credit Declined", COMMUNICATION_ERROR)
        }

        if (status == "USERCANCELLED"){
            setError("Credit Declined", USER_CANCEL)
        }

        if (status == "POSCANCELLED"){
            setError("Credit Declined", SERVER_CANCEL)
        }
    }

    fun startManualCredit(){
        _activePayment.value?.activeTicket()?.let { ticket ->
            _creditAmount.value?.let { ca ->
                settings?.let { s ->
                    terminal?.let { t ->
                        val credentials = MerchantCredentials (
                            MerchantKey = s.merchantCredentials.MerchantKey,
                            MerchantName = s.merchantCredentials.MerchantName,
                            MerchantSiteId = s.merchantCredentials.MerchantSiteId,
                            webAPIKey = s.merchantCredentials.webAPIKey,
                            stripePrivateKey = s.merchantCredentials.stripePrivateKey,
                            stripePublickey = s.merchantCredentials.stripePublickey
                        )

                        val paymentData = AuthPaymentData (
                            source = "Manual",
                            cardNumber = cardNumber.value ?: "",
                            expirationDate = expirationDate.value?.replace("/", "") ?: "",
                            cardHolder = cardHolderName.value ?: "",
                            avsStreetAddress = "",
                            avsZipCode = zipcode.value ?: "",
                            cardVerificationValue = cvv.value ?: ""
                        )

                        val request = AuthRequestData(
                            amount = ca,
                            invoiceNumber = activeOrder.value?.orderNumber.toString() ?: "",
                            registerNumber = t.terminalId.toString(),
                            merchantTransactionId = "",
                            cardAcceptorTerminalId = t.terminalId.toString()
                        )
                        val a = AuthorizationRequest(
                            Credentials = credentials,
                            PaymentData = paymentData,
                            Request = request
                        )
                        viewModelScope.launch {
                            val response45 = manualCreditAuthorization.authorize(a)
                            if (response45.ApprovalStatus == "APPROVED"){
                                val cayanTransaction = creditCardRepository.createManualCreditTransaction(response45, ticket)
                                processApproval(ticket, cayanTransaction)
                                setCardHolder("")
                                setCardNumber("")
                                setExpirationDate("")
                                setCVV("")
                                setZipcode("")
                            }else{
                                val cayanTransaction = creditCardRepository.createManualCreditTransaction(response45, ticket)
                                processDecline(cayanTransaction)
                                setCardHolder("")
                                setCardNumber("")
                                setExpirationDate("")
                                setCVV("")
                                setZipcode("")
                            }
                        }
                    }
                }
            }
        }
    }

    private fun createCreditTicketPayment(ticket: Ticket, paymentType: String,
                                          amount: Double, creditCardTransaction: CreditCardTransaction){
        val list = mutableListOf<TicketPayment>()
        var id = 0
        ticket.paymentList?.let {
            id = it.size
        }

        val ccList = arrayListOf<CreditCardTransaction>()
        ccList.add(creditCardTransaction)
        creditAmount.value?.let { ca ->
            var gratuity = 0.00
            var payment = 0.00
            if (amount > ca){
                gratuity = amount.minus(ca).round(2)
                payment = ca.round(2)
            }else{
                payment = ca.round(2)
            }

            val ticketPayment = TicketPayment (
                id = id,
                paymentType = paymentType,
                ticketPaymentAmount = payment,
                gratuity = gratuity,
                creditCardTransactions = ccList,
                uiActive = true
            )

            if (ticket.paymentList.isNullOrEmpty()){
                if (!ticketPayment.canceled){
                    list.add(ticketPayment)
                    ticket.paymentList = list
                }
            }else{
                ticket.paymentList?.forEach {
                    it.uiActive = false
                }
                if (!ticketPayment.canceled){
                    ticket.paymentList?.let {
                        it.add(ticketPayment)
                    }
                }

            }
        }
    }

    fun cancelCredit(){
        viewModelScope.launch {
            try {
                terminal?.let { t ->
                    val url = "http://" + t.ccEquipment.ipAddress + ":8080/pos?Action=Cancel&Format=JSON"
                    cancelCredit.cancelCreditProcess(url)
                }
            }catch(ex: Exception){
                setError("Error Notification", "An error occurred. Please try again or contact your system administrator.")
            }
        }

    }

    //endregion

    //region Manager Approvals

    fun initialVoidTicket(){
        viewModelScope.launch {
            voidTicket()
        }
    }

    private suspend fun voidTicket(){
        _showProgress.value = true
        val error = checkForPriceChanges(true, false)
        when (error){
            0 -> {
                _activePayment.value?.let { payment ->
                    payment.activeTicket()?.let { ticket ->
                        val approval = approvalRepository.createApproval(payment, "Void Ticket",
                            ticket, null, 0.00, null, user?.userName ?: "")
                        val savedApproval = saveApprovalToCloud(approval)
                        savedApproval?.let {
                            payment.voidTicket(savedApproval.id)
                            savePaymentToCloud()
                        }
                    }
                }
            }
            1 -> {
                setError("Void Ticket", "There is a pending approval for this payment ticket.")
            }
            2 -> {
                setError("Void Ticket", "A manager approval is pending and must be approved or rejected before proceeding.")
            }
            else -> {

            }
        }
        _showProgress.value = false
    }

    fun initialDiscountTicket(discount: Discount){
        viewModelScope.launch {
            discountTicket(discount)
        }
    }

    private suspend fun discountTicket(discount: Discount){
        _showProgress.value = true
        val error = checkForPriceChanges(true, false)
        when (error){
            0 -> {
                _activePayment.value?.let { payment ->
                    payment.activeTicket()?.let { ticket ->
                        var disTotal = 0.00
                        if (discount.discountType == "Flat Amount"){
                            disTotal = discount.discountAmount.round(2)
                        }
                        if (discount.discountType == "Percentage"){
                            disTotal = ticket.subTotal.times(discount.discountAmount.div(100)).round(2)
                        }
                        val approval = approvalRepository.createApproval(payment, "Discount Ticket", ticket,
                            null, disTotal, discount, user?.userName ?: "")
                        val savedApproval = saveApprovalToCloud(approval)
                        savedApproval?.let {
                            payment.discountTicket(discount, savedApproval.id)
                            savePaymentToCloud()
                        }
                    }
                }
            }
            1 -> {
                setError("Discount Ticket", "There is a pending approval for this payment ticket.")
            }
            2 -> {
                setError("Discount Ticket", "A manager approval is pending and must be approved or rejected before proceeding.")
            }
            else -> {

            }
        }
        _showProgress.value = false
    }

    fun initialVoidItem(){
        viewModelScope.launch {
            voidTicketItem()
        }
    }

    private suspend fun voidTicketItem(){
        _showProgress.value = true

        val error = checkForPriceChanges(false, true)
        when (error){
            0 -> {
                _activePayment.value?.let { payment ->
                    payment.activeTicket()?.let { ticket ->
                        liveTicketItem.value?.let { lti ->
                            val id = lti.id
                            val approval = approvalRepository.createApproval(payment, "Void Item", ticket,
                                ticket.ticketItems.find{it.id == id}, 0.00, null, user?.userName ?: "")

                            val savedApproval = saveApprovalToCloud(approval)

                            savedApproval?.let {
                                payment.voidTicketItem(lti, savedApproval.id)
                                savePaymentToCloud()
                            }
                        }
                    }
                }
            }
            1 -> {
                setError("Void Ticket Item", "There is a pending approval for this payment ticket.")
            }
            3 -> {
                setError("Void Ticket Item", "A manager approval for this item is pending and must be approved or rejected before proceeding.")
            }
            else -> {}
        }
        _showProgress.value = false
    }


    fun initialDiscountItem(discount: Discount){
        viewModelScope.launch {
            discountTicketItem(discount)
        }
    }

    private suspend fun discountTicketItem(discount: Discount){
        _showProgress.value = true
        val error = checkForPriceChanges(false, true)

        when (error){
            0 -> {
                _activePayment.value?.let { payment ->
                    payment.activeTicket()?.let { ticket ->
                        liveTicketItem.value?.let { lti ->
                            val id = lti.id
                            val item = ticket.ticketItems.find{it.id == id}
                            item?.let { foundItem ->
                                val disTotal = payment.discountTicketItem(foundItem, discount)
                                _paymentScreen.value = ShowPayment.NONE
                                val approval = approvalRepository.createApproval(payment, "Discount Item", ticket,
                                    foundItem, disTotal.round(2), null, user?.userName ?: "")

                                val savedApproval = saveApprovalToCloud(approval)
                                savedApproval?.let {
                                    ticket.ticketItems.find{it.id == id}?.approvalId = savedApproval.id
                                    savePaymentToCloud()
                                }
                            }
                        }
                    }
                }
            }
            1 -> {
                setError("Void Ticket Item", "There is a pending approval for this payment ticket.")
                _paymentScreen.value = ShowPayment.NONE
            }
            3 -> {
                setError("Void Ticket Item", "A manager approval for this item is pending and must be approved or rejected before proceeding.")
                _paymentScreen.value = ShowPayment.NONE
            }
            else -> {}
        }
        _showProgress.value = false
    }

    fun initiateModifyPrice(){
        viewModelScope.launch {
            modifyPrice()
        }
    }

    private suspend fun modifyPrice(){
        val price = _newModifyPrice.value
        price?.let {
        _showProgress.value = true
        _activePayment.value?.let {payment ->
            payment.activeTicket()?.let { ticket ->
                liveTicketItem.value?.let { ticketItem ->
                    when (checkForPriceChanges(false, true)) {
                        0 -> {
                            payment.modifyItemPrice(ticketItem, price.toDouble())
                            _paymentScreen.value = ShowPayment.NONE
                            val approval = approvalRepository.createApproval(payment,
                                "Modify Price",
                                ticket,
                                ticket.ticketItems.find { it.id == ticketItem.id },
                                price.toDouble().round(2),
                                null,
                                user?.userName ?: ""
                            )

                            val savedApproval = saveApprovalToCloud(approval)
                            savedApproval?.let {
                                ticket.ticketItems.find { it.id == ticketItem.id }?.approvalId = savedApproval.id
                                savePaymentToCloud()
                            }

                        }
                        1 -> {
                            setError("Void Ticket Item", "There is a pending approval for this payment ticket.")
                            _paymentScreen.value = ShowPayment.NONE
                        }
                        3 -> {
                            setError(
                                "Void Ticket Item", "A manager approval for this item is pending and must be approved or rejected before proceeding.")
                            _paymentScreen.value = ShowPayment.NONE
                        }
                        else -> {

                        }
                    }
                }
            }
        }
        _showProgress.value = false
        }
    }


    private suspend fun checkForPriceChanges(forTicket: Boolean, forTicketItem: Boolean): Int {
        var list = mutableListOf<Approval>()

        val job = viewModelScope.launch {
            list = getAllPaymentApprovals() as MutableList<Approval>
        }

        job.join()
        //0 = no error, 1 = an Approval Ticket exists, 2 = an Approval Ticket Items Exists, 3 = an Approval for this Specific Item Exists
        var error = 0
        _activePayment.value?.activeTicket()?.let { ticket ->
            liveTicketItem.value?.let { ticketItem ->
                if (forTicket){
                    for (approval in list){
                        if (approval.approvalType == "Void Ticket" || approval.approvalType == "Discount Ticket" && approval.approved == null){
                            error = 1
                            break
                        }

                        if (approval.approvalType == "Void Item" || approval.approvalType == "Discount Item" || approval.approvalType == "Modify Price"){
                            if (approval.ticketId == ticket.id && approval.approved == null){
                                error = 2
                                break
                            }
                        }
                    }
                }

                if (forTicketItem){
                    for (approval in list){
                        if (approval.approvalType == "Void Ticket" || approval.approvalType == "Discount Ticket" && approval.approved == null){
                            error = 1
                            break
                        }

                        if (approval.approvalType == "Void Item" || approval.approvalType == "Discount Item" || approval.approvalType == "Modify Price"){
                            if (approval.ticketId == ticket.id && approval.ticketItemId == ticketItem.id && approval.approved == null){
                                error = 3
                                break
                            }
                        }
                    }
                }
            }
        }

        return error
    }

    //endregion

    fun voidPayment(tp: TicketPayment){
        viewModelScope.launch {
            val payment = _activePayment.value
            val order = _activeOrder.value
            if (payment != null && order != null){
                payment.tickets?.let { tickets ->
                    for (ticket in tickets){
                        val foundTicket = ticket.paymentList?.find { it.id == tp.id && !tp.canceled }

                        if (foundTicket != null){
                            if (tp.paymentType == "Cash"){
                                reopenPaymentAndOrder(order, payment, ticket, tp)
                            }

                            if (tp.paymentType == "Credit" || tp.paymentType == "Manual Credit"){
                                val response = voidCreditTransaction.void(creditRefundRequest(tp, payment))
                                if (response.ApprovalStatus == "APPROVED"){
                                    val cc = tp.creditCardTransactions?.get(0)
                                    cc?.let {
                                        cc.creditTotal = 0.00
                                        cc.creditTransaction = null
                                        cc.voidTransaction = response
                                        reopenPaymentAndOrder(order, payment, ticket, tp)
                                    }
                                }
                            }
                        break
                    }
                }}
            }
        }
    }

    private suspend fun reopenPaymentAndOrder(order: Order, payment: Payment, ticket: Ticket, ticketPayment: TicketPayment){
        ticketPayment.canceled = true
        val tempAmount = ticketPayment.ticketPaymentAmount.toString()

        val total = ticket.paymentTotal.minus(ticketPayment.ticketPaymentAmount.plus(ticketPayment.gratuity)).round(2)

        ticket.paymentTotal = total.round(2)
        payment.reopen()
        _activePayment.postValue(payment)
        order.reopen()
        _activeOrder.postValue(order)
        updatePaymentToCloud(payment)
        saveOrderToCloud(order)
    }

    private fun creditRefundRequest(ticketPayment: TicketPayment, payment: Payment): RefundRequest {
        val cct = ticketPayment.creditCardTransactions?.get(0)?.creditTransaction

        return RefundRequest(
            credentials = MerchantCredentials(
                MerchantKey = settings?.merchantCredentials?.MerchantKey ?: "",
                MerchantName = settings?.merchantCredentials?.MerchantName ?: "",
                MerchantSiteId = settings?.merchantCredentials?.MerchantSiteId ?: "",
                webAPIKey = settings?.merchantCredentials?.webAPIKey ?: "",
                stripePrivateKey = "",
                stripePublickey = ""
            ),
            paymentData = PaymentData(
                source = "PreviousTransaction",
                token = cct?.Token ?: ""
            ),
            request = RefundRequestData(
                amount = cct?.AmountApproved?.toDouble() ?: 0.00,
                invoiceNumber = payment.orderNumber.toString(),
                registerNumber = "",
                merchantTransactionId = payment.id,
                cardAcceptorTerminalId = null
            )
        )
    }

    private suspend fun getAllPaymentApprovals(): List<Approval>{
        var list = mutableListOf<Approval>()
        val job = viewModelScope.launch {
            _activePayment.value?.let {
                list = getPaymentApprovals.getAllPaymentApprovals(it.id, it.locationId) as MutableList<Approval>
            }
        }

        job.join()
        return list
    }

    fun cancelDiscount(){
        _paymentScreen.value = ShowPayment.NONE
    }

    private suspend fun saveApprovalToCloud(approval: Approval): Approval? {
        var x: Approval? = null
         val job = viewModelScope.launch {
            if (approval._rid == ""){
                x = saveApproval.saveApproval(approval)
                setApprovalSaved(true)
            }else{
                x = updateApproval.saveApproval(approval)
                setApprovalSaved(true)
            }
        }

        job.join()
        return x
    }

    private fun setApprovalSaved(b: Boolean){
        _approvalSaved.value = b
    }

    fun setVoidSpinner(b: Boolean){
        _showVoidSpinner.value = b
    }

    fun setModifyPrice(text: String){
        _newModifyPrice.value = text
    }


    //region Manual Credit Card
    private val _cardHolderName = MutableLiveData<String>()
    val cardHolderName: LiveData<String>
        get() = _cardHolderName

    private val _cardNumber = MutableLiveData<String>()
    val cardNumber: LiveData<String>
        get() = _cardNumber

    private val _expirationDate = MutableLiveData<String>()
    val expirationDate: LiveData<String>
        get() = _expirationDate

    private val _cvv = MutableLiveData<String>()
    val cvv: LiveData<String>
        get() = _cvv

    private val _zipcode = MutableLiveData<String>()
    val zipcode: LiveData<String>
        get() = _zipcode

    fun setCardHolder(text: String){
        _cardHolderName.value = text
    }

    fun setCardNumber(text: String){
        _cardNumber.value = text
    }

    fun setExpirationDate(text: String){
        _expirationDate.value = text
    }

    fun setCVV(text: String){
        _cvv.value = text
    }

    fun setZipcode(text: String){
        _zipcode.value = text
    }


    //endregion
}


