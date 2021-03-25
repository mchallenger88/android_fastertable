package com.fastertable.fastertable.ui.ui.login.company

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.databinding.ObservableField
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fastertable.fastertable.api.CompanyService
import com.fastertable.fastertable.data.Company
import com.fastertable.fastertable.data.Location
import com.fastertable.fastertable.data.repository.LoginRepository
import com.fastertable.fastertable.utils.ApiStatus
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


class CompanyLoginViewModel(application: Application, private val loginRepository: LoginRepository) : AndroidViewModel(application) {
    private val app: Application = application
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

    private suspend fun loginCompany(loginName: String?, password: String?){
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
        //getting shared preferences
        val sp: SharedPreferences = app.getSharedPreferences("restaurant", MODE_PRIVATE)
        //initializing editor
        loginName.set(sp.getString("loginName", ""))
        password.set(sp.getString("password", ""))
    }

    fun getRestaurants(){
        _showProgressBar.value = true
        viewModelScope.launch {
            loginCompany(loginName.get(), password.get())

            _showProgressBar.value = false
        }
    }


    fun setRestaurant(l: Location){
        _restaurant.value = l
        val sp: SharedPreferences = app.getSharedPreferences("restaurant", MODE_PRIVATE)
        //initializing editor
        val editor = sp.edit()
        editor.putString("lid", l.id)
        editor.apply()
    }

    private fun saveLogin(loginName: String?, password: String?, cid: String){
        val sp: SharedPreferences = app.getSharedPreferences("restaurant", MODE_PRIVATE)
        //initializing editor
        val editor = sp.edit()
        editor.putString("loginName", loginName)
        editor.putString("password", password)
        editor.putString("cid", cid)
        editor.apply()
    }

}