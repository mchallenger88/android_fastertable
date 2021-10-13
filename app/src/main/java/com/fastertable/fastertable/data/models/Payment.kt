package com.fastertable.fastertable.data.models

import android.os.Parcelable
import com.fastertable.fastertable.services.PrintTicketService
import com.fastertable.fastertable.utils.GlobalUtils
import com.fastertable.fastertable.utils.round
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import technology.master.kotlinprint.printer.Document
import technology.master.kotlinprint.printer.DocumentSettings
import technology.master.kotlinprint.printer.Epson
import technology.master.kotlinprint.printer.PrinterDriver

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
    var splitType: String,
    var splitTicket: Int?,
    val employeeId: String,
    val userName: String,
    var tickets: ArrayList<Ticket>?,
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
        this.tickets!!.forEach { ticket ->

            if (ticket.paymentTotal < ticket.total){
                paid = false
            }
        }
        return paid
    }

    fun anyTicketsPaid(): Boolean{
        var paid: Boolean = false
        this.tickets!!.forEach { ticket ->
            if (ticket.paymentTotal >= ticket.total && ticket.paymentList != null){
                paid = true
            }
        }
        return paid
    }

    private fun anyTicketsModified(): Boolean{
        var modified: Boolean = false
        if (tickets != null){
            for (ticket in tickets!!){
                for (item in ticket.ticketItems){
                    if (item.priceModified){
                        modified = true
                    }
                }
            }
        }

        return modified
    }

    fun activeTicket(): Ticket?{
        return tickets!!.find{it -> it.uiActive}
    }

    fun amountOwed(): Double{
        return if (activeTicket() != null){
            activeTicket()!!.total.minus(activeTicket()!!.paymentTotal).round(2)
        }else
            0.00
    }

    private fun allTicketItems(): ArrayList<TicketItem>{
        val ticketItems = arrayListOf<TicketItem>()
        if (tickets != null){
            for (ticket in tickets!!){
            for (item in ticket.ticketItems){
                ticketItems.add(item)
            }
        }}
        return ticketItems
    }

    private fun recalculateTotals(){
        if (tickets != null){
            for (ticket in tickets!!){
                ticket.subTotal = ticket.ticketItems.sumOf { it -> it.ticketItemPrice }.round(2)
                ticket.tax = ticket.ticketItems.sumOf { it -> it.tax }.round(2)
                ticket.total = ticket.ticketItems.sumOf { it -> it.ticketItemPrice }.plus(ticket.tax)
            }
        }

    }

    fun createSingleTicket(order: Order, fees: List<AdditionalFees>?){
        if (tickets == null){
            val tickets = arrayListOf<Ticket>()
            val ticketItems = arrayListOf<TicketItem>()
            order.guests?.forEach { guest ->
                guest.orderItems?.forEachIndexed { index, orderItem ->
                    val ticketItem = order.createTicketItem(index, orderItem)
                    ticketItems.add(ticketItem)
                }
            }
            val ticket = order.createTicket(0, ticketItems, fees)
            tickets.add(ticket)
            this.tickets = tickets
        }else{
            if (!anyTicketsPaid()){
                var ticketItems = arrayListOf<TicketItem>()
                if (splitType == "Evenly"){
                    ticketItems = tickets!![0].ticketItems
                }else{
                    ticketItems = allTicketItems()
                    println(ticketItems.size)
                }

                for (item in ticketItems){
                    if (item.discountPrice != null){
                        println("discount")
                        item.ticketItemPrice = item.discountPrice!!
                    }else{
                        println("not discount")
                        item.ticketItemPrice = item.itemPrice.times(item.quantity)
                    }
                }
                val ts = arrayListOf<Ticket>()
                tickets!![0].ticketItems = ticketItems
                tickets!![0].uiActive = true
                recalculateTotals()

                ts.add(tickets!![0])
                splitTicket = null
                splitType = "None"
                this.tickets = ts
            }else{
                println("ticktes paid")
            }
        }

    }

    fun splitByGuest(order: Order, fees: List<AdditionalFees>?) {
            if (!anyTicketsPaid()) {
                val ticketItems = allTicketItems()
                val ts = arrayListOf<Ticket>()
                for (guest in order.guests!!){
                    val tis = ticketItems.filter{it.orderGuestNo == guest.id}
                    val ticket = order.createTicket(guest.id, tis.toCollection(ArrayList()), fees)
                    ticket.uiActive = ticket.id == 0
                    ts.add(ticket)
                }
                recalculateTotals()
                splitTicket = order.guests!!.size
                splitType = "Guest"
                this.tickets = ts
            }

    }

    fun splitEvenly(order: Order, fees: List<AdditionalFees>?){
        if (!anyTicketsPaid()) {
            val ticketItems = allTicketItems()
            val ts = arrayListOf<Ticket>()
            val guestCount = order.guests?.size
            for (item in ticketItems){
                item.ticketItemPrice = item.ticketItemPrice.div(guestCount!!).round(2)
            }

            for (guest in order.guests!!){
                val ticket = order.createTicket(guest.id, ticketItems.toCollection(ArrayList()), fees)
                ticket.uiActive = ticket.id == 0
                ts.add(ticket)
            }
            recalculateTotals()
            splitTicket = order.guests!!.size
            splitType = "Evenly"
            this.tickets = ts
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

    fun changeGiftItemAmount(amount: Double){
        this.tickets!!.get(0).ticketItems.get(0).itemPrice = amount
        this.tickets!!.get(0).ticketItems.get(0).ticketItemPrice = amount
        this.tickets!!.get(0).recalculateAfterApproval(0.00)
    }

    fun getTicketReceipt(order: Order,printer: Printer, location: Location): Document{
        Epson.use()
        val document = PrinterDriver.createDocument(
            DocumentSettings(), printer.printerModel)
        PrintTicketService().ticketReceipt(document, order, this, activeTicket()!!, location)

        return document
    }

    fun getCashReceipt(printer: Printer, location: Location): Document{
        Epson.use()
        val document = PrinterDriver.createDocument(
            DocumentSettings(), printer.printerModel)
        PrintTicketService().paidCashReceipt(document, this, activeTicket()!!, location)

        return document
    }

    fun getCreditReceipt(printer: Printer, location: Location): Document{
        Epson.use()
        val document = PrinterDriver.createDocument(
            DocumentSettings(), printer.printerModel)
        PrintTicketService().creditCardReceipt(document, this, activeTicket()!!, location)

        return document
    }
}

@Parcelize
data class Ticket(
    val orderId: String,
    val id: Int,
    var ticketItems: ArrayList<TicketItem>,
    var subTotal: Double,
    var tax: Double,
    var total: Double,
    var gratuity: Double,
    val deliveryFee: Double,
    val extraFees: List<AdditionalFees>?,
    var paymentTotal: Double = 0.00,
    var paymentList: MutableList<TicketPayment>?,
    var partialPayment: Boolean,
    var uiActive: Boolean = false,
    //To be deprecated
    var paymentType: String

    ): Parcelable{
        fun getTicketSubTotal(): Double{
            var price: Double = 0.00
            ticketItems.forEach{ticketItem ->
                price += price.plus(ticketItem.ticketItemPrice)
            }
            return price
        }

        fun calculatePaymentTotal(partial: Boolean){
            paymentTotal = paymentList!!.sumOf {it.ticketPaymentAmount}.round(2)
            partialPayment = partial
        }

        fun recalculateAfterApproval(taxRate: Double){
            subTotal = ticketItems.sumOf { it.ticketItemPrice }.round(2)
            tax = subTotal.times(taxRate).round(2)
            total = subTotal.plus(tax)
        }

        fun addTip(tip: Double, taxRate: Double){
            activePayment()!!.gratuity = tip
//            this.gratuity = tip
            recalculateAfterApproval(taxRate)
            this.total = this.total.plus(tip).round(2)
            this.paymentTotal = this.total;
        }

        fun activePayment(): TicketPayment?{
            if (paymentList != null){
                return paymentList!!.find { it.uiActive }
            }
            return null
        }

        fun ticketTotal(): Double{
            return if (allGratuities() != null){
                total.plus(allGratuities()!!).round(2)
            }else{
                total
            }
        }

        fun allGratuities(): Double?{
            if (paymentList != null){
                val grats = paymentList!!.sumOf{ it.gratuity }.round(2)
                return if (grats != 0.00){
                    grats
                }else{
                    null
                }
            }else{
                return null
            }

        }

    }

//    var paymentType: String,
//    var stageResponse: MutableList<StageResponse>,
//    var creditCardTransactions: ArrayList<CreditCardTransaction>,

@Parcelize
data class TicketPayment(
    val id: Int,
    var paymentType: String,
    val ticketPaymentAmount: Double = 0.00,
    var gratuity: Double = 0.00,
    var creditCardTransactions: ArrayList<CreditCardTransaction>?,
    var uiActive: Boolean
    ): Parcelable {

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
    var tipTransaction: TransactionResponse45?
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
    var itemPrice: Double,
    var discountPrice: Double?,
    var priceModified: Boolean,
    val itemMods: ArrayList<ModifierItem>,
    val salesCategory: String,
    var ticketItemPrice: Double,
    var tax: Double,
): Parcelable{
    fun approve(){
        ticketItemPrice = discountPrice!!
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

@Parcelize
data class PayTicket(
    var order: Order,
    var payment: Payment?,
    var ticket: Ticket?
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

@Parcelize
data class ManualCredit(
    val cardHolder: String,
    val cardNumber: String,
    val expirationDate: String,
    val cvv: String,
    val postalCode: String
): Parcelable