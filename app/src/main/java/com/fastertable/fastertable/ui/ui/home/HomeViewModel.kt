package com.fastertable.fastertable.ui.ui.home

import android.app.Application
import androidx.lifecycle.*
import com.fastertable.fastertable.data.Company
import com.fastertable.fastertable.data.Order
import com.fastertable.fastertable.data.Settings
import com.fastertable.fastertable.data.Terminal
import com.fastertable.fastertable.data.repository.LoginRepository
import com.fastertable.fastertable.data.repository.OrderRepository
import com.fastertable.fastertable.utils.GlobalUtils
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File

class HomeViewModel(private val loginRepository: LoginRepository,
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

    private val _orders = MutableLiveData<List<Order>>()
    val orders: LiveData<List<Order>>
        get() = _orders

    private val _navigateToOrder = MutableLiveData<String>()
    val navigateToOrder: LiveData<String>
        get() = _navigateToOrder

    init{
        viewModelScope.launch {
            _showProgressBar.postValue(true)
            getCompany()
            getSettings()
            getTerminal()
            getOrders()
            _showProgressBar.postValue(false)
        }

    }

    private suspend fun getOrders(){
        withContext(IO){
            val midnight = GlobalUtils().getMidnight() - 86400
            val rid = loginRepository.getStringSharedPreferences("rid")
            _orders.postValue(orderRepository.getOrders(midnight, rid!!))
        }
    }

    private suspend fun getCompany(){
        withContext(IO){
            _company.postValue(loginRepository.getCompany())
        }
    }

    private suspend fun getSettings(){
        withContext(IO){
            _settings.postValue(loginRepository.getSettings())
        }
    }

    private suspend fun getTerminal(){
        withContext(IO){
            _terminal.postValue(loginRepository.getTerminal())
        }
    }

    fun onOrderClicked(id: String) {
        _navigateToOrder.value = id
    }
}