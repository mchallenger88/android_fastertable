package com.fastertable.fastertable.ui.order

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fastertable.fastertable.data.*
import com.fastertable.fastertable.data.repository.LoginRepository
import com.fastertable.fastertable.data.repository.OrderRepository
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OrderViewModel(private val loginRepository: LoginRepository, private val orderRepository: OrderRepository) : ViewModel() {

    private val _order = MutableLiveData<Order>()
    val order: LiveData<Order>
        get() = _order

    private val _guestAdd = MutableLiveData<Boolean>()
    val guestAdd: LiveData<Boolean>
        get() = _guestAdd

    private val _activeGuest = MutableLiveData<Int>()
    val activeGuest: LiveData<Int>
        get() = _activeGuest

    private val _orderItem = MutableLiveData<OrderItem>()
    val orderItem: LiveData<OrderItem>
        get() = _orderItem

    private val orderItemMods = ArrayList<OrderMod>()

    init{
        viewModelScope.launch {
            getOrder()
            _guestAdd.postValue(true)
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
        _guestAdd.value = true
    }

    fun setGuestAdd(b: Boolean){
        _guestAdd.value = b
    }

    fun onModItemClicked(item: OrderMod){
        //Find out if the selection must be deleted before being added
        val found = orderItemMods.find { x -> x.mod.arrayId == item.mod.arrayId }
        if (found != null){
          orderItemMods.remove(found)
          orderItemMods.add(item)
        }else{
            orderItemMods.add(item)
        }
        println(orderItemMods.size)
    }

    fun onIngredientClicked(item:IngredientList){

    }


}