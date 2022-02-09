package com.fastertable.fastertable2022.ui.home

import androidx.lifecycle.*
import com.fastertable.fastertable2022.data.models.*
import com.fastertable.fastertable2022.data.repository.GetOrders
import com.fastertable.fastertable2022.data.repository.LoginRepository
import com.fastertable.fastertable2022.data.repository.OrderRepository
import com.fastertable.fastertable2022.utils.GlobalUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor (private val loginRepository: LoginRepository,
                                         private val getOrders: GetOrders,
                                         private val orderRepository: OrderRepository) : ViewModel() {

    val user: OpsAuth? = loginRepository.getOpsUser()
    val settings: Settings? = loginRepository.getSettings()
    val terminal: Terminal? = loginRepository.getTerminal()

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

    private val _orderFilter = MutableLiveData("Open")
    val orderFilter: LiveData<String>
        get() = _orderFilter

    private val _navigateToOrder = MutableLiveData<String>()
    val navigateToOrder: LiveData<String>
        get() = _navigateToOrder

    private val _navigateToPayment = MutableLiveData<String>()
    val navigateToPayment: LiveData<String>
        get() = _navigateToPayment

    private val _navigateToVoid = MutableLiveData<String>()
    val navigateToVoid: LiveData<String>
        get() = _navigateToVoid

    private val _navigateToFloorplan = MutableLiveData<Boolean>()
    val navigateToFloorplan: LiveData<Boolean>
        get() = _navigateToFloorplan

    private val _navigateToTakeout = MutableLiveData<Boolean>()
    val navigateToTakeout: LiveData<Boolean>
        get() = _navigateToTakeout

    init{
        _showProgressBar.value = false

        viewModelScope.launch {
            _orders.postValue(orderRepository.getOrdersFromFile())
            setOrderFilter("Open")
        }
    }

    fun getOrders(){
        viewModelScope.launch {
           getWebOrders()
        }
    }

    fun getOrdersFromFile(){
        _orders.postValue(orderRepository.getOrdersFromFile())
        setOrderFilter("Open")
    }

    private suspend fun getWebOrders(){
        val job = viewModelScope.launch {
            val midnight = GlobalUtils().getMidnight()
            val rid = loginRepository.getStringSharedPreferences("rid")
            rid?.let {
                getOrders.getOrders(midnight, it)
            }

        }

        job.join()
        _orders.postValue(orderRepository.getOrdersFromFile())
        setOrderFilter("Open")
    }

    fun getCompany(){
        viewModelScope.launch{
            _company.postValue(loginRepository.getCompany())
        }
    }

    fun setNavigateToFloorPlan(b: Boolean){
        _navigateToFloorplan.value = b
    }

    fun setNavigateToTakeout(b: Boolean){
        _navigateToTakeout.value = b
    }

    fun onOrderClicked(id: String) {
        val selectedOrder = _orders.value?.find { it.id == id }
        if (selectedOrder != null){
            if (selectedOrder.closeTime == null){
                _navigateToOrder.value = id
            }else{
                _navigateToVoid.value = id
            }
        }

    }

    fun startCounterOrder(){
        if (settings != null && user != null){
            orderRepository.createNewOrder("Counter", settings, user, null,  null)
            _navigateToOrder.value = "Counter"
        }

    }

    fun navigationEnd(){
        _navigateToOrder.value = ""
    }

    fun setOrderFilter(filter: String){
        _orderFilter.value = filter
        when (filter) {
            "Open" -> _filteredOrders.value = orders.value?.filter { it -> it.closeTime == null }
            "Closed" -> _filteredOrders.value = orders.value?.filter { it -> it.closeTime != null }
            "All" -> _filteredOrders.value = orders.value
        }
    }


}