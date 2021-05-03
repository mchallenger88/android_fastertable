package com.fastertable.fastertable.ui.payment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fastertable.fastertable.common.base.BaseViewModel
import com.fastertable.fastertable.data.models.*
import com.fastertable.fastertable.data.repository.LoginRepository
import com.fastertable.fastertable.data.repository.OrderRepository
import com.fastertable.fastertable.data.repository.PaymentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

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

    fun payNow(){

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
}