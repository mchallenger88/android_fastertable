package com.fastertable.fastertable.ui.login.restaurant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fastertable.fastertable.data.repository.LoginRepository
import com.fastertable.fastertable.data.repository.MenusRepository

class RestaurantLoginViewModelFactory (
        private val loginRepository: LoginRepository, private val menusRepository: MenusRepository
) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RestaurantLoginViewModel::class.java)) {
            return RestaurantLoginViewModel(loginRepository, menusRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}