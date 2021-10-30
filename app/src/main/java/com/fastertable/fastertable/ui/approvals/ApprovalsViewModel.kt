package com.fastertable.fastertable.ui.approvals

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fastertable.fastertable.common.base.BaseViewModel
import com.fastertable.fastertable.data.models.*
import com.fastertable.fastertable.data.repository.*
import com.fastertable.fastertable.utils.GlobalUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ApprovalsViewModel @Inject constructor(
                                        private val getApprovals: GetApprovals,
                                        private val orderRepository: OrderRepository,
                                        private val paymentRepository: PaymentRepository,
                                        private val getPayment: GetPayment,
                                        private val getOrder: GetOrder,
                                        private val updatePayment: UpdatePayment,
                                        private val updateApproval: UpdateApproval,
                                        private val updateOrder: UpdateOrder,
                                        private val loginRepository: LoginRepository) : BaseViewModel() {

    private val _approvals = MutableLiveData<List<ApprovalOrderPayment>>()
    val approvals: LiveData<List<ApprovalOrderPayment>>
        get() = _approvals

    private val _approvalsShown = MutableLiveData<List<ApprovalOrderPayment>>()
    val approvalsShown: LiveData<List<ApprovalOrderPayment>>
        get() = _approvalsShown

    private val _approvalTicket = MutableLiveData<ApprovalTicket?>()
    val approvalTicket: LiveData<ApprovalTicket?>
        get() = _approvalTicket

    private val _activeApproval = MutableLiveData<ApprovalOrderPayment?>()
    val activeApproval: LiveData<ApprovalOrderPayment?>
        get() = _activeApproval

    private val _showPending = MutableLiveData(true)
    val showPending: LiveData<Boolean>
        get() = _showPending

    private val _showProgress = MutableLiveData(false)
    val showProgress: LiveData<Boolean>
        get() = _showProgress

    private val _discountAmount = MutableLiveData(0.00)
    val discountAmount: LiveData<Double>
        get() = _discountAmount

    init{
        viewModelScope.launch {
            loadApprovals()
        }
    }

    private suspend fun loadApprovals(){
        viewModelScope.launch {
            if (loginRepository.getSettings() != null){
                val timeBasedRequest = TimeBasedRequest(
                    midnight = GlobalUtils().getMidnight(),
                    locationId = loginRepository.getSettings()!!.locationId
                )
                val list = getApprovals.getApprovals(timeBasedRequest)
                _approvals.postValue(list)
                sortApprovals(list)

            }
        }
    }

    private fun sortApprovals(list: List<ApprovalOrderPayment>){
        var listNew = mutableListOf<ApprovalOrderPayment>()
        if (_showPending.value == true){
            listNew = list.filter { it.approval.timeHandled == null } as MutableList<ApprovalOrderPayment>
        }

        if (_showPending.value == false){
            listNew = list.filter { it.approval.timeHandled != null } as MutableList<ApprovalOrderPayment>
        }
        val distinctList = listNew.distinctBy{it.payment.id}

        _approvalsShown.postValue(distinctList)

    }


    private fun findApproval(id: String){
        val aop = _approvalsShown.value?.find{it.approval.id == id}
        if (aop != null){
            _activeApproval.value = aop!!
            val at = ApprovalTicket(
                approval = aop.approval,
                ticket = aop.payment.tickets?.find{it.id == aop.approval.ticketId}!!
            )
            _approvalTicket.value = at

            if (aop.approval.timeHandled != null){
                calculateApprovedAmount()
            }
        }
    }

    private fun calculateApprovedAmount(){
        _discountAmount.value = 0.00
        val ticket = _approvalTicket.value
        if (ticket?.ticket?.ticketItems?.size!! > 0){
            for (item in ticket.ticket.ticketItems){
                val dis = item.itemPrice.times(item.quantity).minus(item.ticketItemPrice)
                _discountAmount.value = _discountAmount.value?.plus(dis)
            }
        }

    }

    fun setPending(b: Boolean){
        _showPending.value = b
        _approvalTicket.value = null
        _activeApproval.value = null
        if (_approvals.value != null){

            sortApprovals(_approvals.value!!)
        }
    }

    fun save(){
        viewModelScope.launch {
            processApproval()
        }
    }

    private fun processApproval(){
        viewModelScope.launch {
            _showProgress.postValue(true)
            val aop = _activeApproval.value!!
            val ticket = _approvalTicket.value?.ticket
            if (ticket != null){
                for (item in ticket.ticketItems){
                    if (item.uiApproved){
                        approveTicketItem(item, aop.payment, aop.order)
                    }else{
                        rejectTicketItem(item, aop.payment, aop.order)
                    }
                }
            }
            _activeApproval.postValue(null)
            loadApprovals()
            _showProgress.postValue(false)

        }

    }

    private fun approveTicketItem(item: TicketItem, payment: Payment, order: Order){
        viewModelScope.launch {
            val aop = _activeApproval.value!!
            val ticket = payment.tickets!!.find{ it.id == aop.approval.ticketId}
            item.approve()

            ticket?.recalculateAfterApproval()
            payment.statusApproval = "Approved"

            //Check the total owed and if it's zero then close the check
            if (payment.allTicketsPaid()){
                payment.close()
                order.close()
            }
            updatePayment.savePayment(payment)

            order.pendingApproval = false
            updateOrder.saveOrder(order)

            aop.approval.approved = true
            aop.approval.timeHandled = GlobalUtils().getNowEpoch()
            aop.approval.managerId = loginRepository.getOpsUser()?.employeeId
            updateApproval.saveApproval(aop.approval)
        }
    }

    private fun rejectTicketItem(item: TicketItem, payment: Payment, order: Order){
        viewModelScope.launch {
            val aop = _activeApproval.value!!
            val ticket = payment.tickets!!.find{ it.id == aop.approval.ticketId}
            item.reject()

            ticket?.recalculateAfterApproval()
            payment.statusApproval = "Rejected"

            //Check the total owed and if it's zero then close the check
            if (payment.allTicketsPaid()){
                payment.close()
                order.close()
            }
            updatePayment.savePayment(payment)

            order.pendingApproval = false
            updateOrder.saveOrder(order)

            aop.approval.approved = true
            aop.approval.timeHandled = GlobalUtils().getNowEpoch()
            aop.approval.managerId = loginRepository.getOpsUser()?.employeeId
            updateApproval.saveApproval(aop.approval)
        }
    }


    fun addAllToList(b: Boolean){
        val list = _approvalTicket.value?.ticket?.ticketItems
        if (b){
            for (item in list!!){
                if (item.discountPrice != null){
                    item.uiApproved = true
                }
            }
        }else{
            for (item in list!!){
                if (item.discountPrice != null){
                    item.uiApproved = false
                }
            }
        }
        _approvalTicket.value = _approvalTicket.value
    }

    fun addItemToApprovalList(item: TicketItem){
        if (item.discountPrice != null){
            item.uiApproved = true
            val discount = item.ticketItemPrice.minus(item.discountPrice!!)
            _discountAmount.value = _discountAmount.value?.plus(discount)
        }

    }

    fun addItemToRejectList(item: TicketItem){
        if (item.discountPrice != null) {
            item.uiApproved = false
            val discount = item.ticketItemPrice.minus(item.discountPrice!!)
            _discountAmount.value = _discountAmount.value?.minus(discount)
        }
    }


    fun onApprovalSidebarClick(approval: ApprovalOrderPayment){
        viewModelScope.launch {
            findApproval(approval.approval.id)
        }
    }

}