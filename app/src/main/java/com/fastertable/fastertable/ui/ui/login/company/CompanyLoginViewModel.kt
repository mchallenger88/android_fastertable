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


class CompanyLoginViewModel(application: Application, loginRepository: LoginRepository) : AndroidViewModel(application) {
    private val app: Application = application
    private val loginRepository = loginRepository
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

    private suspend fun testLoginCompany(loginName: String?, password: String?){
        withContext(IO){
            _company.postValue(loginRepository.loginCompany(loginName, password))
        }
    }

    @SuppressLint("CommitPrefEdits")
    private fun loginCompany(loginName: String?, password: String?){
        viewModelScope.launch{
            //Get company
            _status.value = ApiStatus.LOADING
            try {
                val company: Company = CompanyService.Companion.ApiService.retrofitService.getCompany(loginName, password)
                _status.value = ApiStatus.SUCCESS
                _error.value = false
                //getting shared preferences
                val sp: SharedPreferences = app.getSharedPreferences("restaurant", MODE_PRIVATE)
                //initializing editor
                val editor = sp.edit()
                editor.putString("loginName", loginName);
                editor.putString("password", password);
                editor.putString("cid", company.id)
                editor.apply();

                //Save company json to file
                saveCompany(app, company)

                //Get Restaurant Locations
                _locations.value = company.locations


            } catch (e: Exception) {
                _status.value = ApiStatus.ERROR
                _error.value = true
            }

            _showProgressBar.value = false
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
        loginCompany(loginName.get(), password.get())
//        viewModelScope.launch {
//            testLoginCompany(loginName.get(), password.get())
//            _showProgressBar.value = false
//        }
    }

    private fun saveCompany(app: Application, company: Company){
        val gson = Gson()
        val jsonString = gson.toJson(company)
        val file= File(app.filesDir, "company.json")
        file.writeText(jsonString)
    }

    fun setRestaurant(l: Location){
        _restaurant.value = l
        val sp: SharedPreferences = app.getSharedPreferences("restaurant", MODE_PRIVATE)
        //initializing editor
        val editor = sp.edit()
        editor.putString("lid", l.id)
        editor.apply()
    }

}