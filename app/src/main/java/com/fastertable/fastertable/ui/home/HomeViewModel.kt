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

    private val _showProgressBar = MutableLiveData<Boolean>()
    val showProgressBar: LiveData<Boolean>
        get() = _showProgressBar

    private val _company = MutableLiveData<Company>()
    val company: LiveData<Company>
        get() = _company

    private val _settings = MutableLiveData<Settings>()
    val settings: LiveData<Settings>
        get() = _settings

    private val _terminal = MutableLiveData<Terminal>()
    val terminal: LiveData<Terminal>
        get() = _terminal

    private val _user = MutableLiveData<OpsAuth>()
    val user: LiveData<OpsAuth>
        get() = _user

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

    private val _viewLoaded = MutableLiveData<Boolean>()
    val viewLoaded: LiveData<Boolean>
        get() = _viewLoaded

    init{
        _showProgressBar.value = false
        _viewLoaded.value = false

        viewModelScope.launch {
            getOrders()
            getSettings()
            getUser()
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

    private fun getSettings(){
        viewModelScope.launch{
            _settings.postValue(loginRepository.getSettings())
        }
    }

    private fun getUser(){
        viewModelScope.launch{
            _user.postValue(loginRepository.getOpsUser())
        }
    }

    private fun getTerminal(){
        viewModelScope.launch{
            _terminal.postValue(loginRepository.getTerminal())
        }
    }

    fun filterOrders(filter: String){
        when (filter) {
            "Open" -> _filteredOrders.value = orders.value?.filter { it -> it.closeTime == null }
            "Closed" -> _filteredOrders.value = orders.value?.filter { it -> it.closeTime != null }
            "All" -> _filteredOrders.value = orders.value
        }
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
        orderRepository.createNewOrder("Counter", _settings.value!!, _user.value!!, null)
        _navigateToOrder.value = "Counter"

    }


}