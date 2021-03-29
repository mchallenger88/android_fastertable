package com.fastertable.fastertable.ui.ui.home

import android.app.Application
import androidx.lifecycle.*
import com.fastertable.fastertable.data.Company
import com.fastertable.fastertable.data.Settings
import com.fastertable.fastertable.data.Terminal
import com.fastertable.fastertable.data.repository.LoginRepository
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File

class HomeViewModel(private val loginRepository: LoginRepository) : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is home Fragment"
    }
    val text: LiveData<String> = _text

    private val _company = MutableLiveData<Company>()
    val company: LiveData<Company>
        get() = _company

    private val _settings = MutableLiveData<Settings>()
    val settings: LiveData<Settings>
        get() = _settings

    private val _terminal = MutableLiveData<Terminal>()
    val terminal: LiveData<Terminal>
        get() = _terminal

    init{
        viewModelScope.launch {
            getCompany()
            getSettings()
            getTerminal()
        }

    }

    private suspend fun getCompany(){
        withContext(IO){
            _company.postValue(loginRepository.getCompany())
        }
    }

    private suspend fun getSettings(){
        withContext(IO){
            _settings.postValue(loginRepository.getSettings())
        }
    }

    private suspend fun getTerminal(){
        withContext(IO){
            _terminal.postValue(loginRepository.getTerminal())
        }
    }
}