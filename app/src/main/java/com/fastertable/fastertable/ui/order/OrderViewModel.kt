package com.fastertable.fastertable.ui.order

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fastertable.fastertable.common.Constants
import com.fastertable.fastertable.common.base.BaseViewModel
import com.fastertable.fastertable.data.models.*
import com.fastertable.fastertable.data.repository.*
import com.fastertable.fastertable.services.KitchenPrintingService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class MenusNavigation{
    CATEGORIES,
    MENU_ITEMS,
    MENU_ITEM,
    EDIT_MENU_ITEM
}

enum class AddSubtract{
    ADD,
    SUBTRACT
}

@HiltViewModel
class OrderViewModel @Inject constructor (private val menusRepository: MenusRepository,
                                          private val loginRepository: LoginRepository,
                                          private val orderRepository: OrderRepository,
                                          private val printerService: KitchenPrintingService,
                                          private val getPayment: GetPayment,
                                          private val saveOrder: SaveOrder,
                                          private val updateOrder: UpdateOrder) : BaseViewModel() {

    //region Model Variables
    private var itemNote = ""
    val user: OpsAuth = loginRepository.getOpsUser()!!
    val settings: Settings = loginRepository.getSettings()!!
    var reorderList = mutableListOf<ReorderDrink>()

    private val _menus = MutableLiveData<List<Menu>>()
    val menus: LiveData<List<Menu>>
        get() = _menus

    private val _activeCategory = MutableLiveData<MenuCategory>()
    val activeCategory: LiveData<MenuCategory>
        get() = _activeCategory

    private val _activeItem = MutableLiveData<MenuItem?>()
    val activeItem: LiveData<MenuItem?>
        get() = _activeItem

    private val _editItem = MutableLiveData<OrderItem?>()
    val editItem: LiveData<OrderItem?>
        get() = _editItem

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

    private val _activeOrder = MutableLiveData<Order?>()
    val activeOrder: LiveData<Order?>
        get() = _activeOrder

    private val _orderItem = MutableLiveData<OrderItem?>()
    val orderItem: LiveData<OrderItem?>
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

    private val _orderMore = MutableLiveData<Boolean>()
    val orderMore: LiveData<Boolean>
        get() = _orderMore

    private val _showTransfer = MutableLiveData<Boolean>()
    val showTransfer: LiveData<Boolean>
        get() = _showTransfer

    private val _ticketsPrinted = MutableLiveData<Order>()
    val ticketsPrinted: LiveData<Order>
        get() = _ticketsPrinted

    private val _showTableDialog = MutableLiveData<Boolean>()
    val showTableDialog: LiveData<Boolean>
        get() = _showTableDialog

    private val _noOrderItems = MutableLiveData<Boolean>()
    val noOrderItems: LiveData<Boolean>
        get() = _noOrderItems

    private val _drinkList = MutableLiveData<List<ReorderDrink>>()
    val drinkList: LiveData<List<ReorderDrink>>
        get() = _drinkList

    private val _error = MutableLiveData<Boolean>()
    val error: LiveData<Boolean>
        get() = _error

    private val _errorTitle = MutableLiveData<String>()
    val errorTitle: LiveData<String>
        get() = _errorTitle

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String>
        get() = _errorMessage

    //endregion

    //region Initialization
    init{
        setPageLoaded(false)
        viewModelScope.launch {
            _menus.postValue(menusRepository.getMenus())
            _menusNavigation.value = MenusNavigation.CATEGORIES
        }
    }

    fun initOrder(){
        viewModelScope.launch {
            if (currentOrderId.value != null){
                val id = currentOrderId.value!!
                _activeOrder.postValue(orderRepository.getOrderById(id))
                getPayment.getPayment(id.replace("O_", "P_"), settings.locationId)
            }else{
                _activeOrder.postValue(orderRepository.getNewOrder())
            }

            _itemQuantity.postValue(1)
            _pageLoaded.postValue(true)
        }
    }

    fun setPageLoaded(b: Boolean){
        _pageLoaded.value = b
    }

    //endregion

    //region Guest Functions

    fun setActiveGuest(g: Guest){
        _activeOrder.value?.guests?.forEach { guest ->
            guest.uiActive = guest.id == g.id
        }
        _activeOrder.value = _activeOrder.value
    }

    fun addGuest(){
        _activeOrder.value?.guestAdd()
        _activeOrder.value = _activeOrder.value
    }

    //endregion

    // region Modifier Functions

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

    //endregion

    //region Ingredient Functions

    private fun AddSubtract.updateWorkingPrice(surcharge: Double){
        when (this){
            AddSubtract.ADD -> _workingItemPrice.value = workingItemPrice.value!!.plus(surcharge.times(itemQuantity.value!!))
            AddSubtract.SUBTRACT -> _workingItemPrice.value = workingItemPrice.value!!.minus(surcharge.times(itemQuantity.value!!))
        }
    }

    private fun onIngredientLoad(item: IngredientChange){
        val ings = _activeItem.value?.ingredients
        if (ings != null){
            val ing = ings.find{it.name == item.item.name}
            if (ing != null){
                ing.orderValue = item.item.orderValue
            }
            _changedIngredients.value = ings!!
        }
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
    //endregion

    //region MenuItem Functions

    fun addItemToOrder(){
        if (activeOrder.value!!.closeTime == null) {
            val mods = arrayListOf<ModifierItem>()
            activeItem.value!!.modifiers.forEach { mod ->
                mod.modifierItems.forEach { mi ->
                    if (mi.quantity > 0) {
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

            val g = _activeOrder.value!!.guests!!.find { it -> it.uiActive }
            g?.orderItemAdd(item)

            _activeOrder.value = _activeOrder.value
            _menusNavigation.value = MenusNavigation.CATEGORIES
            clearItemSettings()
        }else{
            setError("Order Closed", Constants.ORDER_CLOSED)
        }
    }

    fun addAdHocItem(item: OrderItem){
        val g = _activeOrder.value!!.guests!!.find { it -> it.uiActive }
        g?.orderItemAdd(item)

        _activeOrder.value = _activeOrder.value
        _menusNavigation.value = MenusNavigation.CATEGORIES
    }

    fun saveModifiedItem(){
        val mods = arrayListOf<ModifierItem>()
        activeItem.value!!.modifiers.forEach { mod ->
            mod.modifierItems.forEach { mi ->
                if (mi.quantity > 0){
                    mods.add(mi)
                }
            }
        }

        _editItem.value?.orderMods = mods
        _editItem.value?.quantity = itemQuantity.value!!
        _editItem.value?.ingredients = changedIngredients.value

        for (guest in _activeOrder.value?.guests!!){
            if (guest.uiActive){
                guest.orderItems?.find{ it.id == _editItem.value?.id }?.orderMods = mods
                guest.orderItems?.find{ it.id == _editItem.value?.id }?.quantity = itemQuantity.value!!
                guest.orderItems?.find{ it.id == _editItem.value?.id }?.ingredients = changedIngredients.value
            }
        }

        _activeOrder.value = _activeOrder.value
        clearItemSettings()
    }

    private fun findPrepStation(): PrepStation?{
        val printerName = activeItem.value!!.printer.printerName
        return settings.getPrepStation(printerName)
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

    private fun enableAddItemButton(){
        var enable = true
        activeItem.value?.modifiers?.forEach { it ->
            if (it.selectionLimitMin >= 1){
                enable = false
            }
        }
        _enableAddButton.value = enable
    }

    private suspend fun modifyOrderItem(item: OrderItem){
        val out = viewModelScope.launch {
            _editItem.value = item
            _itemQuantity.value = item.quantity
        }

        out.join()
        setActiveItem(findMenuItem(item.menuItemId)!!)
        if (item.ingredients != null){
            for (ing in item.ingredients!!){
                val ig = IngredientChange(
                    item = ing,
                    value = 1
                )
                onIngredientLoad(ig)
                _workingItemPrice.value = item.getExtendedPrice()
            }
        }
    }

    //endregion

    //region Menu Functions

    fun setActiveCategory(cat: MenuCategory){
        _activeCategory.value = cat
    }

    fun setActiveItem(menuItem: MenuItem) {
        _modifiers.value = null
        modList.clear()
        _activeItem.value = menuItem.clone()
        enableAddItemButton()
        if (_editItem.value == null){
            activeItem.value?.modifiers?.forEachIndexed { int, mod ->
                mod.arrayId = int.toDouble()
                modList.add(mod)
            }
        }else{
            for (modItem in editItem.value?.orderMods!!){
                activeItem.value?.modifiers?.forEachIndexed { int, mod ->
                    mod.arrayId = int.toDouble()
                    val found = mod.modifierItems.find{it.itemName == modItem.itemName}
                    if (found != null){
                        val om = OrderMod(
                            item = modItem,
                            mod = mod
                        )
                        onModItemClicked(om)
                    }
                    modList.add(mod)
                }
            }
        }


        _modifiers.value = modList
        _changedIngredients.value = activeItem.value?.ingredients
        _workingItemPrice.value = activeItem.value!!.prices[0].price

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

    private fun findMenuItem(id: String): MenuItem? {
        val list = mutableListOf<MenuItem>()
        for (menu in menus.value!!){
            list.addAll(menu.getMenuItems())
        }
        return list.find{it.id == id}
    }

    //endregion

    //region Note Functions

    fun openNoteDialog(){
        _showOrderNote.value = true
    }

    fun saveOrderNote(note: String){
        itemNote = note
    }

    //endregion

    //region Send Kitchen Functions

    private suspend fun sendToKitchen(){
        viewModelScope.launch {
            var started = false
            val order = activeOrder.value!!
            val orderItems = order.getAllOrderItems()

            if (orderItems.any { it.status == "Started" }){
                started = true
            }

            if (started){
                if (settings.restaurantType == "Counter Service"){
                    if (order.orderNumber == 99 && order.tableNumber == null && order.orderType == "Counter"){
                        _showTableDialog.postValue(true)

                    }else{
                        saveOrderToCloud()
                    }
                }
                if (settings.restaurantType == "Full Service"){
                    saveOrderToCloud()
                    orderRepository.clearNewOrder()
                }
            }else{
                _noOrderItems.postValue(true)
            }
        }


    }

    fun sendKitchenClick(){
        viewModelScope.launch {
            sendToKitchen()
        }
    }

    //endregion

    fun setTableNumber(table: Int){
        viewModelScope.launch {
            if (table != -1){
                _activeOrder.value?.tableNumber = table
            }
            saveOrderToCloud()
        }
    }

    fun orderItemClicked(item: OrderItemTapped){
        viewModelScope.launch {
            if (item.button){
                _orderItemClicked.value = item.item
            }else{
                modifyOrderItem(item.item)
            }
        }
    }

    fun actionOnItemClicked(action: String){
        when (action) {
            "Delete" -> {
                _activeOrder.value?.orderItemRemove(orderItemClicked.value!!)
            }
            "Toggle Rush" -> {
                _activeOrder.value?.toggleItemRush(orderItemClicked.value!!)
            }
            "Toggle Takeout"-> {
                _activeOrder.value?.toggleItemTakeout(orderItemClicked.value!!)
            }
            "Toggle No Make"-> {
                _activeOrder.value?.toggleItemNoMake(orderItemClicked.value!!)
            }
            "Modify Order Item" -> {
                viewModelScope.launch {
                    modifyOrderItem(orderItemClicked.value!!)
                }
            }
            else -> {
            }
        }
        _activeOrder.value = _activeOrder.value
    }

    fun reorderDrinks(){
        if (activeOrder.value!!.closeTime == null) {
            viewModelScope.launch {
                reorderList = mutableListOf<ReorderDrink>()
                for (guest in activeOrder.value?.guests!!) {
                    val oi = guest.orderItems?.findLast { it.salesCategory == "Bar" }
                    if (oi != null) {
                        val drink = ReorderDrink(
                            guestId = guest.id,
                            drink = oi
                        )
                        reorderList.add(drink)
                    }
                }
                _drinkList.value = emptyList()
                _drinkList.postValue(reorderList)
            }
        }else{
            setError("Order Closed", Constants.ORDER_CLOSED)
        }
    }

    fun addDrinksToOrder(){
        if (activeOrder.value!!.closeTime != null){
            for (drink in reorderList){
                val newOrderItem = drink.drink.deepCopy()
                newOrderItem.id = _activeOrder.value?.guests?.get(drink.guestId)?.orderItems?.last()?.id!!.plus(1)
                newOrderItem.status = "Started"
                _activeOrder.value?.guests?.get(drink.guestId)?.orderItems!!.add(newOrderItem)
            }
            _activeOrder.value = _activeOrder.value
        }
    }

    suspend fun saveOrderToCloud(){
        var o: Order? = null
        val job = viewModelScope.launch {
            if (_activeOrder.value?.orderNumber!! == 99){
                o = saveOrder.saveOrder(_activeOrder.value!!)
                val list = o!!.getKitchenTickets()
                printerService.printKitchenTickets(list, settings)

            }else{
                val list = _activeOrder.value!!.getKitchenTickets()
                printerService.printKitchenTickets(list, settings)
            }
        }

        job.join()
        if (o != null){
            updateOrderStatus(o!!)
        }else{
            updateOrderStatus(_activeOrder.value!!)
        }

    }

    fun updateOrderStatus(order: Order){
        viewModelScope.launch {
            order.guests?.forEach{ guest ->
                guest.orderItems?.forEach { item ->
                    item.status = "Kitchen"
                }
            }
            val o = updateOrder.saveOrder(order)
            _activeOrder.postValue(o)
        }

    }

    fun navToPayment(b: Boolean){
        _paymentClicked.value = b
    }

    fun setCurrentOrderId(id: String){
        _currentOrderId.value = id
    }

    fun clearOrder(){
        orderRepository.clearOrder()
        _currentOrderId.value = null
        _activeOrder.value = null
        _activeOrder.value = _activeOrder.value
    }

    fun closeOrder(){
        viewModelScope.launch {
            _activeOrder.value?.close()
            saveOrderToCloud()
        }

    }

    fun setOpenMore(b: Boolean){
        _orderMore.value = b
    }

    fun showTransferOrder(b: Boolean){
        _showTransfer.value = b
    }

    fun reOpenCheckoutSendOrder(){
        viewModelScope.launch {
            sendToKitchen()
        }
    }

    private fun setError(title: String, message: String){
        _errorTitle.value = title
        _errorMessage.value = message
        _error.value = true

    }

    fun clearError(){
        _error.value = false
    }
}

