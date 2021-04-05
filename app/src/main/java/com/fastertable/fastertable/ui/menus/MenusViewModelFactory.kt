package com.fastertable.fastertable.ui.menus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fastertable.fastertable.data.repository.LoginRepository
import com.fastertable.fastertable.data.repository.MenusRepository
import com.fastertable.fastertable.data.repository.OrderRepository

class MenusViewModelFactory (private val menusRepository: MenusRepository, private val orderRepository: OrderRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MenusViewModel::class.java)) {

            return MenusViewModel(menusRepository, orderRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}