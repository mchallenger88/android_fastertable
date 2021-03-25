package com.fastertable.fastertable.ui.ui.login.terminal

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fastertable.fastertable.data.Location
import com.fastertable.fastertable.data.Settings
import com.fastertable.fastertable.data.Terminal
import com.google.gson.Gson
import java.io.BufferedReader
import java.io.File

class TerminalSelectViewModel(application: Application, settings: Settings) : AndroidViewModel(application) {
    private val app: Application = application
    private val _settings = MutableLiveData<Settings>()
    val settings: LiveData<Settings>
        get() = _settings

    private val _terminal = MutableLiveData<Terminal>()
    val terminal: LiveData<Terminal>
        get() = _terminal

    init{
        _settings.value = settings
        checkTerminal()
    }

    private fun checkTerminal(){
        var gson = Gson()
        if (File(app.filesDir, "terminal.json").exists()){
            val bufferedReader: BufferedReader = File(app.filesDir, "terminal.json").bufferedReader()
            val inputString = bufferedReader.use { it.readText() }
            _terminal.value = gson.fromJson(inputString, Terminal::class.java)
        }
    }

    fun setTerminal(terminal: Terminal){
        val gson = Gson()
        val jsonString = gson.toJson(terminal)
        val file= File(app.filesDir, "terminal.json")
        file.writeText(jsonString)
        _terminal.value = terminal
    }
}