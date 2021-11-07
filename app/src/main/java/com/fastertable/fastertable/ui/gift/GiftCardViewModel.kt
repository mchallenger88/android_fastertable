package com.fastertable.fastertable.ui.gift

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fastertable.fastertable.common.Constants
import com.fastertable.fastertable.common.base.BaseViewModel
import com.fastertable.fastertable.data.models.*
import com.fastertable.fastertable.data.repository.*
import com.fastertable.fastertable.services.ReceiptPrintingService
import com.fastertable.fastertable.utils.GlobalUtils
import com.fastertable.fastertable.utils.round
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import kotlin.math.absoluteValue

enum class ShowGift {
    ADD_CASH,
    BALANCE_INQUIRY,
    PAY_CASH,
    PAY_CREDIT,
    SWIPE_CARD
}

@HiltViewModel
class GiftCardViewModel @Inject constructor (
    private val loginRepository: LoginRepository,
    private val paymentRepository: PaymentRepository,
    private val startCredit: StartCredit,
    private val cancelCredit: CancelCredit,
    private val stageTransaction: StageTransaction,
    private val savePayment: SavePayment,
    private val updatePayment: UpdatePayment,
    private val saveGiftOrder: SaveGiftOrder,
    private val creditCardRepository: CreditCardRepository,
    private val receiptPrintingService: ReceiptPrintingService,
    private val initiateCreditTransaction: InitiateCreditTransaction,) : BaseViewModel() {

    val user: OpsAuth = loginRepository.getOpsUser()!!
    val settings: Settings = loginRepository.getSettings()!!
    val terminal: Terminal = loginRepository.getTerminal()!!
    val company: Company = loginRepository.getCompany()!!

    private val _giftScreen = MutableLiveData(ShowGift.ADD_CASH)
    val giftScreen: LiveData<ShowGift>
        get() = _giftScreen

    private val _cashAmount = MutableLiveData<Double>()
    val cashAmount: LiveData<Double>
        get() = _cashAmount

    private val _calculatingCash = MutableLiveData("")
    private val calculatingCash: LiveData<String>
        get() = _calculatingCash

    private val _amountOwed = MutableLiveData<Double>()
    val amountOwed: LiveData<Double>
        get() = _amountOwed

    private val _ticketPaid = MutableLiveData<Boolean>()
    val ticketPaid: LiveData<Boolean>
        get() = _ticketPaid

    private val _activeOrder = MutableLiveData<Order?>()
    val activeOrder: LiveData<Order?>
        get() = _activeOrder

    private val _activePayment = MutableLiveData<Payment?>()
    val activePayment: LiveData<Payment?>
        get() = _activePayment

    private val _error = MutableLiveData<Boolean>()
    val error: LiveData<Boolean>
        get() = _error

    private val _errorTitle = MutableLiveData<String>()
    val errorTitle: LiveData<String>
        get() = _errorTitle

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String>
        get() = _errorMessage

    private val _balanceResponse = MutableLiveData<String>()
    val balanceResponse: LiveData<String>
        get() = _balanceResponse

    private val _swipeResponse = MutableLiveData<MutableList<String>>()
    val swipeResponse: LiveData<MutableList<String>>
        get() = _swipeResponse

    fun addGiftAmount(amount: Double) {
        if (_activeOrder.value == null) {
            createGiftOrder(amount)
        }else{
            _activeOrder.value!!.changeGiftItemAmount(amount)
            _activePayment.value!!.changeGiftItemAmount(amount)
            _activePayment.value = _activePayment.value
        }
    }



    fun setCashAmount(number: Int) {
        _calculatingCash.value = _calculatingCash.value + number.toString()
        _cashAmount.value = _calculatingCash.value!!.toDouble()
    }

    fun addDecimal() {
        if (!calculatingCash.value!!.contains(".")) {
            _calculatingCash.value = _calculatingCash.value + "."
            _cashAmount.value = _calculatingCash.value!!.toDouble()
        }
    }

    fun exactPayment() {
        _activePayment.value?.tickets?.forEach { t ->
            if (t.uiActive) {
                _cashAmount.value = t.total
            }
        }
    }

    fun roundedPayment(number: Int) {
        _cashAmount.value = number.toDouble()
    }

    fun clearCashAmount() {
        _cashAmount.value = 0.00
    }

    fun clearPayment() {
        _activePayment.value = null
        _activePayment.value = _activePayment.value
    }

    fun payNow() {
        _activePayment.value?.tickets?.forEach { t ->
            if (t.uiActive && cashAmount.value != null){
                val paidAmount = cashAmount.value!!.round(2)
                createCashTicketPayment(t, paidAmount)

                val owed = t.total.minus(t.paymentTotal)
                val amountOwed = (owed.minus(paidAmount)).round(2)
                if (amountOwed < 0){
                    _amountOwed.value = amountOwed.absoluteValue

                    t.paymentTotal = paidAmount
                    t.partialPayment = false
                }

                if (amountOwed == 0.00){
                    t.paymentTotal = paidAmount
                    t.partialPayment = false
                }

                if (amountOwed > 0){
                    t.paymentTotal = paidAmount
                    t.partialPayment = true
                }
                _activePayment.value = _activePayment.value

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

    fun showGiftScreen(screen: Int){
        when (screen){
            1 -> _giftScreen.value = ShowGift.ADD_CASH
            2 -> _giftScreen.value = ShowGift.BALANCE_INQUIRY
            3 -> _giftScreen.value = ShowGift.PAY_CASH
            4 -> _giftScreen.value = ShowGift.PAY_CREDIT
        }
    }

    fun swipeCard(){
        viewModelScope.launch {
            val terminal = loginRepository.getTerminal()!!
            try {
                val url =
                    "http://" + terminal.ccEquipment.ipAddress + ":8080/pos?Action=StartOrder&Order=100&Format=JSON"
                val response: TerminalResponse = startCredit.startCreditProcess(url)
                if (response.Status == "Success") {
                    addValueStaging()
                } else {
                    cancelCredit()
                    setError("Error Notification", Constants.TERMINAL_ERROR)
                }
            } catch (ex: Exception) {
                setError("Error Notification", Constants.TIMEOUT_ERROR)
            }
        }
    }

    fun startCredit() {
        viewModelScope.launch {
            try{
                val url = "http://" + terminal.ccEquipment.ipAddress + ":8080/pos?Action=StartOrder&Order=" + activeOrder.value?.orderNumber.toString() + "&Format=JSON"
                val response: TerminalResponse = startCredit.startCreditProcess(url)
                if (response.Status == "Success"){
                    creditStaging(activeOrder.value!!, settings, terminal)
                }else{
                    setError("Error Notification", Constants.TERMINAL_ERROR)
                }
            }catch(ex: Exception){
                setError("Error Notification", Constants.TIMEOUT_ERROR)
            }

        }
    }

    private suspend fun creditStaging(order: Order, settings: Settings, terminal: Terminal){
        viewModelScope.launch {
            //TODO: Handle partial payments
            try{
                val transaction: CayanCardTransaction = creditCardRepository.createCayanTransaction(order, activePayment.value?.activeTicket()!!, settings, terminal, activePayment.value?.activeTicket()!!.total)
                val stageResponse: Any = stageTransaction.stageTransaction(transaction)

                if (stageResponse is String){
                    cancelCredit()
                    setError("Error Notification", Constants.STAGING_ERROR)
                }

                if (stageResponse is StageResponse){
//                    _activePayment.value?.activeTicket()?.stageResponse?.add(stageResponse)
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

    private fun processApproval(ticket: Ticket, cayanTransaction: CayanTransaction){
        val amount = cayanTransaction.AmountApproved.toDouble()
        val cc: CreditCardTransaction = creditCardRepository.createCreditCardTransaction(ticket, cayanTransaction)
        createCreditTicketPayment(ticket, "Credit", amount, cc)

        ticket.paymentTotal = amount

//        val cc: CreditCardTransaction = creditCardRepository.createCreditCardTransaction(ticket, cayanTransaction)
//        ticket.creditCardTransactions.add(cc)

        if (ticket.paymentTotal > ticket.total){
            ticket.gratuity = ticket.paymentTotal.minus(ticket.total).round(2)
            _amountOwed.value = 0.00
            ticket.paymentTotal = ticket.total
            setError("Credit Card Process", "A tip was added to the payment. The Tip Amount is: $${ticket.gratuity}")
            //TODO: Need to make sure tip is added to the cc receipt
        }

        if (ticket.paymentTotal == ticket.total){
            _amountOwed.value = 0.00
            ticket.paymentTotal = ticket.total
            setError("Credit Card Process","The credit was approved.")
        }

        if (ticket.paymentTotal < ticket.total){
            _amountOwed.value = ticket.total.minus(ticket.paymentTotal).round(2)
            ticket.paymentTotal = amountOwed.value!!
            ticket.partialPayment = true
            setError("Credit Card Process","The credit was approved.")
        }

        if (activePayment.value!!.allTicketsPaid()){
            _activePayment.value!!.close()
            _activePayment.value = _activePayment.value
        }else{
            _activePayment.value = _activePayment.value
        }
        _ticketPaid.value = true
    }

    private fun createCreditTicketPayment(ticket: Ticket, paymentType: String,
                                          amount: Double, creditCardTransaction: CreditCardTransaction){
        val list = mutableListOf<TicketPayment>()
        var id = 0
        if (!ticket.paymentList.isNullOrEmpty()){
            id = ticket.paymentList!!.size
        }
        val ticketPayment = TicketPayment (
            id = id,
            paymentType = paymentType,
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

    @SuppressLint("DefaultLocale")
    fun processDecline(cayanTransaction: CayanTransaction){
        val status = cayanTransaction.Status.uppercase(Locale.getDefault())
        val error = cayanTransaction.ErrorMessage.lowercase(Locale.getDefault())

        if (status == "DECLINED"){
            if (error.contains("insufficient funds")){
                setError("Credit Declined", Constants.DECLINED)
            }
            if (error.contains("referral")){
                setError("Credit Declined", Constants.DECLINED_MORE_INFO)
            }
            if (error == ""){
                setError("Credit Declined", Constants.DECLINED_MORE_INFO)
            }
            if (error.contains("field format error")){
                setError("Credit Declined", Constants.BAD_VALUE)
            }
            if (error.contains("declined insufficient funds available")){
                setError("Credit Declined", Constants.INSUFFICIENT_FUNDS)
            }
        }

        if (status == "REFERRAL"){
            if (error.contains("referral")){
                setError("Credit Declined", Constants.DECLINED_MORE_INFO)
            }
        }

        if (status == "DECLINED_DUPLICATE"){
            setError("Credit Declined", Constants.DUPLICATE)
        }

        if (status == "FAILED"){
            setError("Credit Declined", Constants.COMMUNICATION_ERROR)
        }

        if (status == "USERCANCELLED"){
            setError("Credit Declined", Constants.USER_CANCEL)
        }

        if (status == "POSCANCELLED"){
            setError("Credit Declined", Constants.SERVER_CANCEL)
        }
    }

    fun balanceInquiry(){
        viewModelScope.launch {
            val terminal = loginRepository.getTerminal()!!
            try{
                val url = "http://" + terminal.ccEquipment.ipAddress + ":8080/pos?Action=StartOrder&Order=100&Format=JSON"
                val response: TerminalResponse = startCredit.startCreditProcess(url)
                if (response.Status == "Success"){
                    balanceStaging()
                }else{
                    cancelCredit()
//                    setError("Error Notification", Constants.TERMINAL_ERROR)
                }
            }catch(ex: Exception){
//                setError("Error Notification", Constants.TIMEOUT_ERROR)
            }
        }
    }

    private suspend fun balanceStaging(){
        viewModelScope.launch {
            val transaction = createCayanTransaction("BALANCEINQUIRY", 1.00)
                val stageResponse: Any = stageTransaction.stageTransaction(transaction)
                if (stageResponse is StageResponse){
                    val transportURL: String = "http://" + terminal.ccEquipment.ipAddress + ":8080/v2/pos?TransportKey=" + stageResponse.transportKey + "&Format=JSON"
                    val response: CayanTransaction = initiateCreditTransaction.initiateTransaction(transportURL)
                    if (response.Status.uppercase(Locale.ROOT) == "APPROVED"){
                        _balanceResponse.value = response.AdditionalParameters?.Loyalty?.Balances?.AmountBalance.toString()
                    }
                }

            }
    }

    private suspend fun addValueStaging(){
        viewModelScope.launch {
            val transaction = createCayanTransaction("ADDVALUE", _activePayment.value?.tickets?.get(0)?.paymentTotal!!)
            val stageResponse: Any = stageTransaction.stageTransaction(transaction)
            if (stageResponse is StageResponse){
//                _activePayment.value?.tickets?.get(0)?.stageResponse?.add(stageResponse)
                val transportURL: String = "http://" + terminal.ccEquipment.ipAddress + ":8080/v2/pos?TransportKey=" + stageResponse.transportKey + "&Format=JSON"
                val response: CayanTransaction = initiateCreditTransaction.initiateTransaction(transportURL)
                if (response.Status.uppercase(Locale.ROOT) == "APPROVED"){
                    clearActiveOrderPayment()
                    _giftScreen.postValue(ShowGift.ADD_CASH)
                    val list = mutableListOf<String>()
                    list.add(response.AmountApproved)
                    list.add(response.AdditionalParameters?.Loyalty?.Balances?.AmountBalance.toString())
                    _swipeResponse.value = list

                }
            }
        }
    }


    private fun createCayanTransaction(type: String, amount: Double): CayanCardTransaction {
        return CayanCardTransaction(
            merchantName = settings.merchantCredentials.MerchantName,
            merchantSiteId = settings.merchantCredentials.MerchantSiteId,
            merchantKey = settings.merchantCredentials.MerchantKey,
            request = CayanRequest(
                transactionType = type,
                amount = amount,
                clerkId = "None",
                orderNumber = "None",
                dba = "fastertable",
                softwareName = "fastertable",
                softwareVersion = "1.18.0.0",
                transactionId = "",
                forceDuplicate = true,
                taxAmount = 0.00,
                terminalId = terminal.terminalId.toString(),
                ticketItems = null,
                enablePartialAuthorization = false
            ),
            requestWithTip = null
        )
    }

    fun cancelCredit(){
        viewModelScope.launch {
            val terminal = loginRepository.getTerminal()
            try {
                val url =
                    "http://" + terminal?.ccEquipment?.ipAddress + ":8080/pos?Action=Cancel&Format=JSON"
                cancelCredit.cancelCreditProcess(url)
            }catch(ex: Exception){
                setError("Error Notification", "An error occurred. Please try again or contact your system administrator.")
            }
        }

    }

    private fun setError(title: String, message: String){
        _errorTitle.value = title
        _errorMessage.value = message
        _error.value = true
    }

    private fun createGiftOrder(amount: Double) {
        val orderItems = mutableListOf<OrderItem>()
        val guests = mutableListOf<Guest>()
        val terminal = loginRepository.getTerminal()!!
        val orderItem = OrderItem(
            id = 1,
            quantity = 1,
            menuItemId = "giftCard",
            menuItemName = "Gift Card",
            menuItemPrice = ItemPrice(
                size = "Regular",
                price = amount,
                discountPrice = null,
                tax = "Tax Exempt"
            ),
            orderMods = listOf<ModifierItem>(),
            salesCategory = "Gift",
            ingredientList = null,
            ingredients = null,
            prepStation = null,
            printer = terminal.defaultPrinter,
            priceAdjusted = false,
            menuItemDiscount = null,
            takeOutFlag = false,
            dontMake = false,
            rush = false,
            tax = "Tax Exempt",
            note = null,
            employeeId = user.employeeId,
            status = "Paid")

        orderItems.add(orderItem)
        val guest = Guest(
            id = 0,
            startTime = GlobalUtils().getNowEpoch(),
            orderItems = orderItems,
            subTotal = null,
            tax = null,
            gratuity = 0.00,
            total = null)
        guests.add(guest)

        val order = Order (
            id = "O_" + randomUUID(),
            orderType = "Gift Card",
            orderNumber = 99,
            tableNumber = null,
            employeeId = user.employeeId,
            userName = user.userName,
            startTime = GlobalUtils().getNowEpoch(),
            closeTime = GlobalUtils().getNowEpoch(),
            midnight = GlobalUtils().getMidnight(),
            orderStatus = "Paid",
            kitchenStatus = false,
            rush = false,
//            guests = guests,
            orderItems = orderItems,
            splitChecks = null,
            note = "",
            customer = null,
            takeOutCustomer = null,
            outsideDelivery = null,
            orderFees = null,
            orderDiscount = null,
            pendingApproval = false,
            gratuity = 0.00,
            subTotal = 0.00,
            tax = 0.00,
            total = 0.00,
            locationId = settings.locationId,
            taxRate = 0.00,
            accepted = null,
            estReadyTime = null,
            estDeliveryTime = null,
            transfer = null,
            archived = false,
            type = "Order",
            _rid = "",
            _self = "",
            _etag = "",
            _attachments = "",
            _ts = null
        )
        _activeOrder.value = order
        _activePayment.value = paymentRepository.createNewPayment(order, terminal, settings.additionalFees)

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

    private fun printPaymentReceipt(){
        viewModelScope.launch {
            val ticket = _activePayment.value?.activeTicket()
            val printer = settings.printers.find { it.printerName == terminal.defaultPrinter.printerName }
            val location = company.getLocation(settings.locationId)
            if (ticket?.activePayment()!!.paymentType == "Cash"){
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

    fun closeOrder(){
        _activeOrder.value?.close()
        _activeOrder.value = _activeOrder.value
        saveOrderToCloud()
    }

    private fun saveOrderToCloud(){
        viewModelScope.launch {
            if (_activeOrder.value?.orderNumber!! == 99){
                _activeOrder.postValue(saveGiftOrder.saveOrder(_activeOrder.value!!))
                _giftScreen.postValue(ShowGift.SWIPE_CARD)
            }
        }
    }

    private fun clearActiveOrderPayment(){
        _activeOrder.value = null
        _activePayment.value = null

    }

    private fun randomUUID() = UUID.randomUUID().toString()
}