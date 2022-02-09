package com.fastertable.fastertable2022.ui.confirm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fastertable.fastertable2022.common.base.BaseViewModel
import com.fastertable.fastertable2022.data.models.*
import com.fastertable.fastertable2022.data.repository.ConfirmCheckout
import com.fastertable.fastertable2022.data.repository.GetConfirmList
import com.fastertable.fastertable2022.data.repository.LoginRepository
import com.fastertable.fastertable2022.utils.GlobalUtils
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

    val settings: Settings? = loginRepository.getSettings()
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
            var rollingMidnight = midnight
            _activeDate.value?.let {
                rollingMidnight = GlobalUtils().unixMidnight(it)
            }

            val request: CompanyTimeBasedRequest = if (midnight == rollingMidnight){
                CompanyTimeBasedRequest(
                    midnight = midnight,
                    locationId = settings?.locationId ?: "",
                    companyId = settings?.companyId ?: ""
                )
            } else{
                CompanyTimeBasedRequest(
                    midnight = rollingMidnight,
                    locationId = settings?.locationId ?: "",
                    companyId = settings?.companyId ?: ""
                )
            }
            var list = getConfirmList.getList(request)
            list = filterListByOrders(list)
            _confirmList.postValue(list)
            _progressVisibility.value = false
        }
    }

    private fun filterListByOrders(list: List<ConfirmEmployee>): List<ConfirmEmployee>{
        val newList = mutableListOf<ConfirmEmployee>()
        for (emp in list){
            if (emp.orders?.isNullOrEmpty() == false){
                newList.add(emp)
            }
        }
        return newList
    }

    fun setDate(date: Date){
        _activeDate.value = Instant.ofEpochMilli(date.time)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        getConfirmList()
    }

    fun confirm(confirmEmployee: ConfirmEmployee) {
        viewModelScope.launch {
            _activeDate.value?.let {
                val date = GlobalUtils().unixMidnight(it)
                val cc = CheckoutCredentials(
                    employeeId = confirmEmployee.employeeId,
                    companyId = settings?.companyId ?: "",
                    locationId = settings?.locationId ?: "",
                    checkout = true,
                    midnight = date,

                )
                confirmCheckout.confirm(cc)
                getConfirmList()

            }

        }
    }


}