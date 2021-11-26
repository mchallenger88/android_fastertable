package com.fastertable.fastertable.ui.payment

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fastertable.fastertable.common.base.BaseViewModel
import com.fastertable.fastertable.data.models.*
import com.fastertable.fastertable.data.repository.LoginRepository
import com.fastertable.fastertable.data.repository.PaymentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SplitPaymentViewModel @Inject constructor (
    private val loginRepository: LoginRepository,
    private val paymentRepository: PaymentRepository): BaseViewModel() {
    val settings: Settings = loginRepository.getSettings()!!
    private val _activePayment = MutableLiveData<Payment?>()
    val activePayment: LiveData<Payment?>
        get() = _activePayment

    private val _ticketCount = MutableLiveData(1)
    val ticketCount: LiveData<Int>
        get() = _ticketCount

    private val _saveMove = MutableLiveData(false)
    val saveMove: LiveData<Boolean>
        get() = _saveMove

    private val _navigatePayment = MutableLiveData(false)
    val navigatePayment: LiveData<Boolean>
        get() = _navigatePayment

    private val ticketItemList = mutableListOf<TicketItem>()


    fun addTicket(){
        _ticketCount.value = _ticketCount.value?.plus(1)
        _ticketCount.value = _ticketCount.value
    }

    fun reduceTicket(){
        val tv = _ticketCount.value
        tv?.let {
           if (tv > 1){
               _ticketCount.value = _ticketCount.value?.minus(1)
               _ticketCount.value = _ticketCount.value
           }
        }
    }

    fun goToPayment(){
        setNavigatePayment(true)
    }

    fun setActivePayment(payment: Payment){
        _activePayment.value = payment
    }

    fun setAdhocGuestCount(ticketCount: Int){
        for (i in 1..ticketCount.minus(1)){
            _activePayment.value?.let {
                var fees = mutableListOf<AdditionalFees>()
                settings.additionalFees?.let { af ->
                    fees = af
                }
                val ticket = paymentRepository.createEmptyTicket(it, i.plus(1), fees)
                it.tickets?.add(ticket)
            }
        }
        _activePayment.value = _activePayment.value
    }

    fun selectedItem(ticketItem: TicketItem){
        _activePayment.value = _activePayment.value
    }

    fun moveItemsToTicket(selectedTicket: Ticket){
        ticketItemList.clear()
        _activePayment.value?.let { payment ->
            payment.tickets?.let { tickets ->
                for (tick in tickets){
                    if (tick.uiActive){
                        for (ti in tick.ticketItems){
                            if (ti.selected){
                                ti.selected = false
                                selectedTicket.ticketItems.add(ti)
                                ticketItemList.add(ti)
                            }
                        }

                        for (ti in ticketItemList){
                            tick.ticketItems.remove(ti)
                        }
                    }
                }
            }

            _activePayment.value?.let { payment ->
                payment.tickets?.let{
                    for (ticket in it){
                        ticket.uiActive = ticket.id == selectedTicket.id
                    }
                }
            }
        }
        _activePayment.value?.let {
            it.recalculateTotals()
        }
        _activePayment.value = _activePayment.value

    }

    fun saveItemMoveChanges(){
        setSaveMove(true)
    }

    fun setSaveMove(b: Boolean){
        _saveMove.value = b
    }

    fun setNavigatePayment(b: Boolean){
        _navigatePayment.value = b;
    }
}