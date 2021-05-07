package com.fastertable.fastertable.ui.payment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fastertable.fastertable.common.base.BaseViewModel
import com.fastertable.fastertable.data.models.*
import com.fastertable.fastertable.data.repository.LoginRepository
import com.fastertable.fastertable.data.repository.OrderRepository
import com.fastertable.fastertable.data.repository.PaymentRepository
import com.fastertable.fastertable.utils.round
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.math.absoluteValue


@HiltViewModel
class PaymentViewModel @Inject constructor (private val loginRepository: LoginRepository,
                                           private val orderRepository: OrderRepository,
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

    private val _calculatingCash = MutableLiveData<String>()
    private val calculatingCash: LiveData<String>
        get() = _calculatingCash

    init{
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
            }
        }
    }

    fun setLivePayment(payment: Payment){
        _payment.value = payment
    }

    fun setActiveTicket(ticket: Ticket){
        _payment.value?.tickets?.forEach { t ->
            ticket.uiActive = t.id == ticket.id
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


}


