package com.fastertable.fastertable.ui.ui.login.terminal

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fastertable.fastertable.data.Location
import com.fastertable.fastertable.data.Settings
import com.fastertable.fastertable.data.repository.LoginRepository

class TerminalSelectViewModelFactory (private val loginRepository: LoginRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TerminalSelectViewModel::class.java)) {
            return TerminalSelectViewModel(loginRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}