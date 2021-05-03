package com.fastertable.fastertable.ui.order

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fastertable.fastertable.common.base.BaseViewModel
import com.fastertable.fastertable.data.models.*
import com.fastertable.fastertable.data.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class MenusNavigation{
    CATEGORIES,
    MENU_ITEMS,
    MENU_ITEM
}

enum class AddSubtract{
    ADD,
    SUBTRACT
}

@HiltViewModel
class OrderViewModel @Inject constructor (private val menusRepository: MenusRepository,
                                          private val loginRepository: LoginRepository,
                                          private val orderRepository: OrderRepository,
                                          private val saveOrder: SaveOrder,
                                          private val updateOrder: UpdateOrder) : BaseViewModel() {

    private lateinit var user: OpsAuth
    private var itemNote = ""
    //Settings
    private lateinit var settings: Settings
    //Live Data for Menus
    private val _menus = MutableLiveData<List<Menu>>()
    val menus: LiveData<List<Menu>>
        get() = _menus

    private val _activeCategory = MutableLiveData<MenuCategory>()
    val activeCategory: LiveData<MenuCategory>
        get() = _activeCategory

    private val _activeItem = MutableLiveData<MenuItem?>()
    val activeItem: LiveData<MenuItem?>
        get() = _activeItem

    private val _itemQuantity = MutableLiveData<Int>()
    val itemQuantity: LiveData<Int>
        get() = _itemQuantity

    private val _modifiers = MutableLiveData<List<Modifier>?>()
    val modifiers: LiveData<List<Modifier>?>
        get() = _modifiers

    private val _changedIngredients = MutableLiveData<ArrayList<ItemIngredient>>()
    val changedIngredients: LiveData<ArrayList<ItemIngredient>?>
        get() = _changedIngredients

    private val _pageLoaded = MutableLiveData<Boolean>()
    val pageLoaded: LiveData<Boolean>
        get() = _pageLoaded

    private val _menusNavigation = MutableLiveData<MenusNavigation>()
    val menusNavigation: LiveData<MenusNavigation>
        get() = _menusNavigation

    private val _enableAddButton = MutableLiveData<Boolean>()
    val enableAddButton: LiveData<Boolean>
        get() = _enableAddButton

    private val _orderItemClicked = MutableLiveData<OrderItem>()
    val orderItemClicked: LiveData<OrderItem>
        get() = _orderItemClicked

    private val _paymentClicked = MutableLiveData<Boolean>()
    val paymentClicked: LiveData<Boolean>
        get() = _paymentClicked

    private var modList = ArrayList<Modifier>()

    //Live Data for Order Info
    private val _currentOrderId = MutableLiveData<String?>()
    val currentOrderId: LiveData<String?>
        get() = _currentOrderId

    private val _order = MutableLiveData<Order>()
    val liveOrder: LiveData<Order>
        get() = _order

    private val _orderItem = MutableLiveData<OrderItem>()
    val orderItem: LiveData<OrderItem>
        get() = _orderItem

    private val _workingItemPrice = MutableLiveData<Double>()
    val workingItemPrice: LiveData<Double>
        get() = _workingItemPrice

    private val _showOrderNote = MutableLiveData<Boolean>()
    val showOrderNote: LiveData<Boolean>
        get() = _showOrderNote

    private val _orderNotes = MutableLiveData<String?>()
    val orderNotes: LiveData<String?>
        get() = _orderNotes

    private val _sendKitchen = MutableLiveData<Boolean>()
    val sendKitchen: LiveData<Boolean>
        get() = _sendKitchen


    init{
        setPageLoaded(false)
        viewModelScope.launch {
            getUser()
            settings = loginRepository.getSettings()!!
            _menus.postValue(menusRepository.getMenus())
            _menusNavigation.value = MenusNavigation.CATEGORIES
        }
    }

    fun initOrder(){
        viewModelScope.launch {
            if (currentOrderId.value != null){
                _order.postValue(orderRepository.getOrderById(currentOrderId.value!!))
            }else{
                _order.postValue(orderRepository.getNewOrder())
            }

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
        }
    }

    fun setActiveGuest(g: Guest){
        _order.value?.guests?.forEach { guest ->
            guest.uiActive = guest.id == g.id
        }
        _order.value = _order.value
    }

    fun addGuest(){
        _order.value?.guestAdd()
        _order.value = _order.value
    }

    fun onModItemClicked(item: OrderMod) {
        val flat = listOf(item.mod.modifierItems).flatten()
        val sum = flat.sumOf{x -> x.quantity}

        if (item.mod.selectionLimitMax == 1){
            radioMod(sum, item)
        }

        if (item.mod.selectionLimitMax == 0 || item.mod.selectionLimitMax > 1){
            checkboxMod(sum, item)
        }

        val index = _activeItem.value?.modifiers!!.indexOfFirst { x -> x.id == item.mod.id }
        _activeItem.value!!.modifiers[index] = item.mod
        _activeItem.value = _activeItem.value
        checkModifierValidation()
    }

    private fun checkboxMod(sum: Int, item: OrderMod){
        val max = item.mod.selectionLimitMax

        if (max == 0){
            val it = item.mod.modifierItems.find{x -> x.itemName == item.item.itemName}
            it?.quantity = 1
            if (it?.surcharge!! > 0){
                AddSubtract.ADD.updateWorkingPrice(it.surcharge)
            }
        }

        if (max > 1){
            if (sum < max){
                val it = item.mod.modifierItems.find{x -> x.itemName == item.item.itemName}
                it?.quantity = it?.quantity!!.plus(1)
                if (it.surcharge > 0){
                    AddSubtract.ADD.updateWorkingPrice(it.surcharge)
                }

            }else{
                val it = item.mod.modifierItems.find{x -> x.itemName == item.item.itemName}
                val qty = it?.quantity
                it?.quantity = 0
                if (it?.surcharge!! > 0){
                    AddSubtract.SUBTRACT.updateWorkingPrice(it.surcharge.times(qty!!))
                }

            }
        }
    }

    private fun radioMod(sum: Int, item: OrderMod){
        if (sum == 0){
            val it = item.mod.modifierItems.find{x -> x.itemName == item.item.itemName}
            it?.quantity = 1
            if (it?.surcharge!! > 0){
                AddSubtract.ADD.updateWorkingPrice(it.surcharge)
            }
        }else{
            val it = item.mod.modifierItems.find{x -> x.itemName == item.item.itemName}
            if (it?.quantity == 0) {
                it.quantity = 1
                if (it.surcharge > 0){
                    AddSubtract.ADD.updateWorkingPrice(it.surcharge)
                }
            }
            if (it?.quantity == 1){
                it.quantity = 0
                if (it.surcharge > 0){
                    AddSubtract.SUBTRACT.updateWorkingPrice(it.surcharge)
                }
            }

        }
    }

    private fun AddSubtract.updateWorkingPrice(surcharge: Double){
        when (this){
            AddSubtract.ADD -> _workingItemPrice.value = workingItemPrice.value!!.plus(surcharge.times(itemQuantity.value!!))
            AddSubtract.SUBTRACT -> _workingItemPrice.value = workingItemPrice.value!!.minus(surcharge.times(itemQuantity.value!!))
        }
    }


    fun addItemToOrder(){
        val mods = arrayListOf<ModifierItem>()
        activeItem.value!!.modifiers.forEach { mod ->
            mod.modifierItems.forEach { mi ->
                if (mi.quantity > 0){
                    mods.add(mi)
                }
            }
        }

        val item = OrderItem(
            id = 1,
            quantity = itemQuantity.value!!,
            menuItemId = activeItem.value!!.id,
            menuItemName = activeItem.value!!.itemName,
            menuItemPrice = activeItem.value!!.prices[0],
            orderMods = mods,
            salesCategory = activeItem.value!!.salesCategory,
            ingredientList = null,
            ingredients = changedIngredients.value,
            prepStation = findPrepStation()!!,
            printer = activeItem.value!!.printer,
            priceAdjusted = false,
            menuItemDiscount = null,
            takeOutFlag = false,
            dontMake = false,
            rush = false,
            tax = activeItem.value!!.prices[0].tax,
            note = itemNote,
            employeeId = user.employeeId,
            status = "Started",
        )

        val g = _order.value!!.guests!!.find{it -> it.uiActive }
        g?.orderItemAdd(item)

        _order.value = _order.value
        _menusNavigation.value = MenusNavigation.CATEGORIES
        clearItemSettings()
    }

    private fun findPrepStation(): PrepStation?{
        val printerName = activeItem.value!!.printer.printerName
        return settings.prepStations.find{ it ->
            it.stationName == printerName
        }
    }

    fun setActiveCategory(cat: MenuCategory){
        _activeCategory.value = cat
    }

    fun setActiveItem(menuItem: MenuItem) {
        _modifiers.value = null
        modList.clear()
        _activeItem.value = menuItem.clone()
        enableAddItemButton()
        activeItem.value?.modifiers?.forEachIndexed { int, mod ->
            mod.arrayId = int.toDouble()
            modList.add(mod)
        }

        _modifiers.value = modList
        _changedIngredients.value = activeItem.value?.ingredients
        _workingItemPrice.value = activeItem.value!!.prices[0].price

    }

    fun onIngredientClicked(item: IngredientChange){
        val it = changedIngredients.value?.indexOf(item.item)
        val found = changedIngredients.value?.get(it!!)
        if (item.value == 1){
            if (found?.orderValue!! < 2){
                found.orderValue = found.orderValue.plus(1)
                _changedIngredients.value?.set(it!!, found)
                if (found.orderValue == 2 && found.surcharge > 0.0){
                    AddSubtract.ADD.updateWorkingPrice(found.surcharge)
                }
            }

        }

        if (item.value == -1){
            if (found?.orderValue!! > 0){
                found.orderValue = found.orderValue.minus(1)
                _changedIngredients.value?.set(it!!, found)
                if (found.orderValue == 1 && found.surcharge > 0.0){
                    AddSubtract.SUBTRACT.updateWorkingPrice(found.surcharge)
                }
            }
        }
        _changedIngredients.value = _changedIngredients.value
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
            _workingItemPrice.value = unitPrice.times(itemQuantity.value!!)
        }
    }

    fun setMenusNavigation(it: MenusNavigation){
        _menusNavigation.value = it
    }

    fun arrowBack(){
        when (menusNavigation.value){
            MenusNavigation.MENU_ITEMS -> _menusNavigation.value = MenusNavigation.CATEGORIES
            MenusNavigation.MENU_ITEM -> {
                _menusNavigation.value = MenusNavigation.MENU_ITEMS
                clearItemSettings()
            }else -> MenusNavigation.CATEGORIES
            }
        }

    private fun clearItemSettings(){
        _activeItem.value = null
        _workingItemPrice.value = 0.0
        _itemQuantity.value = 1
    }

    fun openNoteDialog(){
        _showOrderNote.value = true
    }

    fun saveOrderNote(note: String){
        itemNote = note
    }

    private fun enableAddItemButton(){
        var enable = true
        activeItem.value?.modifiers?.forEach { it ->
            if (it.selectionLimitMin >= 1){
                enable = false
            }
        }
        _enableAddButton.value = enable
    }

    private fun checkModifierValidation(){
        var requireMet: Boolean = true
        activeItem.value!!.modifiers.forEach { mod ->
            var count = 0
            mod.modifierItems.forEach { mi ->
                if (mi.quantity > 0){
                    count = count.plus(mi.quantity)
                }
            }
            if (count < mod.selectionLimitMin){
                requireMet = false
            }
        }
        _enableAddButton.value = requireMet
    }

    fun sendToKitchen(){
        _sendKitchen.value = true
    }

    fun setTableNumber(table: Int){
        _order.value?.tableNumber = table
        _order.value = _order.value
    }

    fun orderItemClicked(item:OrderItem){
        _orderItemClicked.value = item
    }

    fun actionOnItemClicked(action: String){
        when (action) {
            "Delete" -> {
                _order.value?.orderItemRemove(orderItemClicked.value!!)
            }
            "Toggle Rush" -> {
                _order.value?.toggleItemRush(orderItemClicked.value!!)
            }
            "Toggle Takeout"-> {
                _order.value?.toggleItemTakeout(orderItemClicked.value!!)
            }
            "Toggle No Make"-> {
                _order.value?.toggleItemNoMake(orderItemClicked.value!!)
            }
            else -> {
            }
        }
        _order.value = _order.value
    }

    fun saveOrderToCloud(ord: Order){
        viewModelScope.launch {
            var o: Order
            if (ord.orderNumber == 99){
                o = saveOrder.saveOrder(ord)
                //After tickets are printed the status of the order items is changed from "Started" to "Kitchen"
                o.printKitchenTicket()
                _order.postValue(o)
                o = updateOrder.saveOrder(o)
                _order.postValue(o)
            }

            if (ord.orderNumber != 99){
                o = updateOrder.saveOrder(ord)
                o.printKitchenTicket()
                _order.postValue(o)
            }
        }
    }

    fun navToPayment(b: Boolean){
        _paymentClicked.value = b
    }

    fun setCurrentOrderId(id: String){
        _currentOrderId.value = id
    }
}

