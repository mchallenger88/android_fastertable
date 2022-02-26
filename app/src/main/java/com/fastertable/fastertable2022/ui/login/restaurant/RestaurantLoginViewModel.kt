package com.fastertable.fastertable2022.ui.login.restaurant

import androidx.lifecycle.*
import com.fastertable.fastertable2022.data.models.Company
import com.fastertable.fastertable2022.data.models.Location
import com.fastertable.fastertable2022.data.models.Settings
import com.fastertable.fastertable2022.data.repository.GetMenus
import com.fastertable.fastertable2022.data.repository.GetRestaurantSettings
import com.fastertable.fastertable2022.data.repository.LoginRepository
import com.fastertable.fastertable2022.utils.ApiStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class RestaurantLoginViewModel @Inject constructor(
    private val getRestaurantSettings: GetRestaurantSettings,
    private val getMenus: GetMenus,
    private val loginRepository: LoginRepository) : ViewModel() {

    private val _pin = MutableLiveData<String>()
    val pin: LiveData<String>
        get() = _pin

    private val _settings = MutableLiveData<Settings>()
    val settings: LiveData<Settings>
        get() = _settings

    private val _restaurant = MutableLiveData<Location>()
    val restaurant: LiveData<Location>
        get() = _restaurant

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

    private val _enterEnabled = MutableLiveData(true)
    val enterEnabled: LiveData<Boolean>
        get() = _enterEnabled

    init{
        _error.value = false
        _showProgressBar.value = false
        getRestaurant()
        checkPin()
        _navigateToTerminals.value = false
        _navigateToUserLogin.value = false
    }


    private fun getRestaurant(){
        viewModelScope.launch {
            val rid: String? = loginRepository.getStringSharedPreferences("rid")
            val company: Company? = loginRepository.getCompany()
            if (rid != null && company != null){
                _restaurant.postValue(company.getLocation(rid))
            }

        }
    }

    fun concatPin(num: Int){
        _pin.value = pin.value + num.toString()
    }

    fun pinClear(){
        _pin.value = ""
    }

    fun restLogin(){
        _enterEnabled.value = false
        if (restaurant.value?.loginPin == pin.value?.toInt()){
            setPin(restaurant.value?.loginPin.toString())
            viewModelScope.launch {
                saveRestaurantData()
            }
        }else{
            _error.value = true
            _enterEnabled.value = true
        }
    }

    private fun checkPin(){
        viewModelScope.launch {
            if (loginRepository.getStringSharedPreferences("pin") != null){
                _pin.postValue(loginRepository.getStringSharedPreferences("pin"))
            }else{
                _pin.postValue("")
            }
        }
    }

    private fun setPin(pin: String){
        viewModelScope.launch{
            loginRepository.setSharedPreferences("pin", pin)
        }
    }

    private suspend fun saveRestaurantData(){
        withContext(Dispatchers.IO) {
            _showProgressBar.postValue(true)
            _status.postValue(ApiStatus.LOADING)
            try {
                _restaurant.value?.let { restaurant ->
                    val settings: Settings = getRestaurantSettings.getRestaurantSettings(restaurant.id)
                    _settings.postValue(settings)

                    var terminal = loginRepository.getTerminal()
                    if (terminal != null){
                        settings.terminals.find{ it.terminalId == terminal?.terminalId}
                            ?.let { loginRepository.saveTerminal(it) }
                        terminal = loginRepository.getTerminal()
                    }else{
                        _navigateToTerminals.postValue(true)
                    }
                    saveTaxRate(settings)

                    getMenus.getAllMenus(restaurant.id)
                    checkTerminal()
                    _enterEnabled.value = true
                    _showProgressBar.postValue(false)
                }

            }
            catch (e: Exception) {
                _status.postValue(ApiStatus.ERROR)
            }
        }
    }


    private fun saveTaxRate(settings: Settings) {
        viewModelScope.launch {
            loginRepository.setSharedPreferences("taxrate", settings.taxRate.rate.toString())

        }
    }

        private fun checkTerminal() {
            viewModelScope.launch {
                if (loginRepository.getTerminal() != null){
                    _navigateToUserLogin.postValue(true)
                }else{
                    _navigateToTerminals.postValue(true)
                }
            }

        }

}