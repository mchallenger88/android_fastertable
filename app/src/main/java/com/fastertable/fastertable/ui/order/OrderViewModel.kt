package com.fastertable.fastertable.ui.order

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fastertable.fastertable.common.Constants
import com.fastertable.fastertable.common.base.BaseViewModel
import com.fastertable.fastertable.data.models.*
import com.fastertable.fastertable.data.repository.*
import com.fastertable.fastertable.services.KitchenPrintingService
import com.fastertable.fastertable.utils.GlobalUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
                                          private val paymentRepository: PaymentRepository,
                                          private val printerService: KitchenPrintingService,
                                          private val getPayment: GetPayment,
                                          private val saveOrder: SaveOrder,
                                          private val getOrders: GetOrders,
                                          private val updatePayment: UpdatePayment,
                                          private val updateOrder: UpdateOrder) : BaseViewModel() {

    //region Model Variables
    private var itemNote = ""
    val user: OpsAuth = loginRepository.getOpsUser()!!
    val settings: Settings = loginRepository.getSettings()!!
    var reorderList = mutableListOf<ReorderDrink>()
    var transferList = mutableListOf<OrderItem>()

    private val _menus = MutableLiveData<List<Menu>>()
    val menus: LiveData<List<Menu>>
        get() = _menus

    private val _activeCategory = MutableLiveData<MenuCategory>()
    val activeCategory: LiveData<MenuCategory>
        get() = _activeCategory

    private val _activeItem = MutableLiveData<MenuItem?>()
    val activeItem: LiveData<MenuItem?>
        get() = _activeItem

    private val _editItem = MutableLiveData<MenuItem>()
    val editItem: LiveData<MenuItem>
        get() = _editItem

    private val _editOrderItem = MutableLiveData<OrderItem>()
    val editOrderItem: LiveData<OrderItem>
        get() = _editOrderItem

    private val _selectedPrice = MutableLiveData<ItemPrice>()
    val selectedPrice: LiveData<ItemPrice>
        get() = _selectedPrice

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

    private val _activeOrderSet = MutableLiveData(false)
    val activeOrderSet: LiveData<Boolean>
        get() = _activeOrderSet

    private val _orderItem = MutableLiveData<OrderItem?>()
    val orderItem: LiveData<OrderItem?>
        get() = _orderItem

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

    private val _kitchenButtonEnabled = MutableLiveData<Boolean?>()
    val kitchenButtonEnabled: LiveData<Boolean?>
        get() = _kitchenButtonEnabled

    private val _orders = MutableLiveData<List<Order>>()
    val orders: LiveData<List<Order>>
        get() = _orders

    private val _transferComplete = MutableLiveData<Boolean>()
    val transferComplete: LiveData<Boolean>
        get() = _transferComplete

    private val _guestId = MutableLiveData(1)
    val guestId: LiveData<Int>
        get() = _guestId

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
            _pageLoaded.postValue(true)
        }
    }

    fun setPageLoaded(b: Boolean){
        _pageLoaded.value = b
    }

    //endregion

    //region Guest Functions

    fun setActiveGuest(activeGuest: Int){
        _activeOrder.value?.activeGuest = activeGuest
        _activeOrder.value = _activeOrder.value
    }

    fun addGuest(){
        _activeOrder.value?.guestAdd()
        _activeOrder.value = _activeOrder.value
    }

    //endregion

    // region Modifier Functions

    fun onModItemClicked(item: OrderMod) {
        val menuItem = _activeItem.value
        item.mod.addQuantity(item.item)
        menuItem?.sumSurcharges()
        _activeItem.value = menuItem
        _enableAddButton.value = menuItem?.requirementsMet() == true
    }



    //endregion

    //region Ingredient Functions


    fun onIngredientClicked(item: IngredientChange){
        val menuItem = _activeItem.value

        if (item.value == 1){
            item.item.add()
        }

        if (item.value == -1){
            item.item.subtract()
        }
        menuItem?.sumSurcharges()
        _activeItem.value = menuItem
    }
    //endregion

    //region MenuItem Functions

    fun changeSelectedPrice(price: ItemPrice){
        val item = _activeItem.value
        if (item != null){
            for (p in item.prices){
                p.isSelected = p.size == price.size
            }
        }
        _activeItem.value = item
    }

    fun addItemToOrder(){
        val order = _activeOrder.value
        val item = _activeItem.value
        if (order != null && item != null){
            if (order.closeTime ==  null){
                val mods = arrayListOf<ModifierItem>()
                item.modifiers.forEach { mod ->
                    mod.modifierItems.forEach { mi ->
                        if (mi.quantity > 0) {
                            mods.add(mi)
                }}}

                val price = item.prices.find { it.size == _selectedPrice.value?.size }
                if (price != null) {
                    val orderItem = OrderItem(
                        id = order.orderItems?.size?.plus(1) ?: 1,
                        guestId = order.activeGuest ?: 1,
                        menuItemId = item.id,
                        menuItemName = item.itemName,
                        menuItemPrice = price,
                        modifiers = item.modifiers,
                        salesCategory = item.salesCategory,
                        ingredients = item.ingredients,
                        prepStation = findPrepStation()!!,
                        printer = item.printer,
                        priceAdjusted = false,
                        menuItemDiscount = null,
                        takeOutFlag = false,
                        dontMake = false,
                        rush = false,
                        tax = price.tax,
                        note = itemNote,
                        employeeId = user.employeeId,
                        status = "Started",
                    )
                    order.orderItems?.add(orderItem)
                    _activeOrder.value = order
                    _kitchenButtonEnabled.value = true
                    _menusNavigation.value = MenusNavigation.CATEGORIES
                    clearItemSettings()
                }
            }else{
                setError("Order Closed", Constants.ORDER_CLOSED)
            }
        }
    }

    fun saveEditedItem(){
        val mods = arrayListOf<ModifierItem>()
        activeItem.value!!.modifiers.forEach { mod ->
            mod.modifierItems.forEach { mi ->
                if (mi.quantity > 0){
                    mods.add(mi)
                }
            }
        }

        if (_activeOrder.value != null){
            val item = _activeOrder.value!!.orderItems?.find { it.id == _editOrderItem.value?.id }
        }

        _activeOrder.value = _activeOrder.value
        clearItemSettings()
    }

    fun addAdHocItem(item: OrderItem){
        item.guestId = _activeOrder.value?.activeGuest!!
        _activeOrder.value?.orderItems?.add(item)
        _activeOrder.value = _activeOrder.value
        _menusNavigation.value = MenusNavigation.CATEGORIES
    }

    private fun findPrepStation(): PrepStation?{
        val printerName = activeItem.value!!.printer.printerName
        return settings.getPrepStation(printerName)
    }


    fun increaseItemQuantity(){
        if (_activeItem.value?.prices != null){
            for (price in _activeItem.value?.prices!!){
                price.quantity = price.quantity.plus(1)
            }
            _activeItem.value = _activeItem.value
        }
    }

    fun decreaseItemQuantity(){
        if (_activeItem.value?.prices != null){
            for (price in _activeItem.value?.prices!!){
                if (price.quantity > 1){
                    price.quantity = price.quantity.minus(1)
                }
            }

            _activeItem.value = _activeItem.value
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
            val menuItem = findMenuItem(item.menuItemId)
            menuItem?.prices?.forEach{
                it.modifiedPrice = item.menuItemPrice.modifiedPrice
                it.quantity = item.menuItemPrice.quantity
                it.isSelected = it.size == item.menuItemPrice.size
            }
            menuItem?.modifiers = item.modifiers as ArrayList<Modifier>
            menuItem?.ingredients = item.ingredients as ArrayList<ItemIngredient>
            if (menuItem != null){
                _editItem.value = menuItem!!
            }

            _editOrderItem.value = item

        }

    }

    //endregion

    //region Menu Functions

    fun setActiveCategory(cat: MenuCategory){
        _activeCategory.value = cat
    }

    fun setActiveItem(menuItem: MenuItem) {
        _activeItem.value = menuItem.clone()
        _selectedPrice.value = menuItem.prices[0]
        enableAddItemButton()
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
//        _workingItemPrice.value = 0.0
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


            if (order.orderItems!!.any { it.status == "Started" }){
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
            _kitchenButtonEnabled.value = false
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
                val oi = _activeOrder.value?.orderItems?.filter { it.salesCategory == "Bar" }
                if (oi != null) {
                    for (item in oi){
                        val drink = ReorderDrink(
                            guestId = item.guestId,
                            drink = item
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

    fun addDrinksToOrder(drinksList: MutableList<ReorderDrink>){
        if (activeOrder.value!!.closeTime == null){
            for (drink in drinksList){
                val newOrderItem = drink.drink.clone()
                newOrderItem.id = _activeOrder.value?.orderItems?.last()?.id!!.plus(1)
                newOrderItem.status = "Started"
                _activeOrder.value?.orderItems?.add(newOrderItem)
                _kitchenButtonEnabled.value = true
            }
            _activeOrder.value = _activeOrder.value
        }
    }

    private suspend fun saveOrderToCloud(){
        var o: Order? = null
        val job = viewModelScope.launch {
            if (_activeOrder.value?.orderNumber!! == 99){
                o = saveOrder.saveOrder(_activeOrder.value!!)
                val list = o!!.getKitchenTickets()
                printerService.printKitchenTickets(list, settings)
                getOrders()
            }else{
                val list = _activeOrder.value!!.getKitchenTickets()
                printerService.printKitchenTickets(list, settings)
                updatePayment()
                getOrders()
            }
        }

        job.join()
        if (o != null){
            updateOrderStatus(o!!)
        }else{
            updateOrderStatus(_activeOrder.value!!)
        }

    }

    fun updateOrder(order: Order){
        viewModelScope.launch {
            updateOrder.saveOrder(order)
            _activeOrder.value = _activeOrder.value
        }
    }

    fun updatePayment(payment: Payment){
        viewModelScope.launch {
            updatePayment.savePayment(payment)
        }
    }

    fun updatePayment(){
        viewModelScope.launch {
            val order = _activeOrder.value!!
            val p = getPayment.getPayment(order.id.replace("O", "P"), order.locationId)
            if (p != null){
                val payment = paymentRepository.updatePaymentNewOrderItems(p, order)
                updatePayment.savePayment(payment)

            }
        }
    }

    fun updateOrderStatus(order: Order){
        viewModelScope.launch {
            for (item in order.orderItems!!){
                item.status = "Kitchen"
            }
            val o = updateOrder.saveOrder(order)
            _activeOrder.postValue(o)
        }

    }

    private suspend fun getOrders(){
        withContext(Dispatchers.IO){
            val midnight = GlobalUtils().getMidnight()
            val rid = loginRepository.getStringSharedPreferences("rid")
            getOrders.getOrders(midnight, rid!!)
        }
    }

    fun getOrdersFromFile(){
        _orders.value = orderRepository.getOrdersFromFile()
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

    //region Transfer Order

    fun transferAddItem(item: OrderItem){
        val found = transferList.find { it.id == item.id }
        if (found == null){
            transferList.add(item)
        }
    }

    fun transferRemoveItem(item: OrderItem){
        val found = transferList.find { it.id == item.id }
        if (found != null){
            transferList.remove(item)
        }
    }

    fun transferItemsToOrder(orderTo: Order){
        val payment = paymentRepository.getPayment()
        if (transferList.isNotEmpty()){
            //Remove Items from Active Order and Add Items to Selected Order
            for (item in transferList){
                val orderFrom = _activeOrder.value
                orderFrom?.orderItemRemove(item)
                if (orderFrom != null) {
                    updateOrder(orderFrom)
                }

                if (payment?.tickets != null){
                    for (ticket in payment.tickets!!){
                        ticket.removeTicketItem(item)
                    }
                }
                val id = orderTo.orderItems?.last()?.id!!.plus(1)

                item.id = id
                orderTo.orderItems.add(item)
            }

            updateOrder(orderTo)
            if (payment != null){
                payment.recalculateTotals()
                updatePayment(payment)
            }
            setTransferComplete(true)
        }
    }

    fun setTransferComplete(b: Boolean){
        _transferComplete.value = b
    }

    //endregion

}

