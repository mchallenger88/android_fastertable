package com.fastertable.fastertable.ui.ui.login.restaurant

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fastertable.fastertable.data.Location

class RestaurantLoginViewModelFactory (
        private val application: Application, private val restaurant: Location) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RestaurantLoginViewModel::class.java)) {
            return RestaurantLoginViewModel(application, restaurant) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}