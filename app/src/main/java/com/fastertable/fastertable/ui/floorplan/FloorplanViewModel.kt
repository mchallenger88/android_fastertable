package com.fastertable.fastertable.ui.floorplan

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fastertable.fastertable.common.base.BaseViewModel
import com.fastertable.fastertable.data.models.*
import com.fastertable.fastertable.data.repository.FloorplanQueries
import com.fastertable.fastertable.data.repository.LoginRepository
import com.fastertable.fastertable.data.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FloorplanViewModel @Inject constructor
    (private val loginRepository: LoginRepository,
     private val orderRepository: OrderRepository,
     private val floorplanQueries: FloorplanQueries
    ) : BaseViewModel() {

    val user: OpsAuth = loginRepository.getOpsUser()!!
    val settings: Settings = loginRepository.getSettings()!!
    val terminal: Terminal = loginRepository.getTerminal()!!

    private val _tables = MutableLiveData<List<RestaurantTable>>()
    val tables: LiveData<List<RestaurantTable>>
        get() = _tables

    private val _walls = MutableLiveData<List<FloorplanWall>>()
    val walls: LiveData<List<FloorplanWall>>
        get() = _walls

    private val _floorplans = MutableLiveData<List<RestaurantFloorplan>?>()
    val floorplans: LiveData<List<RestaurantFloorplan>?>
        get() = _floorplans

    private val _orders = MutableLiveData<List<Order>?>()
    val orders: LiveData<List<Order>?>
        get() = _orders

    var prelimOrders: List<Order>? = null
    var prelimFloorplan: List<RestaurantFloorplan>? = null

    private val _navigateToOrder = MutableLiveData<String>()
    val navigateToOrder: LiveData<String>
        get() = _navigateToOrder


    init {

    }

    fun getFloorplans(){
        viewModelScope.launch {
            reallyGetFloorplans()
        }
    }

    suspend fun reallyGetFloorplans(){
        val job = viewModelScope.launch {
            prelimFloorplan = floorplanQueries.getFloorplans(settings.locationId, settings.companyId)
            if (prelimFloorplan!!.isNotEmpty()){
                _floorplans.postValue(prelimFloorplan)
            }
        }
        job.join()
        getOrders()
    }

    private suspend fun getOrders(){
        val job = viewModelScope.launch {
            prelimOrders = orderRepository.getOrdersFromFile()
        }
        job.join()
        _orders.value = prelimOrders
        findTableOrders()
    }

    private fun findTableOrders(){
        viewModelScope.launch {
            val orderTables = prelimOrders?.filter { it.tableNumber != null && it.closeTime == null }
            if (orderTables != null && prelimFloorplan != null ){
                for (floorplan in prelimFloorplan!!){
                    for (table in floorplan.tables){
                        for (order in orderTables){
                            if (table.id == order.tableNumber){
                                table.locked = true
                                val manager = user.claims.find { it.permission.name == "viewOrders" }
                                if (order.employeeId == user.employeeId || manager?.permission?.value!!){
                                    table.locked = false
                                    table.active = true
                                }
                            }
                        }
                    }
                }
                _floorplans.value = prelimFloorplan
            }
        }
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

    private fun startTableOrder(table: RestaurantTable){
        orderRepository.createNewOrder("Table", settings, user, table.id, null)
        _navigateToOrder.value = "Table"
    }
}