package com.fastertable.fastertable.ui.order

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fastertable.fastertable.common.Constants
import com.fastertable.fastertable.data.models.*
import com.fastertable.fastertable.data.repository.LoginRepository
import com.fastertable.fastertable.data.repository.MenusRepository
import com.fastertable.fastertable.data.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class MenusNavigation{
    CATEGORIES,
    MENU_ITEMS,
    MENU_ITEM
}

@HiltViewModel
class OrderViewModel @Inject constructor (private val menusRepository: MenusRepository,
                                          private val loginRepository: LoginRepository,
                                          private val orderRepository: OrderRepository) : ViewModel() {

    private lateinit var user: OpsAuth

    //Settings
    private lateinit var settings: Settings
    //Live Data for Menus
    private val _menus = MutableLiveData<List<Menu>>()
    val menus: LiveData<List<Menu>>
        get() = _menus

    private val _activeMenu = MutableLiveData<Menu>()
    val activeMenu: LiveData<Menu>
        get() = _activeMenu

    private val _activeCategory = MutableLiveData<MenuCategory>()
    val activeCategory: LiveData<MenuCategory>
        get() = _activeCategory

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

    private val _pageLoaded = MutableLiveData<Boolean>()
    val pageLoaded: LiveData<Boolean>
        get() = _pageLoaded

    private val _menusNavigation = MutableLiveData<MenusNavigation>()
    val menusNavigation: LiveData<MenusNavigation>
        get() = _menusNavigation

    private var modList = ArrayList<Modifier>()

    //Live Data for Order Info
    private val _order = MutableLiveData<Order>()
    val order: LiveData<Order>
        get() = _order

    private val _guestAdd = MutableLiveData<Boolean>()
    val guestAdd: LiveData<Boolean>
        get() = _guestAdd

    private val _activeGuest = MutableLiveData<Int>()
    val activeGuest: LiveData<Int>
        get() = _activeGuest

    private val _orderItem = MutableLiveData<OrderItem>()
    val orderItem: LiveData<OrderItem>
        get() = _orderItem

    private val _workingItemPrice = MutableLiveData<Double>()
    val workingItemPrice: LiveData<Double>
        get() = _workingItemPrice

    private val orderMods = arrayListOf<OrderMod>()
    private val modItems = arrayListOf<ModifierItem>()


    init{
        setPageLoaded(false)
        viewModelScope.launch {
            getOrder()
            getUser()
            _menus.postValue(menusRepository.getMenus())
            _menusNavigation.value = MenusNavigation.CATEGORIES
            _itemQuantity.postValue(1)
            _pageLoaded.postValue(true)
        }
    }

    fun setPageLoaded(b: Boolean){
        _pageLoaded.value = b
    }

    private fun getUser(){
        viewModelScope.launch {
            user = loginRepository.getOpsUser()!!
        }
    }

    private fun getOrder(){
        viewModelScope.launch {
            _order.postValue(orderRepository.getNewOrder())
            _guestAdd.postValue(true)
            _activeGuest.postValue(1)
        }
    }

    fun setActiveGuest(int: Int){
        _activeGuest.value = int
    }

    fun addGuest(){
        val ord = _order.value
        ord?.guestAdd()
        _order.value = ord!!
        _activeGuest.value = ord.guests?.last()?.id
        _guestAdd.value = true

    }

    fun setGuestAdd(b: Boolean){
        _guestAdd.value = b
    }

    fun onModItemClicked(item: OrderMod) {
        //These are checkboxes
        if (item.mod.selectionLimitMin >= 1 && item.mod.selectionLimitMax > 1) {
        }

        //These are radio buttons
        if (item.mod.selectionLimitMin <= 1 && item.mod.selectionLimitMax <= 1) {
            val found = orderMods.find { x -> x.mod.arrayId == item.mod.arrayId }
            if (found != null) {
                orderMods.remove(found)
                modItems.remove(found.item)
                if (found.item.surcharge > 0){
                    _workingItemPrice.value = workingItemPrice.value!!.minus(found.item.surcharge.times(itemQuantity.value!!))
                }
                orderMods.add(item)
                modItems.add(item.item)
                _workingItemPrice.value = workingItemPrice.value!!.plus(item.item.surcharge.times(itemQuantity.value!!))
            } else {
                orderMods.add(item)
                modItems.add(item.item)
                _workingItemPrice.value = workingItemPrice.value!!.plus(item.item.surcharge.times(itemQuantity.value!!))
            }

        }
    }

    fun onIngredientClicked(item: IngredientList){

    }

    fun addItemToOrder(){

        val item = OrderItem(
            id = 1,
            quantity = itemQuantity.value!!,
            menuItemId = activeItem.value!!.id,
            menuItemName = activeItem.value!!.itemName,
            menuItemPrice = activeItem.value!!.prices[0],
            orderMods = modItems,
            salesCategory = activeItem.value!!.salesCategory,
            ingredientList = activeItem.value!!.ingredients,
            ingredients = activeItem.value!!.ingredients,
            prepStation = findPrepStation()!!,
            printer = activeItem.value!!.printer,
            priceAdjusted = false,
            menuItemDiscount = null,
            takeOutFlag = false,
            dontMake = false,
            rush = false,
            tax = activeItem.value!!.prices[0].tax,
            note = "",
            employeeId = user.employeeId,
            status = "Started",
        )

        _order.value!!.guests!![activeGuest.value!! - 1].orderItemAdd(item)
        _order.value = _order.value

    }

    private fun findPrepStation(): PrepStation?{
        settings = loginRepository.getSettings()!!
        val printerName = activeItem.value!!.printer.printerName
        return settings.prepStations.find{ it ->
            it.stationName == printerName
        }
    }

    fun setActiveMenu(menu: Menu){
        _activeMenu.value = menu
    }

    fun setActiveCategory(cat: MenuCategory){
        _activeCategory.value = cat
    }

    fun setActiveItem(menuItem: MenuItem){
        _modifiers.value = null
        modList.clear()
        _activeItem.value = menuItem

        menuItem.modifiers.forEachIndexed { int, mod ->
            mod.arrayId = int.toDouble()
            modList.add(mod)
        }

        _modifiers.value = modList
        _ingList.value = createIngredientList(menuItem)
        _workingItemPrice.value = activeItem.value!!.prices[0].price
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
        val unitPrice = _workingItemPrice.value!!.div(itemQuantity.value!!)
        _itemQuantity.value = _itemQuantity.value?.plus(1)
        _workingItemPrice.value = unitPrice.times(itemQuantity.value!!)
    }

    fun decreaseItemQuantity(){
        if (itemQuantity.value!! > 1){
            val unitPrice = _workingItemPrice.value!!.div(itemQuantity.value!!)
            _itemQuantity.value = _itemQuantity.value?.minus(1)
            _workingItemPrice.value = unitPrice!!.times(itemQuantity.value!!)
        }
    }

    fun setMenusNavigation(it: MenusNavigation){
        _menusNavigation.value = it
    }

    fun arrowBack(){
        when (menusNavigation.value){
            MenusNavigation.MENU_ITEMS -> _menusNavigation.value = MenusNavigation.CATEGORIES
            MenusNavigation.MENU_ITEM -> _menusNavigation.value = MenusNavigation.MENU_ITEMS
        }
    }

}