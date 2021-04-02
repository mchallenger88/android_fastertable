package com.fastertable.fastertable.ui.order

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fastertable.fastertable.data.Order
import com.fastertable.fastertable.data.repository.LoginRepository
import com.fastertable.fastertable.data.repository.OrderRepository
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OrderViewModel(private val loginRepository: LoginRepository, private val orderRepository: OrderRepository) : ViewModel() {

    private val _order = MutableLiveData<Order>()
    val order: LiveData<Order>
        get() = _order

    init{
        viewModelScope.launch {
            getOrder()
        }
    }

    private suspend fun getOrder(){
        withContext(IO){
            _order.postValue(orderRepository.getNewOrder())
        }
    }
}