package com.fastertable.fastertable.ui.login.restaurant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fastertable.fastertable.data.repository.LoginRepository

class RestaurantLoginViewModelFactory (
        private val loginRepository: LoginRepository
) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RestaurantLoginViewModel::class.java)) {
            return RestaurantLoginViewModel(loginRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}