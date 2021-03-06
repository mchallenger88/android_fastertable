package com.fastertable.fastertable.ui.home

import androidx.lifecycle.*
import com.fastertable.fastertable.data.models.*
import com.fastertable.fastertable.data.repository.LoginRepository
import com.fastertable.fastertable.data.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor (private val loginRepository: LoginRepository,
                                         private val orderRepository: OrderRepository) : ViewModel() {

    val user: OpsAuth = loginRepository.getOpsUser()!!
    val settings: Settings = loginRepository.getSettings()!!
    val terminal: Terminal = loginRepository.getTerminal()!!

    private val _showProgressBar = MutableLiveData<Boolean>()
    val showProgressBar: LiveData<Boolean>
        get() = _showProgressBar

    private val _company = MutableLiveData<Company>()
    val company: LiveData<Company>
        get() = _company

    private val _orders = MutableLiveData<List<Order>>()
    val orders: LiveData<List<Order>>
        get() = _orders

    private val _filteredOrders = MutableLiveData<List<Order>>()
    val filteredOrders: LiveData<List<Order>>
        get() = _filteredOrders

    private val _orderFilter = MutableLiveData<String>()
    val orderFilter: LiveData<String>
        get() = _orderFilter

    private val _navigateToOrder = MutableLiveData<String>()
    val navigateToOrder: LiveData<String>
        get() = _navigateToOrder

    private val _navigateToFloorplan = MutableLiveData<Boolean>()
    val navigateToFloorplan: LiveData<Boolean>
        get() = _navigateToFloorplan

    private val _navigateToTakeout = MutableLiveData<Boolean>()
    val navigateToTakeout: LiveData<Boolean>
        get() = _navigateToTakeout

    private val _viewLoaded = MutableLiveData<Boolean>()
    val viewLoaded: LiveData<Boolean>
        get() = _viewLoaded

    init{
        _showProgressBar.value = false
        _viewLoaded.value = false

        viewModelScope.launch {
            getOrders()
        }

    }

    private fun getOrders(){
        viewModelScope.launch {
            _orders.postValue(orderRepository.getOrdersFromFile())
            _viewLoaded.postValue(true)
        }

    }

    fun getCompany(){
        viewModelScope.launch{
            _company.postValue(loginRepository.getCompany())
        }
    }

    fun filterOrders(filter: String){
        when (filter) {
            "Open" -> _filteredOrders.value = orders.value?.filter { it -> it.closeTime == null }
            "Closed" -> _filteredOrders.value = orders.value?.filter { it -> it.closeTime != null }
            "All" -> _filteredOrders.value = orders.value
        }
    }

    fun setNavigateToFloorPlan(b: Boolean){
        _navigateToFloorplan.value = b
    }

    fun setNavigateToTakeout(b: Boolean){
        _navigateToTakeout.value = b
    }

    fun onOrderClicked(id: String) {
        _navigateToOrder.value = id
    }

    fun onOpenClicked(){
        _orderFilter.value = "Open"
        filterOrders(orderFilter.value!!)
    }

    fun onAllClicked(){
        _orderFilter.value = "All"
        filterOrders(orderFilter.value!!)
    }

    fun onClosedClicked(){
        _orderFilter.value = "Closed"
        filterOrders(orderFilter.value!!)
    }

    fun startCounterOrder(){
        orderRepository.createNewOrder("Counter", settings, user, null,  null)
        _navigateToOrder.value = "Counter"
    }

    fun navigationEnd(){
        _navigateToOrder.value = ""
    }


}