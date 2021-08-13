package com.fastertable.fastertable.ui.payment

import android.annotation.SuppressLint
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
import technology.master.kotlinprint.printer.Document
import javax.inject.Inject
import kotlin.math.absoluteValue


enum class ShowPayment{
    NONE,
    CASH,
    CREDIT,
    DISCOUNT,
    MODIFY_PRICE
}

@HiltViewModel
class PaymentViewModel @Inject constructor (private val loginRepository: LoginRepository,
                                            private val approvalRepository: ApprovalRepository,
                                            private val savePayment: SavePayment,
                                            private val updatePayment: UpdatePayment,
                                            private val getPayment: GetPayment,
                                            private val startCredit: StartCredit,
                                            private val cancelCredit: CancelCredit,
                                            private val stageTransaction: StageTransaction,
                                            private val creditCardRepository: CreditCardRepository,
                                            private val initiateCreditTransaction: InitiateCreditTransaction,
                                            private val getApproval: GetApproval,
                                            private val saveApproval: SaveApproval,
                                            private val updateApproval: UpdateApproval,
                                            private val receiptPrintingService: ReceiptPrintingService,
                                            private val paymentRepository: PaymentRepository): BaseViewModel() {

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

    private val _amountOwed = MutableLiveData<Double>()
    val amountOwed: LiveData<Double>
        get() = _amountOwed

    private val _ticketPaid = MutableLiveData<Boolean>()
    val ticketPaid: LiveData<Boolean>
        get() = _ticketPaid

    private val _calculatingCash = MutableLiveData("")
    private val calculatingCash: LiveData<String>
        get() = _calculatingCash

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

    fun payNow(){
        _activePayment.value?.tickets?.forEach{ t ->
            if (t.uiActive && cashAmount.value != null){
                t.paymentType = "Cash"
                val amountOwed = (t.total.minus(cashAmount.value!!)).round(2)
                if (amountOwed < 0){
                    _amountOwed.value = amountOwed.absoluteValue
                    t.paymentTotal = t.total
                }

                if (amountOwed == 0.00){
                    t.paymentTotal = t.total
                }

                if (amountOwed > 0){
                    t.paymentTotal = amountOwed.absoluteValue
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
                _cashAmount.value = t.total
            }
        }
    }

    fun roundedPayment(number: Int){
        _cashAmount.value = number.toDouble()
    }

    fun clearCashAmount(){
        _cashAmount.value = 0.00
    }

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

    fun printPaymentReceipt(){
        viewModelScope.launch {
            val ticket = _activePayment.value?.activeTicket()
            val printer = settings.printers.find { it.printerName == terminal.defaultPrinter.printerName }
            val location = company.getLocation(settings.locationId)
            if (ticket?.paymentType == "Cash"){
                if (printer != null){
                    val ticketDocument = _activePayment.value?.getCashReceipt(printer, location!!)
                    receiptPrintingService.printTicketReceipt(ticketDocument!!, printer, settings)
                }
            }

            if (ticket?.paymentType == "Credit"){
                if (printer != null){
                    val ticketDocument = _activePayment.value?.getCreditReceipt(printer, location!!)
                    receiptPrintingService.printTicketReceipt(ticketDocument!!, printer, settings)
                }
            }

        }
    }

    fun showCashView(){
        when (paymentScreen.value){
            ShowPayment.NONE -> _paymentScreen.value = ShowPayment.CASH
            ShowPayment.CASH -> _paymentScreen.value = ShowPayment.NONE
            ShowPayment.CREDIT -> _paymentScreen.value = ShowPayment.CASH
            ShowPayment.DISCOUNT -> _paymentScreen.value = ShowPayment.DISCOUNT
            ShowPayment.MODIFY_PRICE -> _paymentScreen.value = ShowPayment.MODIFY_PRICE
        }
    }



    fun startCredit(order: Order){
        viewModelScope.launch {
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

        }
    }


    suspend fun creditStaging(order: Order, settings: Settings, terminal: Terminal){
        viewModelScope.launch {
        //TODO: Handle partial payments
            try{
                val transaction: CayanCardTransaction = creditCardRepository.createCayanTransaction(order, activePayment.value?.activeTicket()!!, settings, terminal)
                val stageResponse: Any = stageTransaction.stageTransaction(transaction)

                if (stageResponse is String){
                    cancelCredit()
                    setError("Error Notification", STAGING_ERROR)
                }

                if (stageResponse is StageResponse){
                    _activePayment.value?.activeTicket()?.stageResponse?.add(stageResponse)
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
                if (cayanTransaction.Status.toUpperCase() == "APPROVED"){
                    processApproval(activePayment.value!!.activeTicket()!!, cayanTransaction)
                }else{
                    processDecline(cayanTransaction)
                }
            }
        }
    }

    private fun processApproval(ticket: Ticket, cayanTransaction: CayanTransaction){
        if (cayanTransaction.PaymentType == "GIFT"){
            ticket.paymentType = "Gift"
        }else{
            ticket.paymentType = "Credit"
        }

        ticket.paymentTotal = cayanTransaction.AmountApproved.toDouble()

        val cc: CreditCardTransaction = creditCardRepository.createCreditCardTransaction(ticket, cayanTransaction)
        ticket.creditCardTransactions.add(cc)

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
            setError("Credit Card Process","The credit was approved. Print Receipt?")
        }

        if (ticket.paymentTotal < ticket.total){
            _amountOwed.value = ticket.total.minus(ticket.paymentTotal).round(2)
            ticket.paymentTotal = amountOwed.value!!
            ticket.partialPayment = true
            setError("Credit Card Process","The credit was approved. Print Receipt?")
        }

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
        val status = cayanTransaction.Status.toUpperCase()
        val error = cayanTransaction.ErrorMessage.toLowerCase()

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

    fun clearError(){
        _error.value = false
    }

    fun setError(title: String, message: String){
        _errorTitle.value = title
        _errorMessage.value = message
        _error.value = true

    }

    fun setManagePayment(){
        _managePayment.value = !managePayment.value!!
    }


    fun splitByGuest(order: Order){
        _activePayment.value?.splitByGuest(order)
        _activePayment.value = _activePayment.value
    }

    fun noSplit(order: Order){
        _activePayment.value?.createSingleTicket(order)
        _activePayment.value = _activePayment.value
    }

    fun evenSplit(order: Order){
        _activePayment.value?.splitEvenly(order)
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


