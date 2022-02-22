package com.fastertable.fastertable2022.data.models

import android.os.Parcelable
import android.util.Log
import com.fastertable.fastertable2022.services.PrintTicketService
import com.fastertable.fastertable2022.utils.GlobalUtils
import com.fastertable.fastertable2022.utils.round
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import com.squareup.moshi.JsonClass
import technology.master.kotlinprint.printer.Document
import technology.master.kotlinprint.printer.DocumentSettings
import technology.master.kotlinprint.printer.Epson
import technology.master.kotlinprint.printer.PrinterDriver

@JsonClass(generateAdapter = true)
@Parcelize
data class Order(
    val orderType: String,
    val orderNumber: Int,
    var tableNumber: Int?,
    var employeeId: String?,
    var userName: String,
    val startTime: Long,
    var closeTime: Long?,
    val midnight: Long,
    var orderStatus: String,
    val kitchenStatus: Boolean,
    val rush: Boolean?,

//    var guests: MutableList<Guest>?,
    var guestCount: Int = 1,
    var activeGuest: Int = 1,
    var splitChecks: MutableList<Check>?,
    var note: String?,
    val orderItems: MutableList<OrderItem>?,

    var customer: Customer?,
    var takeOutCustomer: TakeOutCustomer?,
    var outsideDelivery: DeliveryCustomer?,

    val orderFees: Double?,
    val orderDiscount: Double?,
    var pendingApproval: Boolean,

    val gratuity: Double,
    val subTotal: Double,
    val tax: Double,
    val taxRate: Double,
    val total: Double,

    var accepted: Boolean?,
    var estReadyTime: Long?,
    var estDeliveryTime: Long?,
    var transfer: Boolean?,

    var id: String,
    @SerializedName("locationid")
        val locationId: String,
    val archived: Boolean,
    val type: String,
    val _rid: String?,
    val _self: String?,
    val _etag: String?,
    val _attachments: String?,
    val _ts: Long?
): Parcelable {
    fun guestAdd(){
        guestCount = guestCount.plus(1)
    }

    fun setActiveGuestFirst(){
        activeGuest = 1
    }

    fun orderItemRemove(item: OrderItem){
        orderItems?.remove(item)

    }

    fun toggleItemRush(item: OrderItem){
        item.rush = !item.rush
    }

    fun toggleItemTakeout(item: OrderItem){
        item.takeOutFlag = !item.takeOutFlag
    }

    fun toggleItemNoMake(item: OrderItem){
        item.dontMake = !item.dontMake
    }

    fun getNewItemId(): Int{
        return if (this.orderItems.isNullOrEmpty()){
            1
        }else{
            this.orderItems.last().id.plus(1)
        }
    }

    fun addItemNote(item: OrderItem){
        item.note = item.note
    }

    fun acceptTakeoutOrder(accept: Boolean, readyTime: Long) = if (accept){
        this.accepted = true
        this.estReadyTime = readyTime
    }else{
        this.accepted = false
    }

    fun acceptDeliveryOrder(accept: Boolean, deliveryTime: Long) = if (accept){
        this.accepted = true
        this.estDeliveryTime = deliveryTime
    }else{
        this.accepted = false
    }

    fun getAllOrderItems(): MutableList<OrderItem>? {
        return orderItems
    }

    fun getSubtotal(): Double{
        var price: Double = 0.00
        val list: MutableList<OrderItem>? = orderItems

        list?.forEach{ item ->
            price += item.getExtendedPrice()
        }
        return price
    }

    private fun getSalesTax(): Double{
        val subTotal: Double = this.getSubtotal()
        return subTotal * taxRate
    }

    fun createSingleTicket(fees: List<AdditionalFees>?): Ticket{
        val ticketItems = arrayListOf<TicketItem>()
        if (orderItems != null) {
            for (item in orderItems){
                val i = ticketItems.size.plus(1)
                val ticketItem = createTicketItem(i, item)
                ticketItems.add(ticketItem)
            }
        }
        return createTicket(1, ticketItems, fees)
    }


    fun createTicketItem(index: Int, orderItem: OrderItem): TicketItem{

        return TicketItem(
            id = index,
            orderGuestNo = orderItem.guestId,
            orderItemId = orderItem.id,
            quantity = orderItem.menuItemPrice.quantity,
            itemName = orderItem.menuItemName,
            itemPrice = orderItem.menuItemPrice.price,
            discountPrice = null,
            priceModified = orderItem.priceAdjusted,
            approvalType = null,
            approvalId = null,
            itemMods = ArrayList(orderItem.activeModItems()),
            salesCategory = orderItem.salesCategory,
            ticketItemPrice = orderItem.getTicketExtendedPrice(this.taxRate).round(2),
            tax = orderItem.getSalesTax(orderItem.tax, this.taxRate).round(2)
        )

    }

    //Used when there is splitting tickets by guest
    fun createTicketItemSplit(index: Int, orderItem: OrderItem, split: Int): TicketItem{
        return TicketItem(
            id = index,
            orderGuestNo = index,
            orderItemId = orderItem.id,
            quantity = orderItem.menuItemPrice.quantity,
            itemName = orderItem.menuItemName,
            itemPrice = orderItem.menuItemPrice.price,
            discountPrice = null,
            priceModified = orderItem.priceAdjusted,
            approvalType = null,
            approvalId = null,
            itemMods = ArrayList(orderItem.activeModItems()),
            salesCategory = orderItem.salesCategory,
            ticketItemPrice = orderItem.getTicketExtendedPrice(this.taxRate).div(split).round(2),
            tax = orderItem.getSalesTax(orderItem.tax, this.taxRate).div(split).round(2)
        )
    }

    fun createTicket(ticketId: Int, items: ArrayList<TicketItem>, fees: List<AdditionalFees>?): Ticket{
        val gift = items.filter{ it -> it.itemName == "Gift Card"}

        if (gift.isEmpty()){
            val xfees = calcExtraFees(items.sumOf { it -> it.ticketItemPrice }.round(2), fees)
            var xfeesSum = 0.00
            if (xfees != null){
                xfees.forEach{
                    it.checkAmount?.let{ ca ->
                        xfeesSum = xfeesSum.plus(ca).round(2)
                    }
                }
            }else{
                xfeesSum = 0.00
            }

            return Ticket(
                orderId = this.id,
                id = ticketId,
                ticketItems = items,
                subTotal = items.sumOf { it -> it.ticketItemPrice }.round(2),
                tax = items.sumOf { it -> it.tax }.round(2),
                total = items.sumOf { it -> it.ticketItemPrice }.plus(items.sumOf { it -> it.tax }).plus(xfeesSum).round(2),
                gratuity = 0.00,
                deliveryFee = 0.00,
                extraFees = xfees,
                paymentTotal = 0.00,
                paymentList = null,
                partialPayment = false,
                uiActive = true,
                taxRate = taxRate,
                paymentType = ""
            )
        }else{
            return Ticket(
                orderId = this.id,
                id = ticketId,
                ticketItems = items,
                subTotal = items.sumOf { it -> it.ticketItemPrice }.round(2),
                tax = items.sumOf { it -> it.tax }.round(2),
                total = items.sumOf { it -> it.ticketItemPrice }.plus(items.sumOf { it -> it.tax }).round(2),
                gratuity = 0.00,
                deliveryFee = 0.00,
                extraFees = null,
                paymentTotal = 0.00,
                paymentList = null,
                partialPayment = false,
                uiActive = true,
                taxRate = taxRate,
                paymentType = ""
            )
        }
    }

    private fun calcExtraFees(subTotal: Double, fees: List<AdditionalFees>?): List<AdditionalFees>?{
        if (fees != null){
            for (fee in fees){
                if (fee.feeType.name == "Flat Amount"){
                    fee.checkAmount = fee.amount.round(2)
                }else{
                    fee.checkAmount = subTotal.times(fee.amount.div(100)).round(2)
                }
            }
        }
        return fees
    }

    fun changeGiftItemAmount(amount: Double){
        if (orderItems != null){
            orderItems[0].menuItemPrice.price = amount
        }
//        this.guests?.get(0)?.orderItems?.get(0)?.menuItemPrice?.price = amount
    }

    fun close(){
        this.closeTime = GlobalUtils().getNowEpoch()
        this.orderStatus = "Paid"
    }

    fun forceClose(){
        this.closeTime = GlobalUtils().getNowEpoch()
        this.orderStatus = "Manually Closed"
    }

    fun reopen(){
        this.closeTime = null
        this.orderStatus = "Kitchen"
    }

    fun getOrderTotal(): Double{
        return this.getSubtotal() + this.getSalesTax()
    }

    fun clone(): Order{
        val order: String = Gson().toJson(this, Order::class.java)
        return Gson().fromJson(order, Order::class.java)
    }

    fun getKitchenTickets(): List<Document>{
        val printers = mutableListOf<Printer>()
        val documents = mutableListOf<Document>()

        if (orderItems != null){
            for (item in orderItems){
                if (item.status == "Started"){
                    val p = printers.find{it.id == item.printer.id}
                    if (p == null){
                        printers.add(item.printer)
                    }}
            }
        }

        for (printer in printers){
            Epson.use()

            val doc = PrinterDriver.createDocument(
                DocumentSettings(), "TM_U220"
            )
            if (printer.master){
                //creates the document
                    Log.d("Testing", "In the master")
                PrintTicketService().masterTicket(doc, this)
                documents.add(doc)
            }else{
                //creates the document
                    Log.d("Testing", printer.toString())
                PrintTicketService().kitchenTicket(doc, this, printer)
                documents.add(doc)
            }
        }
        return documents
    }

    fun createMasterTicket(): List<Document>{
        val printers = mutableListOf<Printer>()
        val documents = mutableListOf<Document>()

        orderItems?.let{ items ->
            val foodItems = items.filter { it.status == "Started" && it.salesCategory == "Food" }

            if (foodItems.isNotEmpty()){
                Epson.use()

                val doc = PrinterDriver.createDocument(
                    DocumentSettings(), "TM_U220"
                )
                PrintTicketService().masterTicket(doc, this)
                documents.add(doc)
            }
        }
        return documents
    }

    fun createKitchenTickets(): List<KitchenPrinterTicket>{
        val printers = mutableListOf<Printer>()
        val tickets = mutableListOf<KitchenPrinterTicket>()

        orderItems?.let{ items ->
            val foodItems = items.filter { it.status == "Started" && it.salesCategory == "Food" }

            if (foodItems.isNotEmpty()){
               foodItems.forEach { item ->
                   val p = printers.find{it.id == item.printer.id}
                   if (p == null){
                       printers.add(item.printer)
                   }}
               }
            }

        for (printer in printers){
            Epson.use()

            val doc = PrinterDriver.createDocument(
                DocumentSettings(), "TM_U220"
            )

            if (!printer.master){
                //creates the document
                PrintTicketService().kitchenTicket(doc, this, printer)
                val kt = KitchenPrinterTicket(
                    printer = printer,
                    document = doc
                )
                tickets.add(kt)
            }
        }
        return tickets
    }

    fun createBarTickets(): List<KitchenPrinterTicket>{
        val printers = mutableListOf<Printer>()
        val tickets = mutableListOf<KitchenPrinterTicket>()

        orderItems?.let { items ->
            val barItems = items.filter { it.status == "Started" && it.salesCategory == "Bar" }
            if (barItems.isNotEmpty()) {
                barItems.forEach { item ->
                    val p = printers.find { it.id == item.printer.id }
                    if (p == null) {
                        printers.add(item.printer)
                    }
                }
            }
        }

        for (printer in printers){
            Epson.use()

            val doc = PrinterDriver.createDocument(
                DocumentSettings(), "TM_U220"
            )

            if (!printer.master){
                //creates the document
                PrintTicketService().kitchenTicket(doc, this, printer)
                val kt = KitchenPrinterTicket(
                    printer = printer,
                    document = doc
                )
                tickets.add(kt)
            }
        }

        return tickets
    }

    fun reprintMasterTicket(): List<Document>{
        val printers = mutableListOf<Printer>()
        val documents = mutableListOf<Document>()

        orderItems?.let{ items ->
            val foodItems = items.filter { it.salesCategory == "Food" }

            if (foodItems.isNotEmpty()){
                Epson.use()

                val doc = PrinterDriver.createDocument(
                    DocumentSettings(), "TM_U220"
                )
                PrintTicketService().resendMasterTicket(doc, this)
                documents.add(doc)
            }
        }
        return documents
    }

    fun reprintKitchenTickets(): List<KitchenPrinterTicket>{
        val printers = mutableListOf<Printer>()
        val tickets = mutableListOf<KitchenPrinterTicket>()

        orderItems?.let{ items ->
            val foodItems = items.filter { it.salesCategory == "Food" }

            if (foodItems.isNotEmpty()){
                foodItems.forEach { item ->
                    val p = printers.find{it.id == item.printer.id}
                    if (p == null){
                        printers.add(item.printer)
                    }}
            }
        }

        for (printer in printers){
            Epson.use()

            val doc = PrinterDriver.createDocument(
                DocumentSettings(), "TM_U220"
            )

            if (!printer.master){
                //creates the document
                PrintTicketService().resendKitchenTicket(doc, this, printer)
                val kt = KitchenPrinterTicket(
                    printer = printer,
                    document = doc
                )
                tickets.add(kt)
            }
        }
        return tickets
    }

    fun reprintBarTickets(): List<KitchenPrinterTicket>{
        val printers = mutableListOf<Printer>()
        val tickets = mutableListOf<KitchenPrinterTicket>()

        orderItems?.let { items ->
            val barItems = items.filter { it.salesCategory == "Bar" }
            if (barItems.isNotEmpty()) {
                barItems.forEach { item ->
                    val p = printers.find { it.id == item.printer.id }
                    if (p == null) {
                        printers.add(item.printer)
                    }
                }
            }
        }

        for (printer in printers){
            Epson.use()

            val doc = PrinterDriver.createDocument(
                DocumentSettings(), "TM_U220"
            )

            if (!printer.master){
                //creates the document
                PrintTicketService().resendKitchenTicket(doc, this, printer)
                val kt = KitchenPrinterTicket(
                    printer = printer,
                    document = doc
                )
                tickets.add(kt)
            }
        }

        return tickets
    }


}

@JsonClass(generateAdapter = true)
@Parcelize
data class Guest(
    val id: Int,
    val startTime: Long,
    var orderItems: MutableList<OrderItem>?,
    var subTotal: Double?,
    var tax: Double?,
    var gratuity: Double?,
    var total: Double?,
    var uiActive: Boolean = false
): Parcelable{
    fun orderItemAdd(oi: OrderItem){
        if (this.orderItems == null){
            val list = mutableListOf<OrderItem>()
            list.add(oi)
            this.orderItems = list
        }else{
            this.orderItems?.add(oi)
        }
    }

    fun orderItemRemove(oi: OrderItem){
        this.orderItems?.remove(oi)
    }

    fun getOrderItems(): ArrayList<OrderItem> {
        val list: ArrayList<OrderItem> = arrayListOf()
        this.orderItems?.forEach{ it
            list.add(it)
        }
        return list;
    }
}


@JsonClass(generateAdapter = true)
@Parcelize
data class OrderItem(
    var id: Int,
    var guestId: Int = 1,
    val menuItemId: String,
    val menuItemName: String,
    var menuItemPrice: ItemPrice,
    var modifiers: MutableList<Modifier>?,
    val salesCategory: String,
    var ingredients: List<ItemIngredient>?,
    val prepStation: PrepStation?,
    val printer: Printer,
    val priceAdjusted: Boolean,
    val menuItemDiscount: Double?,
    var takeOutFlag: Boolean,
    var dontMake: Boolean,
    var rush: Boolean,
    val tax: String,
    var note: String?,
    val employeeId: String,
    var status: String,
): Parcelable {
    fun getExtendedPrice(): Double{
        return menuItemPrice.price.plus(menuItemPrice.modifiedPrice).times(menuItemPrice.quantity)
    }

    fun getTicketExtendedPrice(taxRate: Double): Double{
        var price = this.getExtendedPrice()

        if (this.tax == "Tax Included"){
            price = price.minus(this.getSalesTax(this.tax, taxRate))
        }

        return price
    }

    fun getSalesTax(taxType: String, taxRate: Double): Double{
        val tax = when (taxType){
            "Taxable" -> getExtendedPrice().times(taxRate)
            "Tax Exempt" -> 0.00
            "Tax Included" -> getExtendedPrice().times(taxRate)
            else -> getExtendedPrice().times(taxRate)
        }
        return tax
    }

    fun activeModItems(): List<ModifierItem>{
        val list = mutableListOf<ModifierItem>()
        modifiers?.forEach { mod ->
            mod.modifierItems.forEach {
                if (it.quantity > 0){
                    list.add(it)
                }
            }
        }
        return list
    }

    fun activeIngredients(): List<ItemIngredient>{
        val list = mutableListOf<ItemIngredient>()
        ingredients?.forEach {
            if (it.orderValue != 1){
                list.add(it)
            }
        }
        return list
    }

    fun clone() : OrderItem {
        return Gson().fromJson(Gson().toJson(this), this.javaClass)
    }
}

enum class TaxTypes{
    TAXABLE,
    TAX_INCLUDED,
    TAX_EXEMPT
}

@Parcelize
data class OrderItemTapped(
    val item: OrderItem,
    val button: Boolean
): Parcelable


@JsonClass(generateAdapter = true)
@Parcelize
data class Check(
    val orderItems: MutableList<OrderItem>,
    val subTotal: Double,
    val tax: Double,
    val gratuity: Double,
    val total: Double
): Parcelable

@Parcelize
data class OrderDetails(
    val orderItems: MutableList<OrderItem>,
    val subTotal: Double
): Parcelable

@Parcelize
data class OrderMod(
    var item: ModifierItem,
    var mod: Modifier
): Parcelable

@Parcelize
data class ReorderDrink(
    val guestId: Int,
    val drink: OrderItem
): Parcelable

@Parcelize
data class ModifierOrderItem(
    val modifier: Modifier,
    val orderMods: List<ModifierItem>?,
): Parcelable

@Parcelize
data class newGuest(
    val guest: Int,
    val activeGuest: Int
): Parcelable

@Parcelize
data class TimeBasedRequest
    (
    val midnight: Long,
    val locationId: String
): Parcelable

@Parcelize
data class TokenOrder(
    val orderId: String,
    val locationId: String,
    val locationName: String,
    val startTime: Long,
    val orderTotal: Double
): Parcelable

@Parcelize
data class IdRequest
    (
    val id: String,
    val lid: String
): Parcelable


data class KitchenPrinterTicket(
    val printer: Printer,
    val document: Document
)