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
                                            private val getApproval: GetApproval,
                                            private val saveApproval: SaveApproval,
                                            private val updateApproval: UpdateApproval,
                                            private val receiptPrintingService: ReceiptPrintingService,
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

    private val _ticketPaid = MutableLiveData<Boolean>()
    val ticketPaid: LiveData<Boolean>
        get() = _ticketPaid

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

    private val _whichCreditPayment = MutableLiveData(ShowCreditPayment.DEFAULT)
    val whichCreditPayment: LiveData<ShowCreditPayment>
        get() = _whichCreditPayment

    //endregion

    //region Pay Cash
    fun payCashNow(){
        _activePayment.value?.tickets?.forEach{ t ->
            if (t.uiActive && cashAmount.value != null){
                val amountBeingPaid = cashAmount.value!!.round(2)
                val amountOwed = t.total.minus(t.paymentTotal).minus(amountBeingPaid).round(2)

                if (amountOwed < 0){
                    createCashTicketPayment(t, t.total.minus(t.paymentTotal))
                }else{
                    createCashTicketPayment(t, amountBeingPaid)
                }

                if (amountOwed < 0){
                    _amountOwed.value = amountOwed.absoluteValue
                    t.calculatePaymentTotal(false)
                }

                if (amountOwed == 0.00){
                    t.calculatePaymentTotal(false)
                }

                if (amountOwed > 0){
                    t.calculatePaymentTotal(true)
                }
                _activePayment.value = _activePayment.value
                _cashAmount.value = 0.00

                if (activePayment.value!!.allTicketsPaid()){
                    _activePayment.value!!.close()
                    _activePayment.value = _activePayment.value
                }
                _ticketPaid.value = true
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
            list.add(ticketPayment)
            ticket.paymentList = list
        }else{
            for (payment in ticket.paymentList!!){
                payment.uiActive = false
            }
            ticket.paymentList!!.add(ticketPayment)
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


    fun savePaymentToCloud(){
        viewModelScope.launch {
            val p: Payment
            if (activePayment.value?._rid == ""){
                p = savePayment.savePayment(activePayment.value!!)
                _activePayment.postValue(p)
                printPaymentReceipt()
            }else{
                p = updatePayment.savePayment(activePayment.value!!)
                _activePayment.postValue(p)
                printPaymentReceipt()
            }
            _ticketPaid.value = false
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

    fun setCurrentPayment(id: String){
        viewModelScope.launch {
            val o = getOrder.getOrder(id.replace("P", "O"), settings.locationId)
            _activeOrder.postValue(o)
            getCloudPayment(id, settings.locationId)
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
        _ticketPaid.value = true
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
            list.add(ticketPayment)
            ticket.paymentList = list
        }else{
            for (p in ticket.paymentList!!){
                p.uiActive = false
            }
            ticket.paymentList!!.add(ticketPayment)
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


    fun voidTicket(order: Order){
        viewModelScope.launch {
            _activePayment.value?.voidTicket()
            _activePayment.postValue(_activePayment.value)
            val id: String = order.id.replace("O", "A")
            getApproval.getApproval(id, order.locationId)
            if (approvalRepository.getApproval() == null){
                val approval = approvalRepository.createVoidTicketApproval(order, activePayment.value!!, null)
                savePaymentToCloud()
                saveApprovalToCloud(approval)
            }else{
                val approval = approvalRepository.createVoidTicketApproval(order, activePayment.value!!, approvalRepository.getApproval())
                savePaymentToCloud()
                saveApprovalToCloud(approval)
            }
        }

    }

    fun voidTicketItem(order: Order){
        viewModelScope.launch {
            _activePayment.value?.voidTicketItem(liveTicketItem.value!!)
            _activePayment.value = _activePayment.value
            val id: String = order.id.replace("O", "A")
            getApproval.getApproval(id, order.locationId)
            if (approvalRepository.getApprovalbyId(id) == null){
                val approval = approvalRepository.createVoidTicketItemApproval(order, activePayment.value!!,liveTicketItem.value!!, null)
                saveApprovalToCloud(approval)
                savePaymentToCloud()
            }else{
                val approval = approvalRepository.createVoidTicketItemApproval(order, activePayment.value!!,liveTicketItem.value!!, approvalRepository.getApproval())
                savePaymentToCloud()
                saveApprovalToCloud(approval)
            }
        }

    }

    fun discountTicket(order: Order, discount: Discount){
        viewModelScope.launch {
            var disTotal: Double
            if (discountType.value == "Discount Ticket"){
                disTotal = _activePayment.value?.discountTicket(discount)!!
                _activePayment.value = _activePayment.value
                _paymentScreen.value = ShowPayment.NONE
                val id: String = order.id.replace("O", "A")
                getApproval.getApproval(id, order.locationId)
                if (approvalRepository.getApproval() == null){
                    val approval = approvalRepository.createDiscountTicketApproval(order, activePayment.value!!,discount, disTotal, null)
                    savePaymentToCloud()
                    saveApprovalToCloud(approval)
                }else{
                    val approval = approvalRepository.createDiscountTicketApproval(order, activePayment.value!!,discount, disTotal, approvalRepository.getApproval())
                    savePaymentToCloud()
                    saveApprovalToCloud(approval)
                }
            }

            if (discountType.value == "Discount Item"){
                disTotal = _activePayment.value?.discountTicketItem(liveTicketItem.value!!, discount)!!
                _activePayment.value = _activePayment.value
                _paymentScreen.value = ShowPayment.NONE
                val id: String = order.id.replace("O", "A")
                getApproval.getApproval(id, order.locationId)
                if (approvalRepository.getApproval() == null){
                    val approval = approvalRepository.createDiscountTicketItemApproval(order, activePayment.value!!, liveTicketItem.value!!, discount, disTotal, null)
                    savePaymentToCloud()
                    saveApprovalToCloud(approval)
                }else{
                    val approval = approvalRepository.createDiscountTicketItemApproval(order, activePayment.value!!,liveTicketItem.value!!, discount, disTotal, approvalRepository.getApproval())
                    savePaymentToCloud()
                    saveApprovalToCloud(approval)
                }
            }


        }
    }

    fun modifyPrice(order: Order, price: String){
        viewModelScope.launch {
            _activePayment.value?.modifyItemPrice(liveTicketItem.value!!, price.toDouble())!!
            _activePayment.value = _activePayment.value
            _paymentScreen.value = ShowPayment.NONE
            val id: String = order.id.replace("O", "A")
            getApproval.getApproval(id, order.locationId)

            if (approvalRepository.getApproval() == null){
                val approval = approvalRepository.createModifyItemPriceApproval(order, activePayment.value!!, liveTicketItem.value!!, null)
                savePaymentToCloud()
                saveApprovalToCloud(approval)
            }else{
                val approval = approvalRepository.createModifyItemPriceApproval(order, activePayment.value!!,liveTicketItem.value!!, approvalRepository.getApproval())
                savePaymentToCloud()
                saveApprovalToCloud(approval)
            }
        }
    }

    fun cancelDiscount(){
        _paymentScreen.value = ShowPayment.NONE
    }

    private fun saveApprovalToCloud(approval: Approval){
        viewModelScope.launch {
            if (approval._rid == ""){
                saveApproval.saveApproval(approval)
            }else{
                updateApproval.saveApproval(approval)
            }
        }
    }

    fun setActiveOrder(order: Order){
        _activeOrder.value = order
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
}


