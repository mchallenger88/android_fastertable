package com.fastertable.fastertable2022.ui.approvals

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fastertable.fastertable2022.common.base.BaseViewModel
import com.fastertable.fastertable2022.data.models.*
import com.fastertable.fastertable2022.data.repository.*
import com.fastertable.fastertable2022.utils.GlobalUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ApprovalsViewModel @Inject constructor(
                                        private val getApprovals: GetApprovals,
                                        private val approvalRepository: ApprovalRepository,
                                        private val updatePayment: UpdatePayment,
                                        private val updateApproval: UpdateApproval,
                                        private val updateOrder: UpdateOrder,
                                        private val loginRepository: LoginRepository) : BaseViewModel() {

    private val _approvals = MutableLiveData<MutableList<ApprovalOrderPayment>>()
    val approvals: LiveData<MutableList<ApprovalOrderPayment>>
        get() = _approvals

    private val _approvalTicket = MutableLiveData<ApprovalTicket?>()
    val approvalTicket: LiveData<ApprovalTicket?>
        get() = _approvalTicket

    private val _activeApproval = MutableLiveData<ApprovalOrderPayment?>()
    val activeApproval: LiveData<ApprovalOrderPayment?>
        get() = _activeApproval

    private val _showPending = MutableLiveData(true)
    val showPending: LiveData<Boolean>
        get() = _showPending

    private val _navigateToCompleted = MutableLiveData(false)
    val navigateToCompleted: LiveData<Boolean>
        get() = _navigateToCompleted

    private val _showProgress = MutableLiveData(false)
    val showProgress: LiveData<Boolean>
        get() = _showProgress

    private val _discountAmount = MutableLiveData(0.00)
    val discountAmount: LiveData<Double>
        get() = _discountAmount

    private val _orderSaved = MutableLiveData(false)
    val orderSaved: LiveData<Boolean>
        get() = _orderSaved

    private val _enableButton = MutableLiveData(true)
    val enableButton: LiveData<Boolean>
        get() = _enableButton

    init{
        viewModelScope.launch {
            loadApprovals()
        }
    }

    suspend fun loadApprovals(){
        viewModelScope.launch {
            val settings = loginRepository.getSettings()
            settings?.let {
                val timeBasedRequest = TimeBasedRequest(
                    midnight = GlobalUtils().getMidnight(),
                    locationId = it.locationId
                )
                val list = getApprovals.getApprovals(timeBasedRequest)
                sortApprovals(list)
            }
        }
    }

    private fun sortApprovals(list: List<ApprovalOrderPayment>){
        var listNew: MutableList<ApprovalOrderPayment>
        if (_showPending.value == true){
            listNew = list.filter { it.approval.timeHandled == null } as MutableList<ApprovalOrderPayment>
            val distinctList = listNew.distinctBy{it.payment.id} as MutableList
            _approvals.value = distinctList
            setActiveApproval()
        }


    }

    fun setActiveApproval(){
        val list = _approvals.value
        list?.let {
            if (list.isNotEmpty()){
                val aop = list[0]
                if (aop.approval.timeHandled == null){
                    _activeApproval.value = aop
                    val ticket = aop.payment.tickets?.find{it.id == aop.approval.ticketId}
                    ticket?.let {
                        val at = ApprovalTicket(
                            approval = aop.approval,
                            ticket = it
                        )
                        _approvalTicket.value = at
                    }

                    if (aop.approval.timeHandled != null){
                        calculateApprovedAmount()
                    }
                }else{
                    _activeApproval.value = null
                }
            }
        }
    }


    private fun findApproval(id: String){
        val aop = _approvals.value?.find{it.approval.id == id}
        aop?.let {
            _activeApproval.value = it
            val ticket = it.payment.tickets?.find{a -> a.id == it.approval.ticketId}
            ticket?.let { ticket ->
                val at = ApprovalTicket(
                    approval = it.approval,
                    ticket = ticket
                )
                _approvalTicket.value = at
                if (aop.approval.timeHandled != null){
                    calculateApprovedAmount()
                }
            }

        }

    }

    private fun calculateApprovedAmount(){
        _discountAmount.value = 0.00
        val ticket = _approvalTicket.value
        if (ticket?.ticket?.ticketItems?.isNotEmpty() == true){
            for (item in ticket.ticket.ticketItems){
                val dis = item.itemPrice.times(item.quantity).minus(item.ticketItemPrice)
                _discountAmount.value = _discountAmount.value?.plus(dis)
            }
        }

    }

    fun setPending(b: Boolean){
        _navigateToCompleted.value = b
    }

    fun save(){
        _enableButton.value = false
        _showProgress.postValue(true)
        viewModelScope.launch {
            processApproval()
        }
    }

    private suspend fun processApproval(){
        val job = viewModelScope.launch {

            val ticket = _approvalTicket.value?.ticket

            if (ticket != null){
                val approved = ticket.ticketItems.filter { it.uiApproved && it.discountPrice != null}
                val rejected = ticket.ticketItems.filter { !it.uiApproved && it.discountPrice != null}
                if (approved.isNotEmpty()){
                    approveTicketItem(approved)
                }
                if (rejected.isNotEmpty()){
                    rejectTicketItem(rejected)
                }

            }
        }

        job.join()
        _approvals.value?.let { approvals ->
            _activeApproval.value?.let { aa->
                val index = approvals.indexOfFirst { it.approval.id == aa.approval.id}
                approvals.removeAt(index)
                _approvals.value = approvals

                if (approvals.isNotEmpty()){
                    findApproval(approvals[0].approval.id)
                }else{
                    _activeApproval.value = null
                }
            }
        }

        if (_approvals.value == null){
            _activeApproval.value = null
        }

        _enableButton.value = true
        _showProgress.postValue(false)

    }

    private fun approveTicketItem(list: List<TicketItem>){
        viewModelScope.launch {
            val aop = _activeApproval.value
            aop?.let { approval ->
                approval.payment.tickets?.let { tickets ->
                    val ticket = tickets.find { it.id == approval.approval.ticketId }
                    ticket?.let {
                        for (item in list){
                            item.discountPrice?.let {
                                item.approve(it)
                            }
                            approval.approval.approved = true
                            approval.approval.timeHandled = GlobalUtils().getNowEpoch()
                            approval.approval.managerId = loginRepository.getOpsUser()?.employeeId
                            updateApproval.saveApproval(approval.approval)
                            updateApproval.saveApproval(approval.approval)
                        }
                        ticket.recalculateAfterApproval()
                        approval.payment.statusApproval = "Approved"
                        if (approval.payment.allTicketsPaid()){
                            approval.payment.close()
                            approval.order.close()
                        }

                        updatePayment.savePayment(approval.payment)

                        approval.order.pendingApproval = true
                        updateOrder.saveOrder(approval.order)
                        setOrderSaved(true)
                        approvalRepository.updateApprovalOrderPayment(approval)
                    }
                }
            }
        }
    }

    private fun rejectTicketItem(list: List<TicketItem>){
        viewModelScope.launch {
            val aop = _activeApproval.value
            aop?.let { approval ->
                approval.payment.tickets?.let { tickets ->
                    val ticket = tickets.find { it.id == approval.approval.ticketId }
                    ticket?.let {
                        for (item in list){
                            item.reject()
                            approval.approval.approved = false
                            approval.approval.timeHandled = GlobalUtils().getNowEpoch()
                            approval.approval.managerId = loginRepository.getOpsUser()?.employeeId
                            updateApproval.saveApproval(aop.approval)
                        }

                        approval.payment.statusApproval = "Rejected"
                        updatePayment.savePayment(approval.payment)

                        approval.order.pendingApproval = false
                        updateOrder.saveOrder(approval.order)
                        setOrderSaved(true)
                        approvalRepository.updateApprovalOrderPayment(approval)
                    }
                }
            }
        }
    }


    fun addAllToList(b: Boolean){
        val list = _approvalTicket.value?.ticket?.ticketItems
        list?.let {
            if (b){
                for (item in it){
                    if (item.discountPrice != null){
                        item.uiApproved = true
                    }
                }
            }else{
                for (item in it){
                    if (item.discountPrice != null){
                        item.uiApproved = false
                    }
                }
            }
        }

        _approvalTicket.value = _approvalTicket.value
    }

    fun addItemToApprovalList(item: TicketItem){
        item.discountPrice?.let { disc ->
            item.uiApproved = true
            val discount = item.ticketItemPrice.minus(disc)
            _discountAmount.value = _discountAmount.value?.plus(discount)
        }
    }

    fun addItemToRejectList(item: TicketItem){
        item.discountPrice?.let { disc ->
            item.uiApproved = false
            val discount = item.ticketItemPrice.minus(disc)
            _discountAmount.value = _discountAmount.value?.minus(discount)
        }
    }


    fun onApprovalSidebarClick(approval: ApprovalOrderPayment){
        viewModelScope.launch {
            findApproval(approval.approval.id)
        }
    }

    fun setOrderSaved(b: Boolean){
        _orderSaved.value = b
    }

}