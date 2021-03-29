package com.fastertable.fastertable.ui.ui.login.user

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import androidx.core.app.NavUtils.navigateUpTo
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.*
import com.fastertable.fastertable.LoginActivity
import com.fastertable.fastertable.MainActivity
import com.fastertable.fastertable.OrderingActivity
import com.fastertable.fastertable.api.UserHelper
import com.fastertable.fastertable.api.UserService
import com.fastertable.fastertable.data.OpsAuth
import com.fastertable.fastertable.data.Order
import com.fastertable.fastertable.data.Terminal
import com.fastertable.fastertable.data.repository.LoginRepository
import com.fastertable.fastertable.utils.GlobalUtils
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File


class UserLoginViewModel(private val loginRepository: LoginRepository) : ViewModel() {
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

    init{
        _pin.value = ""
        _navigate.value = false
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
           cid = loginRepository.getStringSharedPreferences("cid")!!
           lid = loginRepository.getStringSharedPreferences("rid")!!
           val now = GlobalUtils().getNowEpoch()
           val midnight = GlobalUtils().getMidnight()
           getUserLogin(pin.value.toString(), cid, lid, now, midnight)
           goHome()
           _navigate.value = true
       }
    }

    private suspend fun getUserLogin(pin: String, cid: String, lid: String, now: Long, midnight: Long){
        withContext(IO){
            loginRepository.loginUser(pin, cid, lid, now, midnight)

        }
    }



    private fun goHome(){
        Handler(Looper.getMainLooper()).post {
            val activity: LoginActivity = LoginActivity()
            activity.goHome()
        }

    }

}