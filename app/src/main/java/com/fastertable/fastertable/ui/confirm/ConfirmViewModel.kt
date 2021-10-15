package com.fastertable.fastertable.ui.confirm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fastertable.fastertable.common.base.BaseViewModel
import com.fastertable.fastertable.data.models.*
import com.fastertable.fastertable.data.repository.ConfirmCheckout
import com.fastertable.fastertable.data.repository.GetConfirmList
import com.fastertable.fastertable.data.repository.LoginRepository
import com.fastertable.fastertable.utils.GlobalUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ConfirmViewModel @Inject constructor (
    private val loginRepository: LoginRepository,
    private val getConfirmList: GetConfirmList,
    private val confirmCheckout: ConfirmCheckout
        ): BaseViewModel() {

    val settings: Settings = loginRepository.getSettings()!!
    private val midnight: Long = GlobalUtils().getMidnight()

    private val _confirmList = MutableLiveData<List<ConfirmEmployee>>()
    val confirmList: LiveData<List<ConfirmEmployee>>
        get() = _confirmList

    private val _activeDate = MutableLiveData<LocalDate>()
    val activeDate: LiveData<LocalDate>
        get() = _activeDate

    private val _progressVisibility = MutableLiveData<Boolean>()
    val progressVisibility: LiveData<Boolean>
        get() = _progressVisibility


    init {
        _activeDate.value = LocalDate.now()
        getConfirmList()
    }

    fun getConfirmList(){
        viewModelScope.launch {
            _progressVisibility.value = true
            val rollingMidnight = GlobalUtils().unixMidnight(_activeDate.value!!)

            val request: CompanyTimeBasedRequest = if (midnight == rollingMidnight){
                CompanyTimeBasedRequest(
                    midnight = midnight,
                    locationId = settings.locationId,
                    companyId = settings.companyId
                )
            } else{
                CompanyTimeBasedRequest(
                    midnight = rollingMidnight,
                    locationId = settings.locationId,
                    companyId = settings.companyId
                )
            }
            var list = getConfirmList.getList(request)
            list = list.filter { it.orders?.size!! > 0 }
            _confirmList.postValue(list)
            _progressVisibility.value = false
        }
    }

    fun setDate(date: Date){
        _activeDate.value = Instant.ofEpochMilli(date.time)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        getConfirmList()
    }

    fun confirm(confirmEmployee: ConfirmEmployee) {
        viewModelScope.launch {
            val user = loginRepository.getOpsUser()
            if (user != null) {
                val cc = CheckoutCredentials(
                    employeeId = user.employeeId,
                    companyId = settings.companyId,
                    locationId = settings.locationId,
                    checkout = true,
                    midnight = GlobalUtils().getMidnight()
                )
                confirmCheckout.confirm(cc)
                getConfirmList()
            }
        }
    }


}