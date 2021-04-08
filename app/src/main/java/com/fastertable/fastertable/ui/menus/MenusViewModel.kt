package com.fastertable.fastertable.ui.menus

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fastertable.fastertable.data.*
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

    private val _activeItem = MutableLiveData<MenuItem>()
    val activeItem: LiveData<MenuItem>
        get() = _activeItem

    private val _itemQuantity = MutableLiveData<Int>()
    val itemQuantity: LiveData<Int>
        get() = _itemQuantity

    private val _modifiers = MutableLiveData<List<Modifier>?>()
    val modifiers: LiveData<List<Modifier>?>
        get() = _modifiers

    private val _ingList = MutableLiveData<List<IngredientList>>()
    val ingList: LiveData<List<IngredientList>?>
        get() = _ingList

    private val _orderItem = MutableLiveData<OrderItem>()
    val orderItem: LiveData<OrderItem>
        get() = _orderItem

    private val _pageLoaded = MutableLiveData<Boolean>()
    val pageLoaded: LiveData<Boolean>
        get() = _pageLoaded

    private var modList = ArrayList<Modifier>()


    init{
        _pageLoaded.value = false
        viewModelScope.launch {
            getMenus()
            _itemQuantity.postValue(1)
        }
    }

    private suspend fun getMenus(){
        withContext(IO){
            _menus.postValue(menusRepository.getMenus())
            _pageLoaded.postValue(true)
        }
    }

    fun setActiveMenu(menu: Menu){
        _previousMenu.value = activeMenu.value
        _activeMenu.value = menu
    }

    fun setActiveItem(menuItem: MenuItem){
        _modifiers.value = null
        modList.clear()
        _activeItem.value = menuItem

        menuItem.modifiers.forEach { mod ->
            modList.add(mod)
        }

        _modifiers.value = modList

        _ingList.value = createIngredientList(menuItem)
    }

    fun createIngredientList(menuItem: MenuItem): ArrayList<IngredientList>{
        val list = IngredientList(
            id = "0",
            locationId = menuItem.id,
            ingredients = menuItem.ingredients.toList()
        )
        val al: ArrayList<IngredientList> = ArrayList<IngredientList>()
        al.add(list)
        return al
    }


    fun increaseItemQuantity(){
        _itemQuantity.value = _itemQuantity.value?.plus(1)
    }

    fun decreaseItemQuantity(){
        println(itemQuantity?.value)
        if (itemQuantity.value!! > 1){
            _itemQuantity.value = _itemQuantity.value?.minus(1)
        }
    }
}