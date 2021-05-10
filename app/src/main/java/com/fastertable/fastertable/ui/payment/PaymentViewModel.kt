package com.fastertable.fastertable.ui.payment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fastertable.fastertable.common.base.BaseViewModel
import com.fastertable.fastertable.data.models.*
import com.fastertable.fastertable.data.repository.*
import com.fastertable.fastertable.utils.round
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.absoluteValue

enum class ShowPayment{
    NONE,
    CASH,
    CREDIT
}

@HiltViewModel
class PaymentViewModel @Inject constructor (private val loginRepository: LoginRepository,
                                           private val orderRepository: OrderRepository,
                                           private val savePayment: SavePayment,
                                           private val updatePayment: UpdatePayment,
                                           private val paymentRepository: PaymentRepository): BaseViewModel() {

    private lateinit var user: OpsAuth
    private lateinit var settings: Settings

    private val _order = MutableLiveData<Order>()
    val liveOrder: LiveData<Order>
        get() = _order

    private val _payment = MutableLiveData<Payment>()
    val livePayment: LiveData<Payment>
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

    init{
        _managePayment.value = false
        _paymentScreen.value = ShowPayment.NONE
        _calculatingCash.value = ""
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

    fun clearPayment(){
        _cashAmount.value = 0.00
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
        }
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


}


