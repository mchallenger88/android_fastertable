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
import okhttp3.internal.notifyAll

class OrderViewModel(private val loginRepository: LoginRepository, private val orderRepository: OrderRepository) : ViewModel() {

    private val _order = MutableLiveData<Order>()
    val order: LiveData<Order>
        get() = _order

    private val _guessAdd = MutableLiveData<Boolean>()
    val guestAdd: LiveData<Boolean>
        get() = _guessAdd

    private val _activeGuest = MutableLiveData<Int>()
    val activeGuest: LiveData<Int>
        get() = _activeGuest

    init{
        viewModelScope.launch {
            getOrder()
            _guessAdd.postValue(true)
            _activeGuest.postValue(1)
        }
    }

    private suspend fun getOrder(){
        withContext(IO){
            _order.postValue(orderRepository.getNewOrder())
        }
    }

    fun setActiveGuest(int: Int){
        _activeGuest.value = int
    }

    fun addGuest(){
        val ord = _order.value
        ord?.guestAdd()
        _order.value = ord!!
        _guessAdd.value = true
    }

    fun setGuestAdd(b: Boolean){
        _guessAdd.value = b
    }
}