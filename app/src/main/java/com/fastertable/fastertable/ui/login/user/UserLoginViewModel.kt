package com.fastertable.fastertable.ui.login.user

import androidx.lifecycle.*
import com.fastertable.fastertable.common.base.BaseViewModel
import com.fastertable.fastertable.data.models.Employee
import com.fastertable.fastertable.data.models.GetEmployee
import com.fastertable.fastertable.data.models.OpsAuth
import com.fastertable.fastertable.data.models.Terminal
import com.fastertable.fastertable.data.repository.GetEmployeeById
import com.fastertable.fastertable.data.repository.GetOrders
import com.fastertable.fastertable.data.repository.LoginRepository
import com.fastertable.fastertable.data.repository.LoginUser
import com.fastertable.fastertable.utils.GlobalUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class UserLoginViewModel @Inject constructor(private val loginRepository: LoginRepository,
                                             private val loginUser: LoginUser,
                                             private val getEmployeeById: GetEmployeeById,
                                             private val getOrders: GetOrders) : BaseViewModel() {

    private var cid: String = ""
    private var lid: String = ""
    private val _pin = MutableLiveData<String>()
    val pin: LiveData<String>
        get() = _pin

    private val _terminal = MutableLiveData<Terminal?>()
    val liveTerminal: LiveData<Terminal?>
        get() = _terminal

    private val _navigate = MutableLiveData<Boolean>()
    val navigate: LiveData<Boolean>
        get() = _navigate

    private val _navigateTerminal = MutableLiveData<Boolean>()
    val navigateTerminal: LiveData<Boolean>
        get() = _navigateTerminal

    private val _kitchen = MutableLiveData<Boolean>()
    val kitchen: LiveData<Boolean>
        get() = _kitchen

    private val _showProgressBar = MutableLiveData<Boolean>()
    val showProgressBar: LiveData<Boolean>
        get() = _showProgressBar

    private val _validUser = MutableLiveData<Boolean?>()
    val validUser: LiveData<Boolean?>
        get() = _validUser

    private val _clockin = MutableLiveData<Boolean>()
    val clockin: LiveData<Boolean>
        get() = _clockin

    private val _loginTime = MutableLiveData<Long>()
    val loginTime: LiveData<Long>
        get() = _loginTime

    private val _employee = MutableLiveData<Employee>()
    val employee: LiveData<Employee>
        get() = _employee

    private val _enterEnabled = MutableLiveData(true)
    val enterEnabled: LiveData<Boolean>
        get() = _enterEnabled

    private var emp: Employee? = null

    private var user: OpsAuth? = null


    init{
        _pin.value = ""
        _navigate.value = false
        _navigateTerminal.value = false
        _showProgressBar.value = false
        getTerminal()
    }

    private fun getTerminal(){
        viewModelScope.launch {
            val term = loginRepository.getTerminal()
            if (term != null){
                _terminal.postValue(term)
            }
        }
    }

    fun setTerminal(terminal: Terminal){
        _terminal.value = terminal
    }

    fun concatPin(num: Int){
        _pin.value = pin.value + num.toString()
    }

    fun pinClear(){
        _pin.value = ""
    }

    fun userLogin(){
        _enterEnabled.value = false
       viewModelScope.launch {
           _showProgressBar.postValue(true)
           loginRepository.getStringSharedPreferences("cid")?.let {
               cid = it
           }
           loginRepository.getStringSharedPreferences("rid")?.let {
               lid = it
           }

           val now = GlobalUtils().getNowEpoch()
           val midnight = GlobalUtils().getMidnight()
           getUserLogin(pin.value.toString(), cid, lid, now, midnight)

       }
    }

    private suspend fun getUserLogin(pin: String, cid: String, lid: String, now: Long, midnight: Long){
        viewModelScope.launch {
            val opsAuth: OpsAuth = loginUser.loginUser(pin, cid, lid, now, midnight)
            user = opsAuth
            if (opsAuth.isAuthenticated){
                _loginTime.postValue(now)
                _validUser.postValue(true)
                getOrders()
                val eids = GetEmployee(cid = cid, eid =opsAuth.employeeId)
                emp = getEmployeeById.getEmployee(eids)
                emp?.let {
                    _employee.postValue(it)
                }

                _showProgressBar.postValue(false)
                _pin.value = ""
                if (isClockIn(opsAuth, now)){
                    _clockin.postValue(true)
                }else{
                    departmentNavigation()
                }
                _enterEnabled.postValue(true)

            }else{
                _validUser.postValue(false)
                _pin.value = ""
                _showProgressBar.postValue(false)
                _enterEnabled.postValue(true)
            }
        }
    }

    private fun departmentNavigation(){
        when(emp?.employeeDetails?.department){
            "Admin" -> _navigate.postValue(true)
            "Waitstaff" -> _navigate.postValue(true)
            "Support" -> _kitchen.postValue(true)
            "Kitchen" -> _kitchen.postValue(true)
            "Host" -> _navigate.postValue(true)
            "Barstaff" -> _navigate.postValue(true)
            "Back Office" -> _kitchen.postValue(true)
            "Manager" -> _navigate.postValue(true)
        }
    }

    fun navigateToHome(){
        _navigate.postValue(true)
    }

    fun navigateToTerminal(b: Boolean){
        _navigateTerminal.postValue(b)
    }

    fun navigateToKitchen(){
        _kitchen.postValue(true)
    }

    private suspend fun getOrders(){
        withContext(IO){
            val midnight = GlobalUtils().getMidnight()
            //    - 86400
            loginRepository.getStringSharedPreferences("rid")?.let {
                getOrders.getOrders(midnight, it)
            }

        }
    }

    fun setUserValid(){
        _validUser.value = null
    }

    private fun isClockIn(user: OpsAuth, loginTime: Long): Boolean{
        return user.userClock.clockInTime == loginTime
    }


}