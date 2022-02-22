package com.fastertable.fastertable2022.ui.order

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fastertable.fastertable2022.common.Constants
import com.fastertable.fastertable2022.common.base.BaseViewModel
import com.fastertable.fastertable2022.data.models.*
import com.fastertable.fastertable2022.data.repository.*
import com.fastertable.fastertable2022.services.KitchenPrintingService
import com.fastertable.fastertable2022.utils.GlobalUtils
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
    val user: OpsAuth? = loginRepository.getOpsUser()
    val settings: Settings? = loginRepository.getSettings()
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

    //Live Data for Order Info
    private val _currentOrderId = MutableLiveData<String?>()
    val currentOrderId: LiveData<String?>
        get() = _currentOrderId

    private val _activeOrder = MutableLiveData<Order?>()
    val activeOrder: LiveData<Order?>
        get() = _activeOrder

    private val _activePayment = MutableLiveData<Payment?>()
    val activePayment: LiveData<Payment?>
        get() = _activePayment

    private val _activeOrderSet = MutableLiveData(false)
    val activeOrderSet: LiveData<Boolean>
        get() = _activeOrderSet

    private val _orderItem = MutableLiveData<OrderItem?>()
    val orderItem: LiveData<OrderItem?>
        get() = _orderItem

    private val _showOrderNote = MutableLiveData<Boolean>()
    val showOrderNote: LiveData<Boolean>
        get() = _showOrderNote

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

    private val _navHome = MutableLiveData(false)
    val navHome: LiveData<Boolean>
        get() = _navHome

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
                currentOrderId.value?.let { id ->
                    _activeOrder.postValue(orderRepository.getOrderById(id))
                    _activePayment.postValue(getPayment.getPayment(id.replace("O_", "P_"), settings?.locationId ?: ""))
                }
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

    // region Modifier Ingredient Functions

    fun onModItemClicked(item: OrderMod) {
        val menuItem = _activeItem.value
        item.mod.addQuantity(item.item)
        menuItem?.sumSurcharges()
        _activeItem.value = menuItem
        _enableAddButton.value = menuItem?.requirementsMet() == true
    }


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
        _activeItem.value?.let { item ->
            for (p in item.prices){
                p.isSelected = p.size == price.size
            }
        }
        _activeItem.value = _activeItem.value
    }

    fun addItemToOrder(){
        val order = _activeOrder.value
        val item = _activeItem.value
        val payment = _activePayment.value
        //Check to see if there are any payments on the order and if yes then no more items can be added
        if (payment != null){
            if (payment.anyTicketsPaid()){
                setError("Tickets Paid", "Payments have been made on this order. You will have to void the payments or begin a new order.")
            }else{
                if (order != null && item != null){
                    createAndAddItem(order, item)
                }
            }
        }else{
            if (order != null && item != null){
                createAndAddItem(order, item)
            }
        }
    }

    fun createAndAddItem(order: Order, item: MenuItem){
        if (order.closeTime ==  null){
            val mods = arrayListOf<ModifierItem>()
            item.modifiers.forEach { mod ->
                mod.modifierItems.forEach { mi ->
                    if (mi.quantity > 0) {
                        mods.add(mi)
                    }}}

            val price = item.prices.find { it.isSelected }
            if (price != null) {
                val orderItem = OrderItem(
                    id = order.getNewItemId(),
                    guestId = order.activeGuest,
                    menuItemId = item.id,
                    menuItemName = item.itemName,
                    menuItemPrice = price,
                    modifiers = item.modifiers,
                    salesCategory = item.salesCategory,
                    ingredients = item.ingredients,
                    prepStation = findPrepStation(),
                    printer = item.printer,
                    priceAdjusted = false,
                    menuItemDiscount = null,
                    takeOutFlag = false,
                    dontMake = false,
                    rush = false,
                    tax = price.tax,
                    note = itemNote,
                    employeeId = user?.employeeId ?: "",
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

    fun saveEditedItem(orderItem: OrderItem){
        _activeOrder.value?.let { order ->
            val index = order.orderItems?.indexOfFirst { it.id == orderItem.id }
            if (index != null) {
                order.orderItems[index] = orderItem
            }
        }
        _activeOrder.value = _activeOrder.value
        clearItemSettings()
    }

    fun addAdHocItem(item: OrderItem){
        item.guestId = _activeOrder.value?.activeGuest ?: 1
        _activeOrder.value?.orderItems?.add(item)
        _activeOrder.value = _activeOrder.value
        _menusNavigation.value = MenusNavigation.CATEGORIES
    }

    private fun findPrepStation(): PrepStation?{
        settings?.prepStations?.let { stations ->
            var prep = stations[0]
            _activeItem.value?.let {
                prep = settings.getPrepStation(it.printer.printerName)
            }
            return  prep
        }
        return null
    }


    fun increaseItemQuantity(){
        _activeItem.value?.prices?.let { list ->
            for (price in list){
                price.quantity = price.quantity.plus(1)
            }
            _activeItem.value = _activeItem.value
        }
    }

    fun decreaseItemQuantity(){
        _activeItem.value?.prices?.let { list ->
            for (price in list){
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

    private fun modifyOrderItem(item: OrderItem){
        viewModelScope.launch {
            val menuItem = findMenuItem(item.menuItemId)
            menuItem?.prices?.forEach{
                if (it.size == item.menuItemPrice.size){
                    it.modifiedPrice = item.menuItemPrice.modifiedPrice
                    it.quantity = item.menuItemPrice.quantity
                    it.isSelected = true
                }else{
                    it.isSelected = false
                }
            }
            menuItem?.modifiers = item.modifiers as ArrayList<Modifier>
            menuItem?.ingredients = item.ingredients as ArrayList<ItemIngredient>
            menuItem?.let{
                _editItem.value = it
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
        menus.value?.forEach { menu ->
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
            activeOrder.value?.let { order ->
                order.orderItems?.let { items ->
                    if (items.any{ it.status == "Started"}){
                        started = true
                    }
                }

                if (started){
                    settings?.let {
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
                    }

                }else{
                    _noOrderItems.postValue(true)
                }
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

    fun removeOrderItem(){
        _orderItemClicked.value?.let {
            _activeOrder.value?.orderItemRemove(it)
            _activeOrder.value = _activeOrder.value
        }
    }

    fun toggleRush(){
        _orderItemClicked.value?.let {
            _activeOrder.value?.toggleItemRush(it)
            _activeOrder.value = _activeOrder.value
        }
    }

    fun toggleTakeout(){
        _orderItemClicked.value?.let {
            _activeOrder.value?.toggleItemTakeout(it)
            _activeOrder.value = _activeOrder.value
        }
    }

    fun toggleNoMake(){
        orderItemClicked.value?.let {
            _activeOrder.value?.toggleItemNoMake(it)
            _activeOrder.value = _activeOrder.value
        }
    }

    fun modifyOrderItem(){
        viewModelScope.launch {
            modifyOrderItem(_orderItemClicked.value!!)
            _activeOrder.value = _activeOrder.value
        }
    }

    fun reorderDrinks(){
        if (activeOrder.value?.closeTime == null) {
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
        Log.d("Testing", "Add drinks")
        if (activeOrder.value?.closeTime == null){
            for (drink in drinksList){
                val newOrderItem = drink.drink.clone()
                _activeOrder.value?.orderItems?.last()?.id?.let {
                    newOrderItem.id = it.plus (1)
                }
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
            settings?.let{
                _activeOrder.value?.let { order ->
                    if (order.orderNumber == 99){
                        o = saveOrder.saveOrder(order)
                        o?.let {
//                            val list = it.getKitchenTickets()
                            val masterList = it.createMasterTicket()
                            val kitchenList = it.createKitchenTickets()
                            val barList = it.createBarTickets()
                            printerService.printMasterTicket(masterList, settings)
                            printerService.printKitchenTickets(kitchenList, settings)
                            printerService.printKitchenTickets(barList, settings)
                            getOrders()
                        }
                    }else{
                        val masterList = order.createMasterTicket()
                        val kitchenList = order.createKitchenTickets()
                        val barList = order.createBarTickets()
                        printerService.printMasterTicket(masterList, settings)
                        printerService.printKitchenTickets(kitchenList, settings)
                        printerService.printKitchenTickets(barList, settings)
                        updatePayment()
                        getOrders()
                    }
                }
            }

        }

        job.join()
        o?.let {
            updateOrderStatus(it)
        }
        if (o == null){
            _activeOrder.value?.let {
                updateOrderStatus(it)
            }
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
            _activeOrder.value?.let { order ->
                val p = getPayment.getPayment(order.id.replace("O", "P"), order.locationId)
                if (p != null){
                    val payment = paymentRepository.updatePaymentNewOrderItems(p, order)
                    updatePayment.savePayment(payment)

                }
            }
        }
    }

    fun updateOrderStatus(order: Order){
        viewModelScope.launch {
            order.orderItems?.let { items ->
                for (item in items){
                    item.status = "Kitchen"
                }
            }
            val o = updateOrder.saveOrder(order)
            _activeOrder.postValue(o)
        }

    }

    private suspend fun getOrders(){
        withContext(Dispatchers.IO){
            val midnight = GlobalUtils().getMidnight()
            val rid = loginRepository.getStringSharedPreferences("rid")
            getOrders.getOrders(midnight, rid ?: "")
        }
    }

    fun getOrdersFromFile(){
        _orders.value = orderRepository.getOrdersFromFile()
    }

    fun navToPayment(b: Boolean){
        _paymentClicked.value = b
    }

    fun setNavHome(b: Boolean){
        _navHome.value = b
    }

    fun setCurrentOrderId(id: String){
        _currentOrderId.value = id
    }

    fun clearOrder(){
        orderRepository.clearOrder()
        _currentOrderId.value = null
        _activeOrder.value = null
        _activePayment.value = null
        _activeOrder.value = _activeOrder.value
        _activePayment.value = _activePayment.value
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

    fun reOpenCheckoutSendOrder(){
        viewModelScope.launch {
            sendToKitchen()
        }
    }

    fun setError(title: String, message: String){
        _errorTitle.value = title
        _errorMessage.value = message
        _error.value = true

    }

    fun clearError(){
        _error.value = false
    }

    fun resendKitchenTicket(){
        viewModelScope.launch {
            settings?.let {
                _activeOrder.value?.let {
                    val masterList = it.reprintMasterTicket()
                    val kitchenList = it.reprintKitchenTickets()
                    val barList = it.reprintBarTickets()
                    printerService.printMasterTicket(masterList, settings)
                    printerService.printKitchenTickets(kitchenList, settings)
                    printerService.printKitchenTickets(barList, settings)
                }
            }
        }
    }

    //region Transfer Order

    fun showTransferOrder(b: Boolean){
        _showTransfer.value = b
    }

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

                payment?.tickets?.forEach {
                    it.removeTicketItem(item)
                }

                orderTo.orderItems?.last()?.id?.let {
                    item.id = it.plus(1)
                    orderTo.orderItems.add(item)
                }
                }
            }

            updateOrder(orderTo)
            if (payment != null){
                payment.recalculateTotals()
                updatePayment(payment)
            }
            setTransferComplete(true)
    }

    fun setTransferComplete(b: Boolean){
        _transferComplete.value = b
    }

    //endregion

}

