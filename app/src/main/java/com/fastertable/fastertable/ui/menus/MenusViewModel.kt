package com.fastertable.fastertable.ui.menus

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fastertable.fastertable.data.Menu
import com.fastertable.fastertable.data.repository.MenusRepository
import com.fastertable.fastertable.data.repository.OrderRepository
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MenusViewModel(private val menusRepository: MenusRepository, private val orderRepository: OrderRepository): ViewModel() {

    private val _menus = MutableLiveData<List<Menu>>()
    val menus: LiveData<List<Menu>>
        get() = _menus

    private val _activeMenu = MutableLiveData<Menu>()
    val activeMenu: LiveData<Menu>
        get() = _activeMenu

    private val _previousMenu = MutableLiveData<Menu?>()
    val previousMenu: LiveData<Menu?>
        get() = _previousMenu

    init{
        viewModelScope.launch {
            getMenus()
        }
    }

    private suspend fun getMenus(){
        withContext(IO){
            _menus.postValue(menusRepository.getMenus())
        }
    }

    fun setActiveMenu(menu: Menu){
        _previousMenu.value = activeMenu.value
        _activeMenu.value = menu
    }
}