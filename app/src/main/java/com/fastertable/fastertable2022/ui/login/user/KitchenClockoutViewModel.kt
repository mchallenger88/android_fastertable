package com.fastertable.fastertable2022.ui.login.user

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fastertable.fastertable2022.common.base.BaseViewModel
import com.fastertable.fastertable2022.data.models.ClockOutCredentials
import com.fastertable.fastertable2022.data.models.OpsAuth
import com.fastertable.fastertable2022.data.models.Settings
import com.fastertable.fastertable2022.data.models.UserClock
import com.fastertable.fastertable2022.data.repository.ClockoutUser
import com.fastertable.fastertable2022.data.repository.LoginRepository
import com.fastertable.fastertable2022.utils.GlobalUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class KitchenClockoutViewModel @Inject constructor (
    private val loginRepository: LoginRepository,
    private val clockoutUser: ClockoutUser
) : BaseViewModel(){

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


    init{
        _clockedOut.value = false
    }

    fun clockout(){
        if (userClockout.value?.clockOutTime == null){
            performClockout()
            _errorMessage.value = "You have been clocked out."
        }
    }

    private fun performClockout(){
        viewModelScope.launch {
            val clockoutCredentials = ClockOutCredentials(
                employeeId = user?.employeeId ?: "",
                companyId = settings?.companyId ?: "",
                locationId = settings?.locationId ?: "",
                time = GlobalUtils().getNowEpoch(),
                midnight = GlobalUtils().getMidnight()
            )
            clockoutUser.clockout(clockoutCredentials)
            _clockedOut.postValue(true)
        }
    }
}