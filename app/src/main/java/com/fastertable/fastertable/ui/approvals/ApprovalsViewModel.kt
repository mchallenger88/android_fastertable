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
                                        private val approvalRepository: ApprovalRepository,
                                        private val updatePayment: UpdatePayment,
                                        private val updateApproval: UpdateApproval,
                                        private val updateOrder: UpdateOrder,
                                        private val loginRepository: LoginRepository) : BaseViewModel() {

    private val _approvals = MutableLiveData<List<ApprovalOrderPayment>>()
    val approvals: LiveData<List<ApprovalOrderPayment>>
        get() = _approvals

    private val _approvalsShown = MutableLiveData<List<ApprovalOrderPayment>?>()
    val approvalsShown: LiveData<List<ApprovalOrderPayment>?>
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

    private val _orderSaved = MutableLiveData(false)
    val orderSaved: LiveData<Boolean>
        get() = _orderSaved

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

    fun findFirstApproval(){
        val list = _approvalsShown.value
        if (!list.isNullOrEmpty()){
            val aop = list[0]
            _activeApproval.value = aop
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

    private suspend fun processApproval(){
        val job = viewModelScope.launch {
            _showProgress.postValue(true)
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
        _activeApproval.postValue(null)
        _approvalsShown.postValue(null)
        approvalRepository.getApprovalOrderPaymentsFromFile()
        _showProgress.postValue(false)

    }

    private fun findTicketItemApproval(item: TicketItem): ApprovalOrderPayment? {
        if (_approvals.value != null){
            for (aop in _approvals.value!!){
                if (aop.approval.id == item.approvalId){
                    return aop
                }
            }
        }
        return null
    }

    private fun approveTicketItem(list: List<TicketItem>){
        viewModelScope.launch {
            val aop = findTicketItemApproval(list[0])
            if (aop != null){
                val ticket = aop.payment.tickets!!.find{ it.id == aop.approval.ticketId}
                if (ticket != null){
                    for (item in list){
                        item.approve()
                        val tempAop = findTicketItemApproval(item)
                        tempAop?.approval?.approved = true
                        tempAop?.approval?.timeHandled = GlobalUtils().getNowEpoch()
                        tempAop?.approval?.managerId = loginRepository.getOpsUser()?.employeeId
                        updateApproval.saveApproval(tempAop?.approval!!)
                    }

                    ticket.recalculateAfterApproval()

                    aop.payment.statusApproval = "Approved"

                    //Check the total owed and if it's zero then close the check
                    if (aop.payment.allTicketsPaid()){
                        aop.payment.close()
                        aop.order.close()
                    }

                    updatePayment.savePayment(aop.payment)

                    aop.order.pendingApproval = false
                    updateOrder.saveOrder(aop.order)
                    setOrderSaved(true)
                    approvalRepository.updateApprovalOrderPayment(aop)
                }
            }
        }
    }

    private fun rejectTicketItem(list: List<TicketItem>){
        viewModelScope.launch {
            val aop = findTicketItemApproval(list[0])
            if (aop != null){
                val ticket = aop.payment.tickets!!.find{ it.id == aop.approval.ticketId}
                Log.d("ApprovalTesting", ticket.toString())
                if (ticket != null){
                    for (item in list){
                        Log.d("ApprovalTesting", item.toString())
                        item.reject()
                        Log.d("ApprovalTesting", item.toString())
                        val tempAop = findTicketItemApproval(item)
                        tempAop?.approval?.approved = false
                        tempAop?.approval?.timeHandled = GlobalUtils().getNowEpoch()
                        tempAop?.approval?.managerId = loginRepository.getOpsUser()?.employeeId
                        updateApproval.saveApproval(tempAop?.approval!!)
                    }

                    aop.payment.statusApproval = "Rejected"
                    Log.d("ApprovalTesting", aop.payment.tickets!![0].toString())
                    updatePayment.savePayment(aop.payment)

                    aop.order.pendingApproval = false
                    updateOrder.saveOrder(aop.order)
                    setOrderSaved(true)

                }
            }
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

    fun setOrderSaved(b: Boolean){
        _orderSaved.value = b
    }

}