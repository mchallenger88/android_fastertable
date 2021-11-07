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
    val user: OpsAuth = loginRepository.getOpsUser()!!
    val settings: Settings = loginRepository.getSettings()!!
    val terminal: Terminal = loginRepository.getTerminal()!!
    val company: Company = loginRepository.getCompany()!!

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

    //endregion

    //region Pay Cash
    fun payCashNow(){
        val payment = _activePayment.value
        val ticket = payment?.tickets?.find{ it.uiActive}
        if (ticket != null){
            if (cashAmount.value != null){
                val amountBeingPaid = cashAmount.value!!.round(2)
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
        if (!ticket.paymentList.isNullOrEmpty()){
            id = ticket.paymentList!!.size
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
            for (payment in ticket.paymentList!!){
                payment.uiActive = false
            }
            if (!ticketPayment.canceled) {
                ticket.paymentList!!.add(ticketPayment)
            }
        }
    }

    //endregion

    //region Cash Keypad

    fun setCashAmount(number: Int){
        _calculatingCash.value = _calculatingCash.value + number.toString()
        _cashAmount.value = _calculatingCash.value!!.toDouble()
    }

    fun addDecimal(){
        if (!calculatingCash.value!!.contains(".")){
            _calculatingCash.value = _calculatingCash.value + "."
            _cashAmount.value = _calculatingCash.value!!.toDouble()
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
        _creditAmount.value = _calculatingCredit.value!!.toDouble()
    }

    fun addCreditDecimal(){
        if (!_calculatingCredit.value!!.contains(".")){
            _calculatingCredit.value = _calculatingCredit.value + "."
            _creditAmount.value = _calculatingCredit.value!!.toDouble()
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
            var p: Payment
            if (activePayment.value?._rid == ""){
                p = savePayment.savePayment(activePayment.value!!)
                _activePayment.postValue(p)
                printPaymentReceipt()
            }else{
                p = updatePayment.savePayment(activePayment.value!!)
                p = updatePayment.savePayment(p)
                //Had to add this second save because the first save although it returned the correct values was not actually saving to the database
                _activePayment.postValue(p)
                printPaymentReceipt()
            }
        }
    }

    private fun updatePaymentToCloud(payment: Payment){
        viewModelScope.launch {
            updatePayment.savePayment(payment)
        }
    }

    private suspend fun saveOrderToCloud(order: Order){
        val job = viewModelScope.launch {
            updateOrder.saveOrder(order)
        }
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
    //endregion

    //region Misc Functions

    private fun printPaymentReceipt(){
        viewModelScope.launch {
            val ticket = _activePayment.value?.activeTicket()
            val printer = settings.printers.find { it.printerName == terminal.defaultPrinter.printerName }
            val location = company.getLocation(settings.locationId)
            if (ticket?.activePayment() != null){
                if (ticket.activePayment()!!.paymentType == "Cash"){
                    if (printer != null){
                        val ticketDocument = _activePayment.value?.getCashReceipt(printer, location!!)
                        receiptPrintingService.printTicketReceipt(ticketDocument!!, printer, settings)
                    }
                }

                if (ticket.activePayment()!!.paymentType == "Credit"){
                    if (printer != null){
                        val ticketDocument = _activePayment.value?.getCreditReceipt(printer, location!!)
                        receiptPrintingService.printTicketReceipt(ticketDocument!!, printer, settings)
                    }
                }
            }
        }
    }

    fun setCurrentPayment(id: String) {
        viewModelScope.launch {
            val o = getOrder.getOrder(id.replace("P", "O"), settings.locationId)
            _activeOrder.postValue(o)
            getCloudPayment(id, settings.locationId)
        }
    }

        fun clearError(){
            _error.value = false
        }

        private fun setError(title: String, message: String){
            _errorTitle.value = title
            _errorMessage.value = message
            _error.value = true

        }

        fun setManagePayment(){
            if (!activePayment.value!!.anyTicketsPaid()){
                _managePayment.value = !managePayment.value!!
            }else{
                setError("Manage Payment", "Once a ticket has been paid, splitting tickets is no longer possible.")
            }
        }


        fun splitByGuest(order: Order){
            _activePayment.value?.splitByGuest(order, settings.additionalFees)
            _activePayment.value = _activePayment.value
        }

        fun noSplit(order: Order){
            _activePayment.value?.createSingleTicket(order, settings.additionalFees)
            _activePayment.value = _activePayment.value
        }

        fun evenSplit(order: Order){
            _activePayment.value?.splitEvenly(order,
                settings.additionalFees)
            _activePayment.value = _activePayment.value
        }

        fun toggleTicketMore(){
            _showTicketMore.value = !_showTicketMore.value!!
        }

        fun toggleTicketItemMore(item: TicketItem){
            _liveTicketItem.value = item
            _showTicketItemMore.value = !_showTicketItemMore.value!!
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
            val printer = settings.printers.find { it.printerName == terminal.defaultPrinter.printerName }
            val location = company.getLocation(settings.locationId)
            if (printer != null){
                val ticketDocument = _activePayment.value?.getTicketReceipt(_activeOrder.value!!, printer, location!!)
                receiptPrintingService.printTicketReceipt(ticketDocument!!, printer, settings)
            }

        }
    }


    //endregion

    //region Pay Credit Functions
    fun startCredit(order: Order){
        viewModelScope.launch {
            if (_creditAmount.value!! <= _activePayment.value!!.amountOwed()){
                try{
                    val url = "http://" + terminal.ccEquipment.ipAddress + ":8080/pos?Action=StartOrder&Order=" + order.orderNumber.toString() + "&Format=JSON"
                    val response: TerminalResponse = startCredit.startCreditProcess(url)
                    if (response.Status == "Success"){
                        creditStaging(order, settings, terminal)
                    }else{
                        setError("Error Notification", TERMINAL_ERROR)
                    }
                }catch(ex: Exception){
                    setError("Error Notification", TIMEOUT_ERROR)
                }
            }else{
                clearCreditAmount()
                _creditAmount.value = _activePayment.value?.amountOwed()
                setError("Over Payment","Credit payments cannot be more than what is owed on the ticket")
            }
        }
    }

    fun startManualCredit(manualCredit: ManualCredit, order: Order){
        val credentials = MerchantCredentials (
            MerchantKey = settings.merchantCredentials.MerchantKey,
            MerchantName = settings.merchantCredentials.MerchantName,
            MerchantSiteId = settings.merchantCredentials.MerchantSiteId,
            webAPIKey = settings.merchantCredentials.webAPIKey,
            stripePrivateKey = settings.merchantCredentials.stripePrivateKey,
            stripePublickey = settings.merchantCredentials.stripePublickey
        )

        val paymentData = AuthPaymentData (
            source = "Manual",
            cardNumber = manualCredit.cardNumber,
            expirationDate = manualCredit.expirationDate.replace("/", ""),
            cardHolder = manualCredit.cardHolder,
            avsStreetAddress = "",
            avsZipCode = manualCredit.postalCode,
            cardVerificationValue = manualCredit.cvv
        )

        val request = AuthRequestData(
            amount = creditAmount.value!!,
            invoiceNumber = order.orderNumber.toString(),
            registerNumber = terminal.terminalId.toString(),
            merchantTransactionId = "",
            cardAcceptorTerminalId = terminal.terminalId.toString()
        )
        val a = AuthorizationRequest(
            Credentials = credentials,
            PaymentData = paymentData,
            Request = request
        )
        viewModelScope.launch {
            val response45 = manualCreditAuthorization.authorize(a)
            if (response45.ApprovalStatus == "APPROVED"){
                val cayanTransaction = creditCardRepository.createManualCreditTransaction(response45, activePayment.value!!.activeTicket()!!)
                processApproval(activePayment.value!!.activeTicket()!!, cayanTransaction)
            }else{
                val cayanTransaction = creditCardRepository.createManualCreditTransaction(response45, activePayment.value!!.activeTicket()!!)
                processDecline(cayanTransaction)
            }
        }

    }

    private suspend fun creditStaging(order: Order, settings: Settings, terminal: Terminal){
        viewModelScope.launch {
        //TODO: Handle partial payments
            try{
                val transaction: CayanCardTransaction = creditCardRepository.createCayanTransaction(order, activePayment.value?.activeTicket()!!, settings, terminal, creditAmount.value!!)
                val stageResponse: Any = stageTransaction.stageTransaction(transaction)

                if (stageResponse is String){
                    cancelCredit()
                    setError("Error Notification", STAGING_ERROR)
                }

                if (stageResponse is StageResponse){
                    initiateTransaction(stageResponse, terminal)
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
                cancelCredit()
                _error.postValue(true)
            }

            if (cayanTransaction is CayanTransaction){
                if (cayanTransaction.Status.uppercase(Locale.getDefault()) == "APPROVED"){
                    processApproval(activePayment.value!!.activeTicket()!!, cayanTransaction)
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
        val amountBeingPaid = creditAmount.value!!.round(2)
        val amountOwed = _activePayment.value!!.amountOwed().minus(amountBeingPaid).round(2)

        if (amountOwed < 0){
            ticket.calculatePaymentTotal(false)
            setError("Credit Card Process", "A tip was added to the payment. The Tip Amount is: $${ticket.gratuity}")
        }

        if (amountOwed == 0.00){
            ticket.calculatePaymentTotal(false)
            setError("Credit Card Process","The credit was approved.")
        }

        if (amountOwed > 0){
            ticket.calculatePaymentTotal(true)
            setError("Credit Card Process","The credit was approved.")
        }

//        if (ticket.paymentTotal > ticket.total){
//            ticket.gratuity = ticket.paymentTotal.minus(ticket.total).round(2)
//            _amountOwed.value = 0.00
//            ticket.paymentTotal = ticket.total
//            setError("Credit Card Process", "A tip was added to the payment. The Tip Amount is: $${ticket.gratuity}")
//            //TODO: Need to make sure tip is added to the cc receipt
//        }
//
//        if (ticket.paymentTotal == ticket.total){
//            _amountOwed.value = 0.00
//            ticket.paymentTotal = ticket.total
//            setError("Credit Card Process","The credit was approved.")
//        }
//
//        if (ticket.paymentTotal < ticket.total){
//            _amountOwed.value = ticket.total.minus(ticket.paymentTotal).round(2)
//            ticket.paymentTotal = amountOwed.value!!
//            ticket.partialPayment = true
//            setError("Credit Card Process","The credit was approved.")
//        }

        if (activePayment.value!!.allTicketsPaid()){
            _activePayment.value!!.close()
            _activePayment.value = _activePayment.value
        }else{
            _activePayment.value = _activePayment.value
        }
        savePaymentToCloud()
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

    private fun createCreditTicketPayment(ticket: Ticket, paymentType: String,
                                          amount: Double, creditCardTransaction: CreditCardTransaction){
        val list = mutableListOf<TicketPayment>()
        var id = 0
        if (!ticket.paymentList.isNullOrEmpty()){
            id = ticket.paymentList!!.size
        }

        val ccList = arrayListOf<CreditCardTransaction>()
        ccList.add(creditCardTransaction)

        var gratuity = 0.00
        var payment = 0.00
        if (amount > creditAmount.value!!){
            gratuity = amount.minus(creditAmount.value!!).round(2)
            payment = creditAmount.value!!.round(2)
        }else{
            payment = creditAmount.value!!.round(2)
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
            for (p in ticket.paymentList!!){
                p.uiActive = false
            }
            if (!ticketPayment.canceled){
                ticket.paymentList!!.add(ticketPayment)
            }

        }

    }

    fun cancelCredit(){
        viewModelScope.launch {
            try {
                val url =
                    "http://" + terminal.ccEquipment.ipAddress + ":8080/pos?Action=Cancel&Format=JSON"
                cancelCredit.cancelCreditProcess(url)
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
                _activePayment.value?.voidTicket()
                val ticket = _activePayment.value?.activeTicket()!!
                _activePayment.value?.activeTicket()?.ticketItems?.forEach {
                    var approval = approvalRepository.createApproval(_activePayment.value!!, "Void Item",
                        ticket, it, 0.00, null)
                    approval = saveApprovalToCloud(approval)!!
                    it.approvalId = approval.id
                    it.approvalType = "Void Item"

                }

                savePaymentToCloud()


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
                _activePayment.value?.voidTicket()
                val ticket = _activePayment.value?.activeTicket()!!
                _activePayment.value?.activeTicket()?.ticketItems?.forEach {

                }
                for(item in _activePayment.value?.activeTicket()?.ticketItems!!){
                    val ticket = _activePayment.value?.activeTicket()!!
                    val disTotal = _activePayment.value?.discountTicketItem(item, discount)!!
                    var approval = approvalRepository.createApproval(_activePayment.value!!, "Discount Item", ticket,
                        item, disTotal.round(2), null)

                    approval = saveApprovalToCloud(approval)!!
                    item.approvalId = approval.id
                    item.approvalType = "Void Item"
                    savePaymentToCloud()
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
        val ticket = _activePayment.value!!.activeTicket()!!
        val id = liveTicketItem.value!!.id

        val error = checkForPriceChanges(false, true)
        when (error){
            0 -> {
                _activePayment.value?.voidTicketItem(liveTicketItem.value!!)
                _activePayment.value = _activePayment.value

                var approval = approvalRepository.createApproval(_activePayment.value!!, "Void Item", ticket,
                    ticket.ticketItems.find{it.id == id}, 0.00, null)

                approval = saveApprovalToCloud(approval)!!
                ticket.ticketItems.find{it.id == id}?.approvalId = approval.id
                ticket.ticketItems.find{it.id == id}?.approvalType = "Void Item"
                savePaymentToCloud()
            }
            1 -> {
                setError("Void Ticket Item", "There is a pending approval for this payment ticket.")
            }
            3 -> {
                setError("Void Ticket Item", "A manager approval for this item is pending and must be approved or rejected before proceeding.")
            }
            else -> {

            }
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
        val ticket = _activePayment.value!!.activeTicket()!!
        val id = liveTicketItem.value!!.id

        val error = checkForPriceChanges(false, true)
        when (error){
            0 -> {
                val item = ticket.ticketItems.find{it.id == id}
                val disTotal = _activePayment.value?.discountTicketItem(item!!, discount)!!
                _paymentScreen.value = ShowPayment.NONE
                var approval = approvalRepository.createApproval(_activePayment.value!!, "Discount Item", ticket,
                    item!!, disTotal.round(2), null)

                approval = saveApprovalToCloud(approval)!!
                ticket.ticketItems.find{it.id == id}?.approvalId = approval.id
                savePaymentToCloud()
            }
            1 -> {
                setError("Void Ticket Item", "There is a pending approval for this payment ticket.")
                _paymentScreen.value = ShowPayment.NONE
            }
            3 -> {
                setError("Void Ticket Item", "A manager approval for this item is pending and must be approved or rejected before proceeding.")
                _paymentScreen.value = ShowPayment.NONE
            }
            else -> {

            }
        }
        _showProgress.value = false
    }

    fun initialModifyPrice(price: String){
        viewModelScope.launch {
            modifyPrice(price)
        }
    }

    private suspend fun modifyPrice(price: String){
        _showProgress.value = true
        val ticket = _activePayment.value!!.activeTicket()!!
        val id = liveTicketItem.value!!.id

        val error = checkForPriceChanges(false, true)
        when (error){
            0 -> {
                _activePayment.value?.modifyItemPrice(liveTicketItem.value!!,  price.toDouble())!!
                _paymentScreen.value = ShowPayment.NONE
                var approval = approvalRepository.createApproval(_activePayment.value!!, "Modify Price", ticket,
                    ticket.ticketItems.find{it.id == id}, price.toDouble().round(2), null)

                approval = saveApprovalToCloud(approval)!!
                ticket.ticketItems.find{it.id == id}?.approvalId = approval.id
                savePaymentToCloud()
            }
            1 -> {
                setError("Void Ticket Item", "There is a pending approval for this payment ticket.")
                _paymentScreen.value = ShowPayment.NONE
            }
            3 -> {
                setError("Void Ticket Item", "A manager approval for this item is pending and must be approved or rejected before proceeding.")
                _paymentScreen.value = ShowPayment.NONE
            }
            else -> {

            }
        }
        _showProgress.value = false
    }


    private suspend fun checkForPriceChanges(forTicket: Boolean, forTicketItem: Boolean): Int {
        var list = mutableListOf<Approval>()
        val job = viewModelScope.launch {
            list = getAllPaymentApprovals() as MutableList<Approval>
        }

        job.join()

        val ticket = _activePayment.value!!.activeTicket()!!
        val id = liveTicketItem.value?.id
        //0 = no error, 1 = an Approval Ticket exists, 2 = an Approval Ticket Items Exists, 3 = an Approval for this Specific Item Exists
        var error = 0
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
                    if (approval.ticketId == ticket.id && approval.ticketItemId == id && approval.approved == null){
                        error = 3
                        break
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
                for (ticket in payment.tickets!!){
                    val foundTicket = ticket.paymentList?.find { it.id == tp.id && !tp.canceled }

                    if (foundTicket != null){
                        if (tp.paymentType == "Cash"){
                            reopenPaymentAndOrder(order, payment, ticket, tp)
                        }

                        if (tp.paymentType == "Credit" || tp.paymentType == "Manual Credit"){
                            val response = voidCreditTransaction.void(creditRefundRequest(tp, payment))
                            if (response.ApprovalStatus == "APPROVED"){
                                val cc = tp.creditCardTransactions?.get(0)!!
                                cc.creditTotal = 0.00
                                cc.creditTransaction = null
                                cc.voidTransaction = response
                                reopenPaymentAndOrder(order, payment, ticket, tp)
                            }
                        }
                    }
                    break
                }

            }
        }
    }

    private suspend fun reopenPaymentAndOrder(order: Order, payment: Payment, ticket: Ticket, ticketPayment: TicketPayment){
        ticketPayment.canceled = true
        val tempAmount = ticketPayment.ticketPaymentAmount.toString()

        val total = ticket.paymentTotal.minus(ticketPayment.ticketPaymentAmount.plus(ticketPayment.gratuity)).round(2)

        ticket.paymentTotal = total
        payment.reopen()
        _activePayment.postValue(payment)
        order.reopen()
        _activeOrder.postValue(order)
        updatePaymentToCloud(payment)
        saveOrderToCloud(order)
    }

    private fun creditRefundRequest(ticketPayment: TicketPayment, payment: Payment): RefundRequest {
        val cc = ticketPayment.creditCardTransactions?.get(0)?.creditTransaction!!

        return RefundRequest(
            credentials = MerchantCredentials(
                MerchantKey = settings.merchantCredentials.MerchantKey,
                MerchantName = settings.merchantCredentials.MerchantName,
                MerchantSiteId = settings.merchantCredentials.MerchantSiteId,
                webAPIKey = settings.merchantCredentials.webAPIKey,
                stripePrivateKey = "",
                stripePublickey = ""
            ),
            paymentData = PaymentData(
                source = "PreviousTransaction",
                token = cc.Token
            ),
            request = RefundRequestData(
                amount = cc.AmountApproved.toDouble(),
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
            list = getPaymentApprovals.getAllPaymentApprovals(activePayment.value?.id!!, activePayment.value?.locationId!!) as MutableList<Approval>
        }

        job.join()
        return list
    }

    fun cancelDiscount(){
        _paymentScreen.value = ShowPayment.NONE
    }

    private suspend fun saveApprovalToCloud(approval: Approval): Approval?{
        var x: Approval? = null
         val job = viewModelScope.launch {
            if (approval._rid == ""){
                x = saveApproval.saveApproval(approval)
            }else{
                x = updateApproval.saveApproval(approval)
            }
        }

        job.join()
        return x
    }



}


