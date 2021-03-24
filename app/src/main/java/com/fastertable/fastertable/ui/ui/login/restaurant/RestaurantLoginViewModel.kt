package com.fastertable.fastertable.ui.ui.login.restaurant

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fastertable.fastertable.api.MenuService
import com.fastertable.fastertable.api.SettingsService
import com.fastertable.fastertable.data.Location
import com.fastertable.fastertable.data.Menu
import com.fastertable.fastertable.data.Settings
import com.fastertable.fastertable.data.Terminal
import com.fastertable.fastertable.utils.ApiStatus
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.File

class RestaurantLoginViewModel(application: Application, restaurant: Location) : AndroidViewModel(application) {
    val app: Application = application
    val restaurant: Location = restaurant
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
            saveRestaurantData(app)
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
        editor.putString("pin", pin);
        editor.apply();
    }

    private fun saveRestaurantData(app: Application){
        viewModelScope.launch {
            _showProgressBar.value = true
            _status.value = ApiStatus.LOADING
            try {
                // load settings
                val settings: Settings = SettingsService.Companion.ApiService.retrofitService.getLocationSettings(restaurant.id)
                saveSettings(app, settings)
                // set tax rate
                saveTaxRate(settings)
                // load menus
                val menus: List<Menu> = MenuService.Companion.ApiService.retrofitService.getMenus(restaurant.id)
                saveMenus(app, menus)
                // set terminals
                checkTerminal(app)
                _showProgressBar.value = false
            }
            catch (e: Exception) {
                _status.value = ApiStatus.ERROR
            }

        }

    }

    fun saveSettings(app: Application, settings: Settings){
        _settings.value = settings
        val gson = Gson()
        val jsonString = gson.toJson(settings)
        val file= File(app.filesDir, "settings.json")
        file.writeText(jsonString)
    }

    fun saveMenus(app: Application, menus: List<Menu>){
        val gson = Gson()
        val jsonString = gson.toJson(menus)
        val file= File(app.filesDir, "menus.json")
        file.writeText(jsonString)
    }

    fun saveTaxRate(settings: Settings){
        val sp: SharedPreferences = app.getSharedPreferences("restaurant", Context.MODE_PRIVATE)
        //initializing editor
        val editor = sp.edit()
        editor.putString("taxrate", settings.taxRate.rate.toString());
        editor.apply();
    }

    fun checkTerminal(app: Application){
        var gson = Gson()
        if (File(app.filesDir, "terminal.json").exists()){
            val bufferedReader: BufferedReader = File(app.filesDir, "terminal.json").bufferedReader()
            val inputString = bufferedReader.use { it.readText() }
            var terminal = gson.fromJson(inputString, Terminal::class.java)
            _navigateToUserLogin.value = true

        }else{
            _navigateToTerminals.value = true
        }

    }
}