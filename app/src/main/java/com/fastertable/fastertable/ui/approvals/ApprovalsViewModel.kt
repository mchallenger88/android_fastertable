package com.fastertable.fastertable.ui.approvals

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

    private val _approvals = MutableLiveData<List<Approval>>()
    val approvals: LiveData<List<Approval>>
        get() = _approvals

    private val _pendingApprovals = MutableLiveData<List<Approval>>()
    val pendingApprovals: LiveData<List<Approval>>
        get() = _pendingApprovals

    private val _completedApprovals = MutableLiveData<List<Approval>>()
    val completedApprovals: LiveData<List<Approval>>
        get() = _completedApprovals

    private val _approvalTicket = MutableLiveData<ApprovalTicket>()
    val approvalTicket: LiveData<ApprovalTicket>
        get() = _approvalTicket

    private val _activeApproval = MutableLiveData<Approval>()
    val activeApproval: LiveData<Approval>
        get() = _activeApproval

    private val _activePayment = MutableLiveData<Payment>()
    val activePayment: LiveData<Payment>
        get() = _activePayment

    private val _activeOrder = MutableLiveData<Order>()
    val activeOrder: LiveData<Order>
        get() = _activeOrder

//    private val _approvalItems = MutableLiveData<MutableList<ApprovalItem>>()
//    val approvalItems: LiveData<MutableList<ApprovalItem>>
//        get() = _approvalItems

    private val _showPending = MutableLiveData<Boolean>()
    val showPending: LiveData<Boolean>
        get() = _showPending

//    private val _liveApprovalItem = MutableLiveData<ApprovalItem>()
//    val liveApprovalItem: LiveData<ApprovalItem>
//        get() = _liveApprovalItem

    private val _showProgress = MutableLiveData<Boolean>()
    val showProgress: LiveData<Boolean>
        get() = _showProgress

    init{
        viewModelScope.launch {
            _showProgress.postValue(true)
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
                _showPending.postValue(true)
                _showProgress.postValue(false)
            }
        }
    }

    fun setApprovalsView(b: Boolean){
        viewModelScope.launch {
//            val list = arrayListOf<ApprovalItem>()
//            if (approvalItems.value != null){
//                _approvalItems.value!!.clear()
//                _approvalItems.value = _approvalItems.value
//            }

            _pendingApprovals.value = approvals.value?.filter { it.approved == null }
            _completedApprovals.value = approvals.value?.filter{ it.approved != null}

//            approvals.value?.forEach { it ->
//                if (b){
//                    list.addAll(it.getPending())
//                }else{
//                    list.addAll(it.getComplete())
//                }
//
//            }
//            if (list.size > 0){
//                list[0].uiActive = true
//                _liveApprovalItem.postValue(list[0])
//                findApproval(list[0])
//                _approvalItems.postValue(list)
//            }
        }
    }

//    private suspend fun findApproval(item: ApprovalItem){
//        _approvals.value?.forEach { a ->
//            a.approvalItems.forEach { ai ->
//                if (ai == item){
//                    _activeApproval.value = a
//                    getOrderPayment()
//                }
//            }
//        }
//    }

    private suspend fun findApproval(id: String){
        _activeApproval.value = _approvals.value?.find{it.id == id}
        if (_activeApproval.value != null){
            getOrderPayment()
        }
    }

    fun setPending(b: Boolean){
        _showPending.value = b
    }

    fun approveApproval(){
        viewModelScope.launch {
            processApproval()
        }
    }

    suspend fun processApproval(){
//        val job = viewModelScope.launch {
//            _showProgress.postValue(true)
//            getOrderPayment()
//        }
//
//        job.join()
        // TODO: use activepayment and activeorder
        viewModelScope.launch {
            _showProgress.postValue(true)
            val payment = paymentRepository.getPayment()
            val order = orderRepository.getOrder()
            if (payment != null && order != null){
                when (activeApproval.value?.approvalType){
                    "Discount Item" -> { approveTicketItem(payment, order) }
                    "Modify Price" -> { approveTicketItem(payment, order) }
                    "Void Item" -> { approveTicketItem(payment, order) }
                    "Void Ticket" -> { approveTicket(payment, order) }
                    "Discount Ticket" -> { approveTicket(payment, order) }
                }
                _activeApproval.value?.approved = true
                _activeApproval.value?.timeHandled = GlobalUtils().getNowEpoch()
                _activeApproval.value?.managerId = loginRepository.getOpsUser()?.employeeId
//                _liveApprovalItem.value = _liveApprovalItem.value
                updateApproval.saveApproval(activeApproval.value!!)

                order.pendingApproval = false
                updateOrder.saveOrder(order)
                setApprovalsView(true)
                _showProgress.postValue(false)
            }
        }

    }

    private suspend fun getOrderPayment(){
        viewModelScope.launch {
            _activePayment.value = getPayment.getPayment(activeApproval.value?.id!!.replace("A_", "P_"), activeApproval.value?.locationId!!)
            _activeOrder.value = getOrder.getOrder(activeApproval.value?.id!!.replace("A_", "O_"), activeApproval.value?.locationId!!)
            val at = ApprovalTicket(
                approval = _activeApproval.value!!,
                ticket = _activePayment.value!!.tickets!!.find{ it.id == _activeApproval.value!!.ticketId }!!
            )
            _approvalTicket.value = at
        }

    }

    private fun approveTicketItem(payment: Payment, order: Order){
        viewModelScope.launch {
            val ticket = payment.tickets!!.find{ it.id == _activeApproval.value?.ticketId}
            val ticketItem = ticket?.ticketItems?.find {it.id == _activeApproval.value?.ticketItemId }
            if (ticketItem != null) {
                ticketItem.discountPrice = _activeApproval.value?.newItemPrice
            }
//            ticket?.ticketItems?.find{ it -> it.id == ticketItem?.id}?.approve()
            ticket?.recalculateAfterApproval(order.taxRate)
            payment.statusApproval = "Approved"

            //Check the total owed and if it's zero then close the check
            if (payment.allTicketsPaid()){
                payment.close()
            }
            updatePayment.savePayment(payment)
        }
    }


    private fun approveTicket(payment: Payment, order: Order){
        viewModelScope.launch {
            val ticket = payment.tickets!!.find{ it.id == _activeApproval.value?.ticketId}
            if (_activeApproval.value?.approvalType == "Void"){
                ticket?.ticketItems?.forEach { it ->
                    it.discountPrice = 0.00
                }
            }

            if (_activeApproval.value?.approvalType == "Discount Ticket"){

            }

            ticket?.recalculateAfterApproval(order.taxRate)
            payment.statusApproval = "Approved"
            updatePayment.savePayment(payment)

            //TODO: If there are no other tickets and this is a void then we can close the order and payment
        }
    }

//    private fun rejectTicketItem(payment: Payment){
//        viewModelScope.launch {
////            val ticket = payment.tickets!!.find{it -> it.id == _liveApprovalItem.value?.ticket?.id}
//            val ticketItem = _liveApprovalItem.value?.ticketItem
//            ticketItem?.reject()
//            payment.statusApproval = "Rejected"
//            updatePayment.savePayment(payment)
//        }
//    }
//
//
//    private fun rejectTicket(payment: Payment){
//        viewModelScope.launch {
//            val ticket = payment.tickets!!.find{ it.id == _liveApprovalItem.value?.ticket?.id}
//            ticket?.ticketItems?.forEach { it ->
//                it.reject()
//            }
//            payment.statusApproval = "Rejected"
//            updatePayment.savePayment(payment)
//
//        }
//    }

    fun rejectApproval(){
        viewModelScope.launch {
            _showProgress.postValue(true)
//            getOrderPayment()
            val payment = paymentRepository.getPayment()
            val order = orderRepository.getOrder()
            if (payment != null && order != null){
//                when (liveApprovalItem.value?.approvalType){
//                    "Discount" -> { rejectTicketItem(payment) }
//                    "Modify Price" -> { rejectTicketItem(payment) }
//                    "Void Item" -> { rejectTicketItem(payment) }
//                    "Void Ticket" -> { rejectTicket(payment) }
//                    "Discount Ticket" -> { rejectTicket(payment) }
//                }
                _activeApproval.value?.approved = false
                _activeApproval.value?.timeHandled = GlobalUtils().getNowEpoch()
                _activeApproval.value?.managerId = loginRepository.getOpsUser()?.employeeId
//                _liveApprovalItem.value = _liveApprovalItem.value
                updateApproval.saveApproval(activeApproval.value!!)

                order.pendingApproval = false
                updateOrder.saveOrder(order)
                setApprovalsView(true)
                _showProgress.postValue(false)
            }
        }
    }

    fun onApprovalSidebarClick(approval: Approval){
        viewModelScope.launch {
            findApproval(approval.id)
//            _approvalItems.value?.forEach { it ->
//                if (it.timeRequested == approvalItem.timeRequested){
//                    it.uiActive = true
//                    _liveApprovalItem.value = it
//                    findApproval(it)
//                }else{
//                    it.uiActive = false
//                }
//
//            }
//            _approvalItems.value = _approvalItems.value
        }
    }

//    fun onApprovalSidebarClick(approvalItem: ApprovalItem){
//        viewModelScope.launch {
//            _approvalItems.value?.forEach { it ->
//                if (it.timeRequested == approvalItem.timeRequested){
//                    it.uiActive = true
//                    _liveApprovalItem.value = it
//                    findApproval(it)
//                }else{
//                    it.uiActive = false
//                }
//
//            }
//            _approvalItems.value = _approvalItems.value
//        }
//    }
}