package com.fastertable.fastertable.ui.clockout

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fastertable.fastertable.common.base.BaseViewModel
import com.fastertable.fastertable.data.models.*
import com.fastertable.fastertable.data.repository.ClockoutUser
import com.fastertable.fastertable.data.repository.LoginRepository
import com.fastertable.fastertable.data.repository.OrderRepository
import com.fastertable.fastertable.utils.GlobalUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClockoutViewModel @Inject constructor (
    private val loginRepository: LoginRepository,
    private val orderRepository: OrderRepository,
    private val clockoutUser: ClockoutUser) : BaseViewModel(){

    val settings: Settings? = loginRepository.getSettings()
    val user: OpsAuth? = loginRepository.getOpsUser()

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String>
        get() = _errorMessage

    private val _userClockout = MutableLiveData<UserClock?>()
    val userClockout: LiveData<UserClock?>
        get() = _userClockout

    private val _clockedOut = MutableLiveData<Boolean>()
    val clockedOut: LiveData<Boolean>
        get() = _clockedOut

    private var orders: List<Order>? = null

    init{
        _clockedOut.value = false
        viewModelScope.launch {
            getOrders()
        }
    }

    private fun getOrders(){
        viewModelScope.launch {
            orders = orderRepository.getOrdersFromFile()
        }

    }

    fun clockout(){
        if (settings != null && user !=  null) {
            if (settings.requireCheckoutConfirm) {
                if (user.userClock.checkoutApproved) {
                    if (userClockout.value?.clockOutTime == null) {
                        performClockout()
                        _errorMessage.value = "You have been clocked out."
                    }
                } else {
                    val empOrders = orders?.filter { it.employeeId == user.employeeId }
                    if (empOrders != null) {
                        if (empOrders.isEmpty()) {
                            performClockout()
                            _errorMessage.value = "You have been clocked out."
                        } else {
                            _errorMessage.value =
                                "Your checkout has not yet been approved. Once your checkout is approved, you may clock out."
                        }
                    } else {
                        _errorMessage.value =
                            "Your checkout has not yet been approved. Once your checkout is approved, you may clock out."
                    }
                }
            } else {
                if (user.userClock.checkout == true) {
                    if (userClockout.value?.clockOutTime == null) {
                        performClockout()
                        _errorMessage.value = "You have been clocked out."
                    }
                } else {
                    _errorMessage.value = "You must checkout before clocking out."
                }
            }
        }
    }

    private fun performClockout(){
        viewModelScope.launch {
            if (user != null && settings != null){
                val clockoutCredentials = ClockOutCredentials(
                    employeeId = user.employeeId,
                    companyId = settings.companyId,
                    locationId = settings.locationId,
                    time = GlobalUtils().getNowEpoch(),
                    midnight = GlobalUtils().getMidnight()
                )
                clockoutUser.clockout(clockoutCredentials)
                _clockedOut.postValue(true)
            }

        }
    }
}