package com.fastertable.fastertable.ui.ui.login.user

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fastertable.fastertable.data.Location

class UserLoginViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserLoginViewModel::class.java)) {
            return UserLoginViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}