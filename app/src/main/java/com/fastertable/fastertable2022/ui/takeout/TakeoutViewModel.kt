package com.fastertable.fastertable2022.ui.takeout

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fastertable.fastertable2022.common.base.BaseViewModel
import com.fastertable.fastertable2022.data.models.*
import com.fastertable.fastertable2022.data.repository.LoginRepository
import com.fastertable.fastertable2022.data.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TakeoutViewModel @Inject constructor
    (private val loginRepository: LoginRepository,
     private val orderRepository: OrderRepository
): BaseViewModel(){

    val user: OpsAuth? = loginRepository.getOpsUser()
    val settings: Settings? = loginRepository.getSettings()

    private val _orders = MutableLiveData<List<Order>>()
    val orders: LiveData<List<Order>>
        get() = _orders

    private val _phoneNumber = MutableLiveData("")
    val phoneNumber: LiveData<String>
        get() = _phoneNumber

    private val _navigateToOrder = MutableLiveData<String>()
    val navigateToOrder: LiveData<String>
        get() = _navigateToOrder

    init {
        viewModelScope.launch {
            getOrders()
        }
    }

    fun setPhoneNumber(text: String){
        _phoneNumber.value = text
    }

    private fun getOrders(){
        viewModelScope.launch {
            _orders.postValue(orderRepository.getOrdersFromFile())
        }
    }

    fun startTakeoutOrder(takeOutCustomer: TakeOutCustomer){
        settings?.let { s ->
            user?.let { u ->
                orderRepository.createNewOrder("Takeout", s, u, null, takeOutCustomer)
                _navigateToOrder.value = "Takeout"
            }
        }

    }

}