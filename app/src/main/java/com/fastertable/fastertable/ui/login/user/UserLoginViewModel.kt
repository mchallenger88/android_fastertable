package com.fastertable.fastertable.ui.login.user

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.*
import com.fastertable.fastertable.LoginActivity
import com.fastertable.fastertable.data.Terminal
import com.fastertable.fastertable.data.repository.LoginRepository
import com.fastertable.fastertable.data.repository.OrderRepository
import com.fastertable.fastertable.utils.GlobalUtils
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class UserLoginViewModel(private val loginRepository: LoginRepository, private val orderRepository: OrderRepository) : ViewModel() {
    private var cid: String = ""
    private var lid: String = ""
    private val _pin = MutableLiveData<String>()
    val pin: LiveData<String>
        get() = _pin

    private val _terminal = MutableLiveData<Terminal>()
    val terminal: LiveData<Terminal>
        get() = _terminal

    private val _navigate = MutableLiveData<Boolean>()
    val navigate: LiveData<Boolean>
        get() = _navigate

    private val _showProgressBar = MutableLiveData<Boolean>()
    val showProgressBar: LiveData<Boolean>
        get() = _showProgressBar


    init{
        _pin.value = ""
        _navigate.value = false
        _showProgressBar.value = false
        getTerminal()
    }

    @Suppress("UNNECESSARY_NOT_NULL_ASSERTION")
    private fun getTerminal(){
        viewModelScope.launch {
            val term = loginRepository.getTerminal()
            if (term != null){
                _terminal.postValue(term!!)
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
           getOrders()
           _showProgressBar.postValue(false)
           goHome()
           _navigate.value = true
       }
    }

    private suspend fun getUserLogin(pin: String, cid: String, lid: String, now: Long, midnight: Long){
        withContext(IO){
            loginRepository.loginUser(pin, cid, lid, now, midnight)
        }
    }

    private suspend fun getOrders(){
        withContext(IO){
            val midnight = GlobalUtils().getMidnight()
            //    - 86400
            val rid = loginRepository.getStringSharedPreferences("rid")
            orderRepository.getOrders(midnight, rid!!)
        }
    }


    private fun goHome(){
        Handler(Looper.getMainLooper()).post {
            val activity: LoginActivity = LoginActivity()
            activity.goHome()
        }

    }

}