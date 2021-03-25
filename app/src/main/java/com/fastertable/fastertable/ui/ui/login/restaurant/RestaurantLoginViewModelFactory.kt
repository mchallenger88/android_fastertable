package com.fastertable.fastertable.ui.ui.login.restaurant

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fastertable.fastertable.data.Location
import com.fastertable.fastertable.data.repository.LoginRepository

class RestaurantLoginViewModelFactory (
        private val application: Application, private val restaurant: Location, private val loginRepository: LoginRepository
) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RestaurantLoginViewModel::class.java)) {
            return RestaurantLoginViewModel(application, restaurant, loginRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}