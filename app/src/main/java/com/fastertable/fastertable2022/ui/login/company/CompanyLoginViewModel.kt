package com.fastertable.fastertable2022.ui.login.company

import android.annotation.SuppressLint
import androidx.lifecycle.*
import com.fastertable.fastertable2022.data.models.Company
import com.fastertable.fastertable2022.data.models.Location
import com.fastertable.fastertable2022.data.repository.LoginCompany
import com.fastertable.fastertable2022.data.repository.LoginRepository
import com.fastertable.fastertable2022.utils.ApiStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CompanyLoginViewModel @Inject constructor(
    private val loginCompany: LoginCompany,
    private val loginRepository: LoginRepository) : ViewModel() {

    private val _company = MutableLiveData<Company>()
    val company: LiveData<Company>
        get() = _company

    private val _loginName = MutableLiveData<String>()
    val loginName: LiveData<String>
        get() = _loginName

    private val _password = MutableLiveData<String>()
    val password: LiveData<String>
        get() = _password

    private val _locations = MutableLiveData<List<Location>>()
    val locations: LiveData<List<Location>>
        get() = _locations

    private val _restaurant = MutableLiveData<Location>()
    val restaurant: LiveData<Location>
        get() = _restaurant

    private val _showProgressBar = MutableLiveData(false)
    val showProgressBar: LiveData<Boolean>
        get() = _showProgressBar

    private val _error = MutableLiveData<Boolean>()
    val error: LiveData<Boolean>
        get() = _error

    private val _status = MutableLiveData<ApiStatus>()
    val status: LiveData<ApiStatus>
        get() = _status

    private val _loginEnabled = MutableLiveData<Boolean>()
    val loginEnabled: LiveData<Boolean>
        get() = _loginEnabled

    init{
        _error.value = false
        _loginEnabled.value = true
        checkCompany()
    }

    private suspend fun loginCompany(loginName: String, password: String){
        viewModelScope.launch {
            try {
                loginCompany.loginCompany(loginName, password)
                val comp = loginRepository.getCompany()
                comp?.let {
                    _company.postValue(it)
                    _locations.postValue(it.locations)
                    _error.postValue(false)
                    saveLogin(loginName, password, it.id)
                }
                _showProgressBar.value = false
            }catch (e: Exception) {
                _loginEnabled.postValue(true)
                _error.postValue(true)
                _showProgressBar.value = false
            }
        }

    }


    @SuppressLint("CommitPrefEdits")
    private fun checkCompany(){
        viewModelScope.launch {
            _loginName.value = loginRepository.getStringSharedPreferences("loginName")
            _password.value = loginRepository.getStringSharedPreferences("password")
        }
    }

    fun getRestaurants(){
        _showProgressBar.value = true
        _loginEnabled.value = false
        viewModelScope.launch {
            _loginName.value?.let { name ->
                _password.value?.let { password ->
                    loginCompany(name, password)
                }
            }
            if(_loginName.value == null || _password.value == null){
                //TODO: Add error message
                _showProgressBar.value = false
                _loginEnabled.value = true
            }
        }
    }


    fun setRestaurant(l: Location){
        _restaurant.value = l
        viewModelScope.launch {
            loginRepository.setSharedPreferences("rid", l.id)
        }
    }

    private fun saveLogin(loginName: String, password: String, cid: String){
        viewModelScope.launch {
            loginRepository.setSharedPreferences("loginName", loginName)
            loginRepository.setSharedPreferences("password", password)
            loginRepository.setSharedPreferences("cid", cid)

        }
    }

    fun setLoginText(text: String){
        _loginName.value = text
    }

    fun setPasswordText(text: String){
        _password.value = text
    }

}