package com.fastertable.fastertable.data.models

import android.os.Parcelable
import com.fastertable.fastertable.utils.GlobalUtils
import com.fastertable.fastertable.utils.round
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Payment(
    val id: String,
    val orderId: String,
    val orderNumber: Int,
    val tableNumber: Int?,
    val timeStamp: Long,
    val orderStartTime: Long,
    var orderCloseTime: Long?,
    val orderType: String,
    val guestCount: Int,
    val splitType: String,
    val splitTicket: Int?,
    val employeeId: String,
    val userName: String,
    var tickets: ArrayList<Ticket>,
    val terminalId: String,
    var statusApproval: String?,
    val newApproval: Approval?,
    var closed: Boolean,
    val taxRate: Double,
    @SerializedName("locationid")
    val locationId: String,
    val archived: Boolean,
    val type: String,
    val _rid: String?,
    val _self: String?,
    val _etag: String?,
    val _attachments: String?,
    val _ts: Long?
): Parcelable{
    fun close(){
        this.orderCloseTime = GlobalUtils().getNowEpoch()
        this.closed = true
    }

    fun allTicketsPaid(): Boolean{
        var paid: Boolean = true
        this.tickets.forEach { ticket ->
            if (ticket.paymentTotal < ticket.total){
                paid = false
            }
        }
        return paid
    }

    private fun anyTicketsPaid(): Boolean{
        var paid: Boolean = false
        this.tickets.forEach { ticket ->
            if (ticket.paymentTotal >= ticket.total){
                paid = true
            }
        }
        return paid
    }

    fun activeTicket(): Ticket?{
        return tickets.find{it -> it.uiActive}
    }

    fun createSingleTicket(order: Order){
        if (!anyTicketsPaid()){
            val tickets = arrayListOf<Ticket>()
            val ticketItems = arrayListOf<TicketItem>()
            order.guests?.forEach { guest ->
                guest.orderItems?.forEachIndexed { index, orderItem ->
                    val ticketItem = order.createTicketItem(index, orderItem)
                    ticketItems.add(ticketItem)
                }
            }
            val ticket = order.createTicket(0, ticketItems)
            tickets.add(ticket)
            this.tickets = tickets
        }

    }

    fun splitByGuest(order: Order) {
        if (!anyTicketsPaid()) {
            val tickets = arrayListOf<Ticket>()
            order.guests?.forEach {guest ->
                val ticketItems = arrayListOf<TicketItem>()
                guest.orderItems?.forEachIndexed { index, orderItem ->
                    val ti = order.createTicketItem(index, orderItem)
                    ticketItems.add(ti)
                }

                val ticket = order.createTicket(guest.id, ticketItems)
                ticket.uiActive = ticket.id == 0
                tickets.add(ticket)
            }
            this.tickets = tickets
        }
    }

    fun splitEvenly(order: Order){
        if (!anyTicketsPaid()) {
            val tickets = arrayListOf<Ticket>()
            val orderItems = order.getAllOrderItems()
            val ticketItems = arrayListOf<TicketItem>()
            orderItems.forEachIndexed { index, orderItem ->
                val ti = order.createTicketItemSplit(index, orderItem, order.guests?.size!!)
                ticketItems.add(ti)
            }
            order.guests?.forEach { guest ->
                val ticket = order.createTicket(guest.id, ticketItems)
                ticket.uiActive = ticket.id == 0
                tickets.add(ticket)
            }
           this.tickets = tickets

        }
    }

    fun voidTicket(){
        activeTicket()?.ticketItems?.forEach { ti ->
            ti.discountPrice = 0.00
            ti.tax = 0.00
            ti.priceModified = true
        }
        statusApproval = "Pending"
    }

    fun voidTicketItem(ticketItem: TicketItem){
        ticketItem.discountPrice = 0.00
        ticketItem.tax = 0.00
        ticketItem.priceModified = true
        statusApproval = "Pending"
    }

    fun modifyItemPrice(ticketItem: TicketItem, price: Double){
        ticketItem.discountPrice = price
        ticketItem.tax = (ticketItem.discountPrice!! * taxRate).round(2)
        ticketItem.priceModified = true
        statusApproval = "Pending"
    }

    fun discountTicketItem(ticketItem: TicketItem, discount: Discount): Double{
        var disTotal: Double = 0.00
        if (discount.discountType == "Flat Amount"){
            if (ticketItem.ticketItemPrice < discount.discountAmount){
                ticketItem.discountPrice = ticketItem.ticketItemPrice.minus(discount.discountAmount).round(2)
                ticketItem.tax = (ticketItem.discountPrice!! * taxRate).round(2)
                ticketItem.priceModified = true
                disTotal = discount.discountAmount
            }else{
                ticketItem.discountPrice = 0.00
                ticketItem.tax = 0.00
                ticketItem.priceModified = true
                disTotal = ticketItem.ticketItemPrice
            }
        }

        if (discount.discountType == "Percentage"){
            disTotal = ticketItem.ticketItemPrice * (discount.discountAmount.div(100)).round(2)
            ticketItem.discountPrice = ticketItem.ticketItemPrice.minus(disTotal).round(2)
            ticketItem.tax = (ticketItem.discountPrice!! * taxRate).round(2)
            ticketItem.priceModified = true
        }
        statusApproval = "Pending"
        return disTotal
    }


    fun discountTicket(discount: Discount): Double{
        var disTotal: Double = 0.00
        if (discount.discountType == "Flat Amount"){
            //Because it's a flat amount have to create a new ticket item to hold the discount amount
            val ticketItem = TicketItem(
                id = activeTicket()!!.ticketItems.size + 1,
                orderGuestNo = 0,
                orderItemId = 0,
                quantity = 1,
                itemName = "Discount",
                itemSize = "Regular",
                itemPrice = 0.00,
                discountPrice = -discount.discountAmount.round(2),
                priceModified = true,
                itemMods = arrayListOf<ModifierItem>(),
                salesCategory = "Discount",
                ticketItemPrice = -discount.discountAmount.round(2),
                tax = 0.00
            )
            activeTicket()!!.ticketItems.add(ticketItem)
            statusApproval = "Pending"
            disTotal = activeTicket()!!.total.minus(discount.discountAmount)
        }

        if (discount.discountType == "Percentage"){

            activeTicket()?.ticketItems?.forEach { ti ->
                val dis = ti.ticketItemPrice * (discount.discountAmount.div(100)).round(2)
                disTotal = disTotal.plus(ti.ticketItemPrice.minus(dis))
                ti.discountPrice = ti.ticketItemPrice.minus(dis)
                ti.tax = (ti.discountPrice!! * taxRate).round(2)
                ti.priceModified = true
            }
            statusApproval = "Pending"
        }
        return disTotal
    }
}

@Parcelize
data class Ticket(
    val orderId: String,
    val id: Int,
    val ticketItems: ArrayList<TicketItem>,
    var subTotal: Double,
    var tax: Double,
    var total: Double,
    var paymentType: String,
    var gratuity: Double,
    val deliveryFee: Double,
    var paymentTotal: Double = 0.00,
    val stageResponse: ArrayList<StageResponse>,
    var creditCardTransactions: ArrayList<CreditCardTransaction>,
    var partialPayment: Boolean,
    var uiActive: Boolean = false

    ): Parcelable{
        fun getTicketSubTotal(): Double{
            var price: Double = 0.00
            ticketItems.forEach{ticketItem ->
                price += price.plus(ticketItem.ticketItemPrice)
            }
            return price
        }

        fun recalculateAfterApproval(taxRate: Double){
            subTotal = ticketItems.sumByDouble { it -> it.ticketItemPrice }.round(2)
            tax = subTotal.times(taxRate).round(2)
            total = subTotal.plus(tax)
        }

        fun addTip(tip: Double, taxRate: Double){
            this.gratuity = tip
            recalculateAfterApproval(taxRate)
            this.total = this.total.plus(tip)
            this.paymentTotal = this.total;
        }
    }

@Parcelize
data class CreditCardTransaction(
    val ticketId: Int,
    val creditTotal: Double?,
    var captureTotal: String?,
    val refundTotal: Double?,
    val voidTotal: Double?,
    val creditTransaction: CayanTransaction,
    var captureTransaction: CaptureResponse?,
    val refundTransaction: TransactionResponse45?,
    val voidTransaction: TransactionResponse45?,
): Parcelable

@Parcelize
data class CaptureResponse(
    var ApprovalStatus: String,
    var Token: String,
    var AuthorizationCode: String,
    var TransactionDate: String,
    var Amount: String
): Parcelable

@Parcelize
data class TicketItem(
    val id: Int,
    val orderGuestNo: Int,
    val orderItemId: Int,
    val quantity: Int,
    val itemName: String,
    val itemSize: String,
    val itemPrice: Double,
    var discountPrice: Double?,
    var priceModified: Boolean,
    val itemMods: ArrayList<ModifierItem>,
    val salesCategory: String,
    var ticketItemPrice: Double,
    var tax: Double,
): Parcelable{
    fun approve(){
        println("Discount")
        println(discountPrice)
        ticketItemPrice = discountPrice!!
        println("Final:")
        println(ticketItemPrice)
    }

    fun reject(){
        discountPrice = null
        priceModified = false
    }


}

@Parcelize
data class PaymentTransaction(
    val paymentType: String,
    val splitType: String,
    val checkTotal: Double,
    val amountTendered: Double,
    val amountPaid: Double,
    val paidInFull: Boolean,
): Parcelable




@Parcelize
data class TicketItemSelected(
    val item: TicketItem,
    val selected: Boolean
): Parcelable

@Parcelize
data class TicketItemChosen(
    val ticketId: Int,
    val itemId: Int,
    val guestNo: Int,
): Parcelable

@Parcelize
data class TicketSelected(
    val item: Ticket,
    val selected: Boolean
): Parcelable

@Parcelize
data class processPay(
    val order: Order,
    val payment: Payment,
    val ticket: Ticket,
    val changeDue: Double,
): Parcelable

@Parcelize
data class SortedTicket (
    val p: Payment,
    val t: Ticket,
): Parcelable

@Parcelize
data class PaymentTickets (
    val p: Payment,
    val t: Ticket,
    val ticketDate: Long,
): Parcelable

@Parcelize
data class adHocMove(
    val type: String,
    val guest: Guest,
    val item: OrderItem,
): Parcelable

@Parcelize
data class MobileCredit(
    val ipAddress: String,
    val printer: Printer,
): Parcelable

@Parcelize
data class UserTicketUpdate(
    val t: Ticket,
    val n: Int,
): Parcelable

@Parcelize
data class MonthRequest(
    val locationId: String,
    val month: Int,
    val year: Int,
    val timeZone: Int,
): Parcelable

@Parcelize
data class SalesSummary(
    val sales: Double,
    val cash: Double,
    val credit: Double,
    val guests: Int,
    val tips: Double,
    val averageTip: Double,
    val salesTax: Double,
    val grossSales: Double,
): Parcelable


enum class CreditTransactionType {
    SALE,
    PREAUTH
}

@Parcelize
data class ModifyPrice(
    val t: TicketItem,
    val price: Double
): Parcelable

@Parcelize
data class ticketAmount(
    val amount: Double,
): Parcelable

@Parcelize
data class secretKey(
    val key: String,
): Parcelable

@Parcelize
data class stripePayment(
    val token: String,
): Parcelable


//data class paymentIntent(
//val id: String,
//val object: any,
//val amount: number,
//val amount_capturable: number,
//val amount_received: number,
//val application: any,
//val application_fee_amount: any,
//val canceled_at: any,
//val cancellation_reason: String,
//val capture_method: any,
//val charges: (
//val object: any,
//val data: [],
//val has_more: Boolean,
//val url: String,
//),
//val client_secret: String,
//val confirmation_method: any,
//val created: any,
//val currency: String,
//val customer: any,
//val description: String,
//val invoice: any,
//val last_payment_error: any,
//val livemode: Boolean,
//val metadata: (),
//val next_action: any,
//val on_behalf_of: any,
//val payment_method: any,
//val payment_method_options: (
//val card: (
//val val installments: any,
//val val network: any,
//val val request_three_d_secure: any
//val )
//),
//val payment_method_types: [],
//val receipt_email: String,
//val review: any,
//val setup_future_usage: any,
//val shipping: any,
//val statement_descriptor: any,
//val statement_descriptor_suffix: any,
//val status: any,
//val transfer_data: any,
//val transfer_group: any
//)
@Parcelize
data class PayTicket(
    var payment: Payment,
    var ticket: Ticket
): Parcelable

@Parcelize
data class CaptureTicket(
    val p: Payment,
    val t: Ticket,
    val cc: CreditCardTransaction
): Parcelable

@Parcelize
data class OrderPayTicket(
    val op: OrderPayment,
    val t: Ticket
): Parcelable

@Parcelize
data class OrderPayTID(
    val op: OrderPayment,
    val ti: Int,
): Parcelable

@Parcelize
data class TipAdjustRequest(
    val credentials: MerchantCredentials,
    val tipRequest: TipRequest
): Parcelable