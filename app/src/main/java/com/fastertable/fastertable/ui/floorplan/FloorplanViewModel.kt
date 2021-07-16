package com.fastertable.fastertable.ui.floorplan

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
class FloorplanViewModel @Inject constructor
    (private val loginRepository: LoginRepository,
     private val orderRepository: OrderRepository
    ) : BaseViewModel() {

    val user: OpsAuth = loginRepository.getOpsUser()!!
    val settings: Settings = loginRepository.getSettings()!!
    val terminal: Terminal = loginRepository.getTerminal()!!

    private val _tables = MutableLiveData<List<RestaurantTable>>()
    val tables: LiveData<List<RestaurantTable>>
        get() = _tables

    private val _orders = MutableLiveData<List<Order>>()
    val orders: LiveData<List<Order>>
        get() = _orders

    private val _navigateToOrder = MutableLiveData<String>()
    val navigateToOrder: LiveData<String>
        get() = _navigateToOrder


    init {
        viewModelScope.launch {
            loadTables()
            getOrders()
        }

    }

    private fun getOrders(){
        viewModelScope.launch {
            _orders.postValue(orderRepository.getOrdersFromFile())
        }

    }

    fun loadTables(){
        val table1 = RestaurantTable(
            id = 12,
            type = TableType.Round_Booth,
            rotate = 0,
            locked = false,
            reserved = false,
            active = false,
            id_location = IdLocation.TopCenter,
            maxSeats = 4,
            minSeats = 2,
            left = 500,
            top = 500
        )

        val table2 = RestaurantTable(
            id = 13,
            type = TableType.Booth,
            rotate = 0,
            locked = false,
            reserved = false,
            active = false,
            id_location = IdLocation.TopCenter,
            maxSeats = 4,
            minSeats = 2,
            left = 100,
            top = 100
        )

        val list = mutableListOf<RestaurantTable>()
        list.add(table1)
        list.add(table2)
        _tables.value = list
    }



    fun tableClicked(table: RestaurantTable){
        val openOrder = orders.value?.filter { it -> it.closeTime == null && it.tableNumber == table.id }
        if (openOrder != null) {
            if (openOrder.isNotEmpty()){

                _navigateToOrder.value = openOrder[0].id
            }else{
                startTableOrder(table)
            }
        }

    }

    fun startTableOrder(table: RestaurantTable){
        orderRepository.createNewOrder("Table", settings, user, table.id, null)
        _navigateToOrder.value = "Table"
    }
}