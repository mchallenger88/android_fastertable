package com.fastertable.fastertable.ui.ui.login.restaurant

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fastertable.fastertable.data.Location
import com.fastertable.fastertable.data.Settings
import com.fastertable.fastertable.data.Terminal
import com.fastertable.fastertable.data.repository.LoginRepository
import com.fastertable.fastertable.utils.ApiStatus
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File

class RestaurantLoginViewModel(application: Application,
                private val restaurant: Location,
                private val loginRepository: LoginRepository) : AndroidViewModel(application) {

    val app: Application = application
    private val _pin = MutableLiveData<String>()
    val pin: LiveData<String>
        get() = _pin

    private val _settings = MutableLiveData<Settings>()
    val settings: LiveData<Settings>
        get() = _settings

    private val _navigateToTerminals = MutableLiveData<Boolean>()
    val navigateToTerminals: LiveData<Boolean>
        get() = _navigateToTerminals

    private val _navigateToUserLogin = MutableLiveData<Boolean>()
    val navigateToUserLogin: LiveData<Boolean>
        get() = _navigateToUserLogin

    private val _error = MutableLiveData<Boolean>()
    val error: LiveData<Boolean>
        get() = _error

    private val _status = MutableLiveData<ApiStatus>()
    val status: LiveData<ApiStatus>
        get() = _status

    private val _showProgressBar = MutableLiveData<Boolean>()
    val showProgressBar: LiveData<Boolean>
        get() = _showProgressBar

    init{
        _error.value = false
        _showProgressBar.value = false
        checkPin()
        _navigateToTerminals.value = false
        _navigateToUserLogin.value = false
    }



    fun concatPin(num: Int){
        _pin.value = pin.value + num.toString()
    }

    fun pinClear(){
        checkPin()
    }

    fun restLogin(){
        if (restaurant.loginPin == pin.value?.toInt()){
            setPin(restaurant.loginPin.toString())
            viewModelScope.launch {
                saveRestaurantData()
            }
        }else{
            _error.value = true
        }
    }

    private fun checkPin(){
        val sp: SharedPreferences = app.getSharedPreferences("restaurant", Context.MODE_PRIVATE)
        if (sp.contains("pin")){
            _pin.value = sp.getString("pin", "")
        }else{
            _pin.value = ""
        }
    }

    private fun setPin(pin: String){
        val sp: SharedPreferences = app.getSharedPreferences("restaurant", Context.MODE_PRIVATE)
        //initializing editor
        val editor = sp.edit()
        editor.putString("pin", pin)
        editor.apply()
    }

    private suspend fun saveRestaurantData(){
        withContext(Dispatchers.IO) {
            _showProgressBar.postValue(true)
            _status.postValue(ApiStatus.LOADING)
            try {
                val settings: Settings = loginRepository.getRestaurantSettings(restaurant.id)
                _settings.postValue(settings)
                saveTaxRate(settings)
                loginRepository.saveMenus(restaurant.id)
                checkTerminal(app)
                _showProgressBar.postValue(false)
            }
            catch (e: Exception) {
                _status.postValue(ApiStatus.ERROR)
            }
        }
    }


    private fun saveTaxRate(settings: Settings){
        val sp: SharedPreferences = app.getSharedPreferences("restaurant", Context.MODE_PRIVATE)
        //initializing editor
        val editor = sp.edit()
        editor.putString("taxrate", settings.taxRate.rate.toString())
        editor.apply()
    }

    private fun checkTerminal(app: Application){
        val gson = Gson()
        if (File(app.filesDir, "terminal.json").exists()){
            val bufferedReader: BufferedReader = File(app.filesDir, "terminal.json").bufferedReader()
            val inputString = bufferedReader.use { it.readText() }
            var terminal = gson.fromJson(inputString, Terminal::class.java)
            _navigateToUserLogin.postValue(true)

        }else{
            _navigateToTerminals.postValue(true)
        }

    }
}