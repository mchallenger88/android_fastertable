package com.fastertable.fastertable.ui.ui.login.terminal

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fastertable.fastertable.data.Location
import com.fastertable.fastertable.data.Settings

class TerminalSelectViewModelFactory (private val application: Application, private val settings: Settings) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TerminalSelectViewModel::class.java)) {
            return TerminalSelectViewModel(application, settings) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}