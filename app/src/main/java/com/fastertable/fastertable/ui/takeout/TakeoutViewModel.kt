package com.fastertable.fastertable.ui.takeout

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fastertable.fastertable.common.base.BaseViewModel
import com.fastertable.fastertable.data.models.*
import com.fastertable.fastertable.data.repository.LoginRepository
import com.fastertable.fastertable.data.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TakeoutViewModel @Inject constructor
    (private val loginRepository: LoginRepository,
     private val orderRepository: OrderRepository
): BaseViewModel(){

    val user: OpsAuth = loginRepository.getOpsUser()!!
    val settings: Settings = loginRepository.getSettings()!!
    val terminal: Terminal = loginRepository.getTerminal()!!

    private val _orders = MutableLiveData<List<Order>>()
    val orders: LiveData<List<Order>>
        get() = _orders

    private val _navigateToOrder = MutableLiveData<String>()
    val navigateToOrder: LiveData<String>
        get() = _navigateToOrder

    init {
        viewModelScope.launch {
            getOrders()
        }
    }

    private fun getOrders(){
        viewModelScope.launch {
            _orders.postValue(orderRepository.getOrdersFromFile())
        }
    }

    fun startTakeoutOrder(takeOutCustomer: TakeOutCustomer){
        orderRepository.createNewOrder("Takeout", settings, user, null, takeOutCustomer)
        _navigateToOrder.value = "Takeout"
    }

}