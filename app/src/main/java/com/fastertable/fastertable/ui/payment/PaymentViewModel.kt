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
import com.fastertable.fastertable.utils.round
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.absoluteValue


enum class ShowPayment{
    NONE,
    CASH,
    CREDIT,
    DISCOUNT
}

@HiltViewModel
class PaymentViewModel @Inject constructor (private val loginRepository: LoginRepository,
                                            private val orderRepository: OrderRepository,
                                            private val approvalRepository: ApprovalRepository,
                                            private val savePayment: SavePayment,
                                            private val updatePayment: UpdatePayment,
                                            private val getPayment: GetPayment,
                                            private val startCredit: StartCredit,
                                            private val cancelCredit: CancelCredit,
                                            private val stageTransaction: StageTransaction,
                                            private val creditCardRepository: CreditCardRepository,
                                            private val initiateCreditTransaction: InitiateCreditTransaction,
                                            private val saveApproval: SaveApproval,
                                            private val updateApproval: UpdateApproval,
                                            private val paymentRepository: PaymentRepository): BaseViewModel() {

    private lateinit var user: OpsAuth
    public lateinit var settings: Settings

    private val _order = MutableLiveData<Order>()
    val liveOrder: LiveData<Order>
        get() = _order

    private val _payment = MutableLiveData<Payment?>()
    val livePayment: LiveData<Payment?>
        get() = _payment

    private val _cashAmount = MutableLiveData<Double>()
    val cashAmount: LiveData<Double>
        get() = _cashAmount

    private val _amountOwed = MutableLiveData<Double>()
    val amountOwed: LiveData<Double>
        get() = _amountOwed

    private val _ticketPaid = MutableLiveData<Boolean>()
    val ticketPaid: LiveData<Boolean>
        get() = _ticketPaid

    private val _calculatingCash = MutableLiveData<String>()
    private val calculatingCash: LiveData<String>
        get() = _calculatingCash

    private val _paymentScreen = MutableLiveData<ShowPayment>()
    val paymentScreen: LiveData<ShowPayment>
        get() = _paymentScreen

    private val _managePayment = MutableLiveData<Boolean>()
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

    private val _showTicketMore = MutableLiveData<Boolean>()
    val showTicketMore: LiveData<Boolean>
        get() = _showTicketMore

    private val _showTicketItemMore = MutableLiveData<Boolean>()
    val showTicketItemMore: LiveData<Boolean>
        get() = _showTicketItemMore


    private var paid: Boolean = false
    private var send: Boolean = false
    private var approved: Boolean = false
    private var showMessage: Boolean = false
    private var showErrorMessage: Boolean = false
    private var processing: Boolean = false
    private var message: String = ""
    private var showPartial: Boolean = false
    private var tooMuch: Boolean = false

    init{
        _managePayment.value = false
        _showTicketItemMore.value = false
        _showTicketMore.value = false
        _paymentScreen.value = ShowPayment.NONE
        _calculatingCash.value = ""
        settings = loginRepository.getSettings()!!
    }

    fun payNow(){
        _payment.value?.tickets?.forEach{ t ->
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
                _payment.value = _payment.value

                if (livePayment.value!!.allTicketsPaid()){
                    _payment.value!!.close()
                    _payment.value = _payment.value
                }
                _ticketPaid.value = true
            }
        }
    }

    fun setLivePayment(payment: Payment){
        _payment.value = payment
    }

    suspend fun getCloudPayment(id: String, lid: String){
        viewModelScope.launch {
            getPayment.getPayment(id, lid)
            val payment = paymentRepository.getPayment()
            if (payment != null){
                _payment.postValue(payment!!)
            }

        }

    }

    fun setActiveTicket(ticket: Ticket){
        _payment.value?.tickets?.forEach { t ->
            t.uiActive = t.id == ticket.id
        }
        _payment.value = _payment.value
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
        _payment.value?.tickets?.forEach{ t ->
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
        _payment.value = null
        _payment.value = _payment.value
    }


    fun savePaymentToCloud(){
        viewModelScope.launch {
            val p: Payment
            if (livePayment.value?._rid == ""){
                p = savePayment.savePayment(livePayment.value!!)
                _payment.postValue(p)
            }else{
                p = updatePayment.savePayment(livePayment.value!!)
                _payment.postValue(p)
            }
            _ticketPaid.value = false
        }

    }

    fun showCashView(){
        when (paymentScreen.value){
            ShowPayment.NONE -> _paymentScreen.value = ShowPayment.CASH
            ShowPayment.CASH -> _paymentScreen.value = ShowPayment.NONE
            ShowPayment.CREDIT -> _paymentScreen.value = ShowPayment.CASH
            ShowPayment.DISCOUNT -> _paymentScreen.value = ShowPayment.DISCOUNT
        }
    }



    fun startCredit(order: Order){
        viewModelScope.launch {
            val terminal = loginRepository.getTerminal()!!
            try{
                val url = "http://" + terminal.ccEquipment.ipAddress + ":8080/pos?Action=StartOrder&Order=" + order.orderNumber.toString() + "&Format=JSON"
                val response: TerminalResponse = startCredit.startCreditProcess(url)
                if (response.Status == "Success"){
                    val settings = loginRepository.getSettings()!!
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
                val transaction: CayanCardTransaction = creditCardRepository.createCayanTransaction(order, livePayment.value?.activeTicket()!!, settings, terminal)
                val stageResponse: Any = stageTransaction.stageTransaction(transaction)

                if (stageResponse is String){
                    cancelCredit()
                    setError("Error Notification", STAGING_ERROR)
                }

                if (stageResponse is StageResponse){
                    _payment.value?.activeTicket()?.stageResponse?.add(stageResponse)
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
//            println(cayanTransaction)
            if (cayanTransaction is String){
                cancelCredit()
                _error.postValue(true)
            }

            if (cayanTransaction is CayanTransaction){
                if (cayanTransaction.Status.toUpperCase() == "APPROVED"){
                    processApproval(livePayment.value!!.activeTicket()!!, cayanTransaction)
                }else{
                    processDecline(cayanTransaction)
                }
            }
        }
    }

    fun processApproval(ticket: Ticket, cayanTransaction: CayanTransaction){
        if (cayanTransaction.PaymentType == "GIFT"){
            ticket.paymentType = "Gift"
        }else{
            ticket.paymentType = "Credit"
        }

        ticket.paymentTotal = cayanTransaction.AmountApproved.toDouble()

        val cc: CreditCardTransaction = creditCardRepository.createCreditCardTransaction(ticket, cayanTransaction);
        ticket.creditCardTransactions.add(cc);

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

        if (livePayment.value!!.allTicketsPaid()){
            _payment.value!!.close()
            _payment.value = _payment.value
        }else{
            _payment.value = _payment.value
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
            };
            if (error.contains("referral")){
                setError("Credit Declined", DECLINED_MORE_INFO)
            };
            if (error == ""){
                setError("Credit Declined", DECLINED_MORE_INFO)
            };
            if (error.contains("field format error")){
                setError("Credit Declined", BAD_VALUE)
            };
            if (error.contains("declined insufficient funds available")){
                setError("Credit Declined", INSUFFICIENT_FUNDS)
            }
        };

        if (status == "REFERRAL"){
            if (error.contains("referral")){
                setError("Credit Declined", DECLINED_MORE_INFO)
            };
        }

        if (status == "DECLINED_DUPLICATE"){
            setError("Credit Declined", DUPLICATE)
        };

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
            val terminal = loginRepository.getTerminal()
            try {
                val url =
                    "http://" + terminal?.ccEquipment?.ipAddress + ":8080/pos?Action=Cancel&Format=JSON"
                val response = cancelCredit.cancelCreditProcess(url)
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
        _payment.value?.splitByGuest(order)
        _payment.value = _payment.value
    }

    fun noSplit(order: Order){
        _payment.value?.createSingleTicket(order)
        _payment.value = _payment.value
    }

    fun evenSplit(order: Order){
        _payment.value?.splitEvenly(order)
        _payment.value = _payment.value
    }

    fun toggleTicketMore(){
        _showTicketMore.value = !_showTicketMore.value!!
    }

    fun setPaymentScreen(showPayment: ShowPayment){
        _paymentScreen.value = showPayment
    }


    fun voidTicket(order: Order){
        _payment.value?.voidTicket()
        _payment.value = _payment.value

        val approval = approvalRepository.createVoidTicketApproval(order, livePayment.value!!)

        savePaymentToCloud()
        saveApprovalToCloud(approval)
    }

    fun discountTicket(order: Order, discount: Discount){
        var disTotal: Double = 0.00
        disTotal = _payment.value?.discountTicket(discount)!!
        _payment.value = _payment.value
        _paymentScreen.value = ShowPayment.NONE
        val approval = approvalRepository.createDiscountTicketApproval(order, livePayment.value!!, discount, disTotal)

        savePaymentToCloud()
        saveApprovalToCloud(approval)
    }

    fun cancelDiscount(){
        _paymentScreen.value = ShowPayment.NONE
    }

    private fun saveApprovalToCloud(approval: Approval){
        viewModelScope.launch {
            var a: Approval
            if (approval._rid == ""){
                a = saveApproval.saveApproval(approval)
            }else{
                a = updateApproval.saveApproval(approval)
            }
        }
    }
}


