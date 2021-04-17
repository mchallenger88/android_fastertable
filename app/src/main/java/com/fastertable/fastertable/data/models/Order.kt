package com.fastertable.fastertable.data.models

import android.os.Parcelable
import com.fastertable.fastertable.utils.GlobalUtils
import kotlinx.parcelize.Parcelize
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Parcelize
data class Order(
    val orderType: String,
    val orderNumber: Int,
    val tableNumber: Int?,
    val employeeId: String,
    val userName: String,
    val startTime: Long,
    val closeTime: Long?,
    val midnight: Long,
    val orderStatus: String,
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
    val pendingApproval: Boolean,

    val gratuity: Double,
    val subTotal: Double,
    val tax: Double,
    val total: Double,

    var accepted: Boolean?,
    var estReadyTime: Long?,
    var estDeliveryTime: Long?,

    val id: String,
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
        val count = this.guests?.size
        val newGuest: Guest = Guest(
            id = count!!.plus(1),
            startTime = GlobalUtils().getNowEpoch(),
            orderItems = null,
            subTotal = null,
            tax = null,
            gratuity = 0.00,
            total = null
        )
        this.guests?.add(newGuest)
    }

    fun guestRemove(guest: Guest){
        this.guests?.remove(guest)
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
        return list;
    }

    fun getSubtotal(): Double{
        var price: Double = 0.00
        val list: ArrayList<OrderItem> = this.getAllOrderItems()

        list.forEach{item ->
            price += item.getExtendedPrice()
        }
        return price
    }

    fun getSalesTax(): Double{
        val subTotal: Double = this.getSubtotal()
        //TODO: change this 9% to variable
        return subTotal * 0.09
    }

    fun getOrderTotal(): Double{
        return this.getSubtotal() + this.getSalesTax()
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
    var total: Double?
): Parcelable{
    fun orderItemAdd(oi: OrderItem){
        this.orderItems?.add(oi)
    }

    fun orderItemRemove(oi: OrderItem){
        this.orderItems?.remove(oi)
    }
}


@JsonClass(generateAdapter = true)
@Parcelize
data class OrderItem(
    val id: Int,
    val quantity: Int,
    val menuItemId: String,
    val menuItemName: String,
    val menuItemPrice: ItemPrice,
    val orderMods: MutableList<ModifierItem>?,
    val salesCategory: String,
    val ingredientList: MutableList<ItemIngredient>,
    val ingredients: MutableList<ItemIngredient>,
    val prepStation: PrepStation,
    val printer: Printer,
    val priceAdjusted: Boolean,
    val menuItemDiscount: Double,
    val takeOutFlag: Boolean,
    val dontMake: Boolean,
    val rush: Boolean,
    val tax: String,
    val note: String,
    val employeeId: String,
    val status: String,
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
}


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
    val item: ModifierItem,
    val mod: Modifier
): Parcelable

@JsonClass(generateAdapter = true)
@Parcelize
data class OrderPayment(
    val o: Order,
    val p: Payment
): Parcelable

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
//export interface ReorderDrink{
//    guest: number;
//    item: OrderItem
//}
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