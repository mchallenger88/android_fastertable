package com.fastertable.fastertable.ui.ui.login.terminal

import android.app.Application
import androidx.lifecycle.*
import com.fastertable.fastertable.data.Location
import com.fastertable.fastertable.data.Settings
import com.fastertable.fastertable.data.Terminal
import com.fastertable.fastertable.data.repository.LoginRepository
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.File

class TerminalSelectViewModel(private val loginRepository: LoginRepository) : ViewModel() {
    private val _settings = MutableLiveData<Settings>()
    val settings: LiveData<Settings>
        get() = _settings

    private val _terminal = MutableLiveData<Terminal>()
    val terminal: LiveData<Terminal>
        get() = _terminal

    init{
        getSettings()
        checkTerminal()
    }

    @Suppress("UNNECESSARY_NOT_NULL_ASSERTION")
    private fun getSettings(){
        viewModelScope.launch {
            val set = loginRepository.getSettings()
            if (set != null){
                _settings.postValue(set!!)
            }
        }
    }

    @Suppress("UNNECESSARY_NOT_NULL_ASSERTION")
    private fun checkTerminal(){
        viewModelScope.launch {
            val term = loginRepository.getTerminal()
            if (term != null){
                _terminal.postValue(term!!)
            }
        }
    }

    fun setTerminal(terminal: Terminal){
        viewModelScope.launch {
            loginRepository.saveTerminal(terminal)
        }
    }
}