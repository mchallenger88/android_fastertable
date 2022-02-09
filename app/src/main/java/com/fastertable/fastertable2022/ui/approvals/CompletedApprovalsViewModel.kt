package com.fastertable.fastertable2022.ui.approvals

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fastertable.fastertable2022.common.base.BaseViewModel
import com.fastertable.fastertable2022.data.models.ApprovalOrderPayment
import com.fastertable.fastertable2022.data.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CompletedApprovalsViewModel @Inject constructor(private val approvalRepository: ApprovalRepository): BaseViewModel() {

    private val _approvals = MutableLiveData<List<ApprovalOrderPayment>?>()
    val approvals: LiveData<List<ApprovalOrderPayment>?>
        get() = _approvals

    private val _navigateToApprovals = MutableLiveData(false)
    val navigateToApprovals: LiveData<Boolean>
        get() = _navigateToApprovals

    init{
        viewModelScope.launch {
            loadApprovals()
        }
    }

    private fun loadApprovals(){
        var list = approvalRepository.getApprovalOrderPaymentsFromFile()
        if (list != null){
            list = list.filter { it.approval.timeHandled != null }
            _approvals.value = list
        }


    }

    fun setPending(b: Boolean){
        _navigateToApprovals.value = true
    }
}