package com.fastertable.fastertable.ui.order

import android.util.Log
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

    val user: OpsAuth? = loginRepository.getOpsUser()
    val settings: Settings? = loginRepository.getSettings()

    private val _activeEmployees = MutableLiveData<List<Employee>>()
    val activeEmployees: LiveData<List<Employee>>
        get() = _activeEmployees

    private val _activeOrders = MutableLiveData<MutableList<Order>>()
    val activeOrders: LiveData<MutableList<Order>>
        get() = _activeOrders

    private val _initialOrderId = MutableLiveData<String>()
    val initialOrderId: LiveData<String>
        get() = _initialOrderId

    private val _transferComplete = MutableLiveData<Boolean>()
    val transferComplete: LiveData<Boolean>
        get() = _transferComplete

    private val _showSpinner = MutableLiveData(false)
    val showSpinner: LiveData<Boolean>
        get() = _showSpinner

    init {
        getEmployees()
        filterOrders()
    }

    private fun getEmployees(){
        viewModelScope.launch {
            settings?.let {
                val time = CompanyTimeBasedRequest(
                    midnight = GlobalUtils().getMidnight(),
                    locationId = settings.locationId,
                    companyId = settings.companyId
                )
                _activeEmployees.postValue(getOnShiftEmployees.getEmployees(time))
            }

        }
    }

    fun refreshOrders(){
        orderRepository.getOrdersFromFile()?.let { orders ->
            orders.forEach { it.transfer = false }
            _activeOrders.value = orders as MutableList<Order>?
        }

    }

    private fun filterOrders() {
        val permission = Permissions.viewOrders.toString()
        user?.let {
            val manager = user.claims.find { it.permission.name == permission && it.permission.value }
            if (!user.claims.contains(manager)){
                _activeOrders.value?.let { orders ->
                    _activeOrders.value = orders.filter { it.employeeId == user.employeeId } as MutableList<Order>?
                }
                _activeOrders.value = _activeOrders.value
            }
        }
    }

    fun setInitialOrderId(id: String){
        if (id.isNotBlank()){
            _activeOrders.value?.forEach {
                if (it.id == id){
                    it.transfer = true
                    _activeOrders.value = _activeOrders.value
                }
            }

        }
    }

    fun orderClicked(order: Order){
        _activeOrders.value?.let { orders ->
            val index = orders.indexOfFirst { it.id == order.id }
            if (index != -1){
                orders[index] = order
            }
        }
    }

    fun initiateTransfer(employee: Employee){
        viewModelScope.launch {
            employeeClicked(employee)
        }
    }

    suspend fun employeeClicked(employee: Employee){
        val job = viewModelScope.launch {
            _showSpinner.postValue(true)
            _activeOrders.value?.forEach {
                if (it.transfer == true){
                    it.employeeId = employee.id
                    it.userName = employee.user.userName
                    it.transfer = true
                    updateOrder.saveOrder(it)
                }
            }
        }
        job.join()
        _showSpinner.postValue(false)
        _transferComplete.postValue(true)

    }
}