package com.fastertable.fastertable.ui.login.terminal

import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import com.fastertable.fastertable.data.models.Settings
import com.fastertable.fastertable.data.models.Terminal
import com.fastertable.fastertable.data.repository.GetRestaurantSettings
import com.fastertable.fastertable.data.repository.LoginRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TerminalSelectViewModel @Inject constructor(
    private val getRestaurantSettings: GetRestaurantSettings,
    private val loginRepository: LoginRepository) : ViewModel() {
    private val _settings = MutableLiveData<Settings?>()
    val settings: LiveData<Settings?>
        get() = _settings

    private val _terminal = MutableLiveData<Terminal?>()
    val terminal: LiveData<Terminal?>
        get() = _terminal

    private val _onPage = MutableLiveData<Boolean>()
    val onPage: LiveData<Boolean>
        get() = _onPage

    private val _statusText = MutableLiveData("")
    val statusText: LiveData<String>
        get() = _statusText

    private val _addressText = MutableLiveData("")
    val addressText: LiveData<String>
        get() = _addressText

    private val _bluetoothEnabled = MutableLiveData(true)
    val bluetoothEnabled: LiveData<Boolean>
        get() = _bluetoothEnabled

    private val _discover = MutableLiveData(false)
    val discover: LiveData<Boolean>
        get() = _discover

    init{
        setOnPage(false)
        getSettings()
        checkTerminal()
    }

    fun getSettings(){
        viewModelScope.launch {
            val set = loginRepository.getSettings()
            if (set != null){
                _settings.postValue(set)
            }
        }
    }


    private fun checkTerminal(){
        viewModelScope.launch {
            val term = loginRepository.getTerminal()
            if (term != null){
                _terminal.postValue(term)
            }else{

            }
        }
    }

    private fun setOnPage(b: Boolean){
        _onPage.value = b
    }

    fun setTerminal(terminal: Terminal){
        viewModelScope.launch {
            setOnPage(true)
            _terminal.postValue(terminal)
            loginRepository.saveTerminal(terminal)
        }
    }

    fun updateStatus(str: String) {
        _statusText.value = str
    }

    fun updateAddress(str: String) {
        _addressText.value = str
    }

    fun setButtonEnabled(b: Boolean){
        _bluetoothEnabled.value = b
    }

    fun setDiscover(b: Boolean){
        _discover.value = b
    }

}