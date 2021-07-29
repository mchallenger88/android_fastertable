package com.fastertable.fastertable.ui.login.user

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.*
import com.fastertable.fastertable.LoginActivity
import com.fastertable.fastertable.api.GetOrdersUseCase
import com.fastertable.fastertable.api.LoginUserUseCase
import com.fastertable.fastertable.common.base.BaseViewModel
import com.fastertable.fastertable.data.models.OpsAuth
import com.fastertable.fastertable.data.models.Terminal
import com.fastertable.fastertable.data.repository.GetOrders
import com.fastertable.fastertable.data.repository.LoginRepository
import com.fastertable.fastertable.data.repository.LoginUser
import com.fastertable.fastertable.data.repository.OrderRepository
import com.fastertable.fastertable.utils.GlobalUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class UserLoginViewModel @Inject constructor(private val loginRepository: LoginRepository,
                                             private val loginUser: LoginUser,
                                             private val getOrders: GetOrders) : BaseViewModel() {

    private var cid: String = ""
    private var lid: String = ""
    private val _pin = MutableLiveData<String>()
    val pin: LiveData<String>
        get() = _pin

    private val _terminal = MutableLiveData<Terminal?>()
    val liveTerminal: LiveData<Terminal?>
        get() = _terminal

    private val _navigate = MutableLiveData<Boolean>()
    val navigate: LiveData<Boolean>
        get() = _navigate

    private val _showProgressBar = MutableLiveData<Boolean>()
    val showProgressBar: LiveData<Boolean>
        get() = _showProgressBar

    private val _validUser = MutableLiveData<Boolean?>()
    val validUser: LiveData<Boolean?>
        get() = _validUser

    private val _clockin = MutableLiveData<Boolean>()
    val clockin: LiveData<Boolean>
        get() = _clockin

    private val _loginTime = MutableLiveData<Long>()
    val loginTime: LiveData<Long>
        get() = _loginTime


    init{
        _pin.value = ""
        _navigate.value = false
        _showProgressBar.value = false
        getTerminal()
    }

    private fun getTerminal(){
        viewModelScope.launch {
            val term = loginRepository.getTerminal()
            if (term != null){
                _terminal.postValue(term)
            }
        }
    }

    fun concatPin(num: Int){
        _pin.value = pin.value + num.toString()
    }

    fun pinClear(){
        _pin.value = ""
    }

    fun userLogin(){
       viewModelScope.launch {
           _showProgressBar.postValue(true)
           cid = loginRepository.getStringSharedPreferences("cid")!!
           lid = loginRepository.getStringSharedPreferences("rid")!!
           val now = GlobalUtils().getNowEpoch()
           val midnight = GlobalUtils().getMidnight()
           getUserLogin(pin.value.toString(), cid, lid, now, midnight)

       }
    }

    private suspend fun getUserLogin(pin: String, cid: String, lid: String, now: Long, midnight: Long){
        viewModelScope.launch {
            val user: OpsAuth? = loginUser.loginUser(pin, cid, lid, now, midnight)
            if (user?.isAuthenticated!!){
                _loginTime.postValue(now)
                _validUser.postValue(true)
                getOrders()
                _showProgressBar.postValue(false)
                if (isClockIn(user, now)){
                    _clockin.postValue(true)
                }else{
                    _navigate.postValue(true)
                }


            }else{
                _validUser.postValue(false)
                _pin.value = ""
                _showProgressBar.postValue(false)
            }
        }
    }

    fun navigateToHome(){
        _navigate.postValue(true)
    }

    private suspend fun getOrders(){
        withContext(IO){
            val midnight = GlobalUtils().getMidnight()
            //    - 86400
            val rid = loginRepository.getStringSharedPreferences("rid")
            getOrders.getOrders(midnight, rid!!)
        }
    }

    fun setUserValid(){
        _validUser.value = null
    }

    private fun isClockIn(user: OpsAuth, loginTime: Long): Boolean{
        return user.userClock.clockInTime == loginTime
    }


}