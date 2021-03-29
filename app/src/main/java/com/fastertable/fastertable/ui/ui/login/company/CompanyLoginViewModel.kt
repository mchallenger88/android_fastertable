package com.fastertable.fastertable.ui.ui.login.company

import android.annotation.SuppressLint
import androidx.databinding.ObservableField
import androidx.lifecycle.*
import com.fastertable.fastertable.data.Company
import com.fastertable.fastertable.data.Location
import com.fastertable.fastertable.data.repository.LoginRepository
import com.fastertable.fastertable.utils.ApiStatus
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class CompanyLoginViewModel(private val loginRepository: LoginRepository) : ViewModel() {
    private val _company = MutableLiveData<Company>()
    val company: LiveData<Company>
        get() = _company

    val loginName = ObservableField<String>()
    val password = ObservableField<String>()

    private val _locations = MutableLiveData<List<Location>>()
    val locations: LiveData<List<Location>>
        get() = _locations

    private val _restaurant = MutableLiveData<Location>()
    val restaurant: LiveData<Location>
        get() = _restaurant

    private val _showProgressBar = MutableLiveData<Boolean>()
    val showProgressBar: LiveData<Boolean>
        get() = _showProgressBar

    private val _error = MutableLiveData<Boolean>()
    val error: LiveData<Boolean>
        get() = _error

    private val _status = MutableLiveData<ApiStatus>()
    val status: LiveData<ApiStatus>
        get() = _status

    init{
        _showProgressBar.value = false
        _error.value = false
        checkCompany()
    }

    private suspend fun loginCompany(loginName: String, password: String){
        withContext(IO){
            _status.postValue(ApiStatus.LOADING)
            try {
                val comp = loginRepository.loginCompany(loginName, password)
                _company.postValue(comp)
                _locations.postValue(comp.locations)
                _status.postValue(ApiStatus.SUCCESS)
                _error.postValue(false)
                saveLogin(loginName, password, comp.id)
            }catch (e: Exception) {
                _status.postValue(ApiStatus.ERROR)
                _error.postValue(true)
            }
        }
    }


    @SuppressLint("CommitPrefEdits")
    private fun checkCompany(){
        viewModelScope.launch {
            val sp =
            loginName.set(loginRepository.getStringSharedPreferences("loginName"))
            password.set(loginRepository.getStringSharedPreferences("password"))
        }
    }

    fun getRestaurants(){
        _showProgressBar.value = true
        viewModelScope.launch {
            loginCompany(loginName.get()!!, password.get()!!)

            _showProgressBar.value = false
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

}