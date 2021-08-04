package com.fastertable.fastertable.ui.order

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fastertable.fastertable.common.base.BaseViewModel
import com.fastertable.fastertable.data.models.*
import com.fastertable.fastertable.data.repository.GetOnShiftEmployees
import com.fastertable.fastertable.data.repository.LoginRepository
import com.fastertable.fastertable.data.repository.OrderRepository
import com.fastertable.fastertable.data.repository.UpdateOrder
import com.fastertable.fastertable.utils.GlobalUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransferOrderViewModel @Inject constructor (
    private val loginRepository: LoginRepository,
    private val orderRepository: OrderRepository,
    private val updateOrder: UpdateOrder,
    private val getOnShiftEmployees: GetOnShiftEmployees
        ) : BaseViewModel(){

    val user: OpsAuth = loginRepository.getOpsUser()!!
    val settings: Settings = loginRepository.getSettings()!!
    val orders: List<Order> = orderRepository.getOrdersFromFile()!!

    private val _activeEmployees = MutableLiveData<List<Employee>>()
    val activeEmployees: LiveData<List<Employee>>
        get() = _activeEmployees

    private val _activeOrders = MutableLiveData<List<Order>>()
    val activeOrders: LiveData<List<Order>>
        get() = _activeOrders

    private val _selectedOrders = MutableLiveData<MutableList<Order>>()
    val selectedOrders: LiveData<MutableList<Order>>
        get() = _selectedOrders

    private val _initialOrderId = MutableLiveData<String>()
    val initialOrderId: LiveData<String>
        get() = _initialOrderId

    private val _transferComplete = MutableLiveData<Boolean>()
    val transferComplete: LiveData<Boolean>
        get() = _transferComplete

    init {
        _selectedOrders.value = mutableListOf<Order>()
        getEmployees()
        filterOrders()
    }

    private fun getEmployees(){
        viewModelScope.launch {
            val time = CompanyTimeBasedRequest(
                midnight = GlobalUtils().getMidnight(),
                locationId = settings.locationId,
                companyId = settings.companyId
            )
            _activeEmployees.postValue(getOnShiftEmployees.getEmployees(time))
        }
    }

    private fun filterOrders() {
        val permission = Permissions.viewOrders.toString()
        val manager = user.claims.find { it.permission.name == permission && it.permission.value }
        if (user.claims.contains(manager)){
            _activeOrders.value = orders
        }else{
            _activeOrders.value = orders.filter { it.employeeId == user.employeeId }
        }

    }

    fun setInitialOrderId(id: String){
        _initialOrderId.value = id
        _activeOrders.value?.find{ it -> it.id == _initialOrderId.value!!}?.transfer = true
        val o = _activeOrders.value?.find{ it -> it.id == _initialOrderId.value!!}!!
        orderClicked(o)

    }

    fun orderClicked(order: Order){
        if (selectedOrders.value?.contains(order) == true){
            _selectedOrders.value?.remove(order)
        }else{
            _selectedOrders.value?.add(order)
        }
        println(selectedOrders.value)
    }

    fun employeeClicked(employee: Employee){
        viewModelScope.launch {
            if (_selectedOrders.value != null){
                selectedOrders.value?.forEach {
                    it.employeeId = employee.id
                    it.userName = employee.user.userName
                    it.transfer = true
                    updateOrder.saveOrder(it)
                }
                _transferComplete.postValue(true)
            }
        }

    }
}