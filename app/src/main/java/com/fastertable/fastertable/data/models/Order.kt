package com.fastertable.fastertable.data.models

import android.os.Parcelable
import com.fastertable.fastertable.services.PrintTicketService
import com.fastertable.fastertable.utils.GlobalUtils
import com.fastertable.fastertable.utils.round
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

    var guests: MutableList<Guest>?,
    var splitChecks: MutableList<Check>?,
    var note: String?,

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
        val newGuest = Guest(
            id = this.guests?.size!!,
            startTime = GlobalUtils().getNowEpoch(),
            orderItems = null,
            subTotal = null,
            tax = null,
            gratuity = 0.00,
            total = null,
            uiActive = true
        )
        this.guests?.add(newGuest)
        this.guests?.forEach{guest ->
            if (guest.id != newGuest.id){
                guest.uiActive = false
            }
        }
    }

    fun setActiveGuestFirst(){
        this.guests?.forEach{guest ->
            guest.uiActive = guest.id == 0
        }
    }

    fun guestRemove(guest: Guest){
        this.guests?.remove(guest)
    }

    fun orderItemRemove(item: OrderItem){
        this.guests?.forEach{guest ->
            guest.orderItems?.forEach { oi ->
                if (oi == item){
                    guest.orderItems?.remove(item)
                }}}
    }

    fun toggleItemRush(item: OrderItem){
        this.guests?.forEach{guest ->
            guest.orderItems?.forEach { oi ->
                if (oi == item){
                    oi.rush = !oi.rush
                }}}
    }

    fun toggleItemTakeout(item: OrderItem){
        this.guests?.forEach{guest ->
            guest.orderItems?.forEach { oi ->
                if (oi == item){
                    oi.takeOutFlag = !oi.takeOutFlag
                }}}
    }

    fun toggleItemNoMake(item: OrderItem){
        this.guests?.forEach{guest ->
            guest.orderItems?.forEach { oi ->
                if (oi == item){
                    oi.dontMake = !oi.dontMake
                }}}
    }

    fun addItemNote(item: OrderItem){
        this.guests?.forEach{guest ->
            guest.orderItems?.forEach { oi ->
                if (oi == item){
                    oi.note = item.note
                }}}
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

    fun getAllOrderItems(): ArrayList<OrderItem>{
        val list: ArrayList<OrderItem> = arrayListOf()
        this.guests?.forEach{g ->
            g.orderItems?.forEach{ it
                list.add(it)
            }
        }
        return list
    }

    fun getSubtotal(): Double{
        var price: Double = 0.00
        val list: ArrayList<OrderItem> = this.getAllOrderItems()

        list.forEach{item ->
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
        this.guests?.forEach { guest ->
            guest.orderItems?.forEachIndexed { index, orderItem ->
                val ticketItem = createTicketItem(guest.id, orderItem)
                ticketItems.add(ticketItem)
            }
        }
        return createTicket(0, ticketItems, fees)
    }


    fun createTicketItem(index: Int, orderItem: OrderItem): TicketItem{
        return TicketItem(
            id = index,
            orderGuestNo = index,
            orderItemId = orderItem.id,
            quantity = orderItem.quantity,
            itemName = orderItem.menuItemName,
            itemSize = orderItem.menuItemPrice.size,
            itemPrice = orderItem.menuItemPrice.price,
            discountPrice = null,
            priceModified = orderItem.priceAdjusted,
            itemMods = ArrayList(orderItem.orderMods),
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
            quantity = orderItem.quantity,
            itemName = orderItem.menuItemName,
            itemSize = orderItem.menuItemPrice.size,
            itemPrice = orderItem.menuItemPrice.price,
            discountPrice = null,
            priceModified = orderItem.priceAdjusted,
            itemMods = ArrayList(orderItem.orderMods),
            salesCategory = orderItem.salesCategory,
            ticketItemPrice = orderItem.getTicketExtendedPrice(this.taxRate).div(split).round(2),
            tax = orderItem.getSalesTax(orderItem.tax, this.taxRate).div(split).round(2)
        )
    }

    fun createTicket(ticketId: Int, items: ArrayList<TicketItem>, fees: List<AdditionalFees>?): Ticket{
        val gift = items.filter{ it -> it.itemName == "Gift Card"}

        if (gift.isEmpty()){
            val xfees = calcExtraFees(items.sumOf { it -> it.ticketItemPrice }.round(2), fees)
            val xfeesSum: Double
            if (xfees != null){
                xfeesSum = xfees.sumOf{it -> it.checkAmount!!}.round(2)
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
                paymentType = "",
                gratuity = 0.00,
                deliveryFee = 0.00,
                extraFees = xfees,
                paymentTotal = 0.00,
                stageResponse = arrayListOf<StageResponse>(),
                creditCardTransactions = arrayListOf<CreditCardTransaction>(),
                partialPayment = false,
                uiActive = true
            )
        }else{
            return Ticket(
                orderId = this.id,
                id = ticketId,
                ticketItems = items,
                subTotal = items.sumOf { it -> it.ticketItemPrice }.round(2),
                tax = items.sumOf { it -> it.tax }.round(2),
                total = items.sumOf { it -> it.ticketItemPrice }.plus(items.sumOf { it -> it.tax }).round(2),
                paymentType = "",
                gratuity = 0.00,
                deliveryFee = 0.00,
                extraFees = null,
                paymentTotal = 0.00,
                stageResponse = arrayListOf<StageResponse>(),
                creditCardTransactions = arrayListOf<CreditCardTransaction>(),
                partialPayment = false,
                uiActive = true
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
        this.guests?.get(0)?.orderItems?.get(0)?.menuItemPrice?.price = amount
    }

    fun close(){
        this.closeTime = GlobalUtils().getNowEpoch()
        this.orderStatus = "Paid"
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

        for (guest in guests!!){
            for (item in guest.orderItems!!){
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
                PrintTicketService().masterTicket(doc, this)
                documents.add(doc)
            }else{
                PrintTicketService().kitchenTicket(doc, this, printer)
                documents.add(doc)
            }
        }
        return documents
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
    var quantity: Int,
    val menuItemId: String,
    val menuItemName: String,
    val menuItemPrice: ItemPrice,
    var orderMods: List<ModifierItem>?,
    val salesCategory: String,
    val ingredientList: List<ItemIngredient>?,
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
        var price: Double = this.quantity * this.menuItemPrice.price
        this.orderMods?.forEach{x ->
            if (x.surcharge > 0){
                price += x.surcharge
            }}

        this.ingredients?.forEach{x ->
            if (x.surcharge > 0){
                price += x.surcharge
            }}

        return price
    }

    fun getTicketExtendedPrice(taxRate: Double): Double{
        var price = this.getExtendedPrice()
        if (this.tax === "Tax Included"){
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
        fun deepCopy() : OrderItem {
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

//export interface ReorderDrink{
//    guest: number;
//    item: OrderItem
//}


//export interface OrderItemSelected{
//    item: OrderItem,
//    selected: boolean
//}
//
//export interface AddItemtoOrder{
//    item: MenuItem,
//    price: ItemPrice,
//    modifiers: ModifierItem[] | null
//}
//
//export interface ItemTotals{
//    subTotal: number;
//    tax: number;
//}
//
//export interface ModItemSearch{
//    modName: string;
//    itemName: string;
//}
//
//export interface AddItem{
//    item: MenuItem,
//    price: ItemPrice
//}
//
//export interface CustomerOptions{
//    takeOutCustomer: TakeOutCustomer,
//    customer: Customer
//}
//
//export interface TableSelect{
//    tableNumber: number;
//    inUse: boolean;
//    order: Order
//}
//
//export interface GuestDrinks{
//    guestId: number;
//    drink: OrderItem;
//}
//
//export interface OrderItemDelete{
//    guest: Guest,
//    item: OrderItem
//}
//
//export interface SellingItemsRequest{
//    locationId: string;
//    startDate: number;
//    endDate: number;
//    salesCategory: string
//}
//
//export interface SellingItem{
//    item: string;
//    sum: number;
//}
//
//export interface ItemTap{
//    item: OrderItem;
//    action: string;
//}
//
//export interface TransferGuest{
//    guest: Guest;
//    order: Order;
//}
//

//
//export interface OrderPayment{
//    o: Order;
//    p: Payment;
//}
//
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
//
//export interface OrderEmail{
//    email: string;
//    name: string;
//    order: Order;
//    payment: Payment;
//}
//
//export interface OrderPayment{
//    order: Order;
//    payment: Payment;
//    settings: Settings;
//}