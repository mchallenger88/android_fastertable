package com.fastertable.fastertable.ui.ui.home

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fastertable.fastertable.data.repository.LoginRepository
import com.fastertable.fastertable.ui.ui.login.company.CompanyLoginViewModel

class HomeViewModelFactory (
    private val application: Application, private val loginRepository: LoginRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(application, loginRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}