package com.fastertable.fastertable.data.models

import android.os.Parcelable
import android.util.Log
import com.fastertable.fastertable.services.PrintTicketService
import com.fastertable.fastertable.utils.GlobalUtils
import com.fastertable.fastertable.utils.round
import com.google.gson.Gson
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
    var tickets: MutableList<Ticket>?,
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

    fun reopen(){
        this.orderCloseTime = null
        this.closed = false
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

    fun allTicketItems(): ArrayList<TicketItem>{
        val ticketItems = arrayListOf<TicketItem>()
        if (tickets != null){
            for (ticket in tickets!!){
            for (item in ticket.ticketItems){
                ticketItems.add(item)
            }
        }}
        return ticketItems
    }

    fun recalculateTotals(){
        tickets?.let {
            for (ticket in tickets!!){
                Log.d("Testing", ticket.taxRate.toString())
                ticket.subTotal = ticket.ticketItems.sumOf { it -> it.ticketItemPrice }.round(2)
                ticket.tax = ticket.subTotal.times(ticket.taxRate).round(2)
                ticket.total = ticket.subTotal.plus(ticket.tax)
            }
        }
    }

    fun createSingleTicket(order: Order, fees: List<AdditionalFees>?){
        if (tickets == null){
            val tickets = arrayListOf<Ticket>()
            val ticketItems = arrayListOf<TicketItem>()
            var i = 1
            for (item in order.orderItems!!){
                val ticketItem = order.createTicketItem(i, item)
                ticketItems.add(ticketItem)
                i += i
            }

            val ticket = order.createTicket(1, ticketItems, fees)
            tickets.add(ticket)
            this.tickets = tickets
        }else{
            if (!anyTicketsPaid()){
                var ticketItems = mutableListOf<TicketItem>()
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
                tickets?.clear()
                for (i in 1..order.guestCount){
                    val tis = ticketItems.filter{it.orderGuestNo == i}
                    val ticket = order.createTicket(i, tis.toCollection(ArrayList()), fees)
                    ticket.uiActive = ticket.id == 1
                    ts.add(ticket)
                }
                tickets = ts
                recalculateTotals()
                splitTicket = order.guestCount
                splitType = "Guest"
            }

    }

    fun splitEvenly(order: Order, fees: List<AdditionalFees>?){
        if (!anyTicketsPaid()) {
            val ticketItems = allTicketItems()
            val ts = arrayListOf<Ticket>()

            val guestCount = order.guestCount
            for (item in ticketItems){
                item.ticketItemPrice = item.ticketItemPrice.div(guestCount).round(2)
            }
            for (i in 1..guestCount){
                val ticket = order.createTicket(i, ticketItems.toCollection(ArrayList()), fees)
                ticket.uiActive = ticket.id == 1
                ts.add(ticket)
            }
            tickets = ts
            recalculateTotals()
            splitTicket = guestCount
            splitType = "Evenly"
        }
    }

    fun splitEvenlyXTickets(ticketCount: Int, order: Order, fees: List<AdditionalFees>?){
        if (!anyTicketsPaid()) {
            val ticketItems = allTicketItems()
            val ts = arrayListOf<Ticket>()

            for (item in ticketItems){
                item.ticketItemPrice = item.ticketItemPrice.div(ticketCount).round(2)
            }
            for (i in 1..ticketCount){
                val ticket = order.createTicket(i, ticketItems.toCollection(ArrayList()), fees)
                ticket.uiActive = ticket.id == 1
                ts.add(ticket)
            }

            tickets = ts

            recalculateTotals()
            splitTicket = ticketCount
            splitType = "Evenly"
        }
    }

    fun voidTicket(approvalId: String){
        activeTicket()?.ticketItems?.forEach { ti ->
            ti.discountPrice = 0.00
            ti.tax = 0.00
            ti.priceModified = true
            ti.approvalType = "Void Ticket"
            ti.approvalId = approvalId
        }
        statusApproval = "Pending"
    }

    fun voidTicketItem(ticketItem: TicketItem, approvalId: String){
        ticketItem.discountPrice = 0.00
        ticketItem.tax = 0.00
        ticketItem.priceModified = true
        ticketItem.approvalType = "Void Item"
        ticketItem.approvalId = approvalId
        statusApproval = "Pending"
    }

    fun modifyItemPrice(ticketItem: TicketItem, price: Double){
        ticketItem.discountPrice = price
        ticketItem.tax = (ticketItem.discountPrice!! * taxRate).round(2)
        ticketItem.priceModified = true
        ticketItem.approvalType = "Modify Price"
        statusApproval = "Pending"
    }

    fun discountTicketItem(ticketItem: TicketItem, discount: Discount): Double{
        var disTotal: Double = 0.00
        if (discount.discountType == "Flat Amount"){
            if (ticketItem.ticketItemPrice > discount.discountAmount){
                ticketItem.discountPrice = ticketItem.ticketItemPrice.minus(discount.discountAmount).round(2)
                ticketItem.tax = (ticketItem.discountPrice!! * taxRate).round(2)
                ticketItem.priceModified = true
                ticketItem.approvalType = "Discount Item: ${discount.discountName}"
                disTotal = discount.discountAmount
            }else{
                ticketItem.discountPrice = 0.00
                ticketItem.tax = 0.00
                ticketItem.priceModified = true
                ticketItem.approvalType = "Discount Item: ${discount.discountName}"
                disTotal = ticketItem.ticketItemPrice
            }
        }

        if (discount.discountType == "Percentage"){
            disTotal = ticketItem.ticketItemPrice * (discount.discountAmount.div(100)).round(2)
            ticketItem.discountPrice = ticketItem.ticketItemPrice.minus(disTotal).round(2)
            ticketItem.tax = (ticketItem.discountPrice!! * taxRate).round(2)
            ticketItem.priceModified = true
            ticketItem.approvalType = "Discount Item"
        }
        statusApproval = "Pending"
        return disTotal
    }


    fun discountTicket(discount: Discount, approvalId: String): Double{
        var disTotal = 0.00
        if (discount.discountType == "Flat Amount"){
            disTotal = discount.discountAmount
            var discountLeft = discount.discountAmount
            activeTicket()?.ticketItems?.forEach { ticketItem ->
                if (discountLeft > 0.00){
                    if (ticketItem.ticketItemPrice <= discountLeft){
                        ticketItem.discountPrice = 0.00
                        discountLeft = discountLeft.minus(ticketItem.ticketItemPrice)
                        ticketItem.priceModified = true
                        ticketItem.approvalType = "Discount Ticket: ${discount.discountName}"
                        ticketItem.approvalId = approvalId
                        ticketItem.tax = 0.00

                    }else{
                        ticketItem.discountPrice = ticketItem.ticketItemPrice.minus(discountLeft).round(2)
                        discountLeft = 0.00
                        ticketItem.priceModified = true
                        ticketItem.approvalType = "Discount Ticket: ${discount.discountName}"
                        ticketItem.approvalId = approvalId
                        ticketItem.tax = ticketItem.discountPrice!!.times(taxRate)
                    }
                }
                statusApproval = "Pending"
            }
        }

        if (discount.discountType == "Percentage"){

            activeTicket()?.ticketItems?.forEach { ti ->
                val dis = ti.ticketItemPrice * (discount.discountAmount.div(100)).round(2)
                disTotal = disTotal.plus(ti.ticketItemPrice.minus(dis))
                ti.discountPrice = ti.ticketItemPrice.minus(dis)
                ti.tax = ti.discountPrice!!.times(taxRate).round(2)
                ti.priceModified = true
                ti.approvalType = "Discount Ticket: ${discount.discountName}"
                ti.approvalId = approvalId
            }
            statusApproval = "Pending"
        }
        return disTotal
    }

    fun changeGiftItemAmount(amount: Double){
        this.tickets!!.get(0).ticketItems.get(0).itemPrice = amount
        this.tickets!!.get(0).ticketItems.get(0).ticketItemPrice = amount
        this.tickets!!.get(0).recalculateAfterApproval()
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
        var printerModel: String = ""
        if (printer.printerModel.contains("88")){
            printerModel = "TM_T88"
        }else{
            printerModel = printer.printerModel
        }
        val document = PrinterDriver.createDocument(
            DocumentSettings(), printerModel)
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

    fun clone(): Payment{
        val payment: String = Gson().toJson(this, Payment::class.java)
        return Gson().fromJson(payment, Payment::class.java)
    }
}

@Parcelize
data class Ticket(
    val orderId: String,
    val id: Int,
    var ticketItems: MutableList<TicketItem>,
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
    val taxRate: Double,
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
            val list = paymentList!!.filter { !it.canceled }
            paymentTotal = list.sumOf {it.ticketPaymentAmount}.round(2)
            partialPayment = partial
        }

        fun recalculateAfterApproval(){
            subTotal = ticketItems.sumOf { it.ticketItemPrice }.round(2)
            tax = subTotal.times(taxRate).round(2)
            total = subTotal.plus(tax).round(2)
        }

        fun addTip(tip: Double, taxRate: Double){
            activePayment()!!.gratuity = tip
//            this.gratuity = tip
            recalculateAfterApproval()
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

        fun removeTicketItem(item: OrderItem){
            val found = ticketItems.find { it.orderItemId == item.id }
            if (found != null){
                ticketItems.remove(found)
            }
        }

        fun getApprovedTotal(): Double{
            var total = 0.00
            for (ticketItem in ticketItems){
                val itemTotal = ticketItem.quantity.times(ticketItem.itemPrice).round(2)
                if (itemTotal != ticketItem.ticketItemPrice && ticketItem.ticketItemPrice == 0.00){
                    total = total.plus(itemTotal)
                }

                if (itemTotal != ticketItem.ticketItemPrice && ticketItem.ticketItemPrice != 0.00){
                    total = total.plus(itemTotal.minus(ticketItem.ticketItemPrice))
                }
            }
            return total
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
    var canceled: Boolean = false,
    var uiActive: Boolean
    ): Parcelable {

    }

@Parcelize
data class CreditCardTransaction(
    val ticketId: Int,
    var creditTotal: Double?,
    var captureTotal: String?,
    val refundTotal: Double?,
    val voidTotal: Double?,
    var creditTransaction: CayanTransaction?,
    var captureTransaction: CaptureResponse?,
    val refundTransaction: TransactionResponse45?,
    var voidTransaction: TransactionResponse45?,
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
    var itemPrice: Double,
    var discountPrice: Double?,
    var priceModified: Boolean,
    var approvalType: String?,
    var approvalId: String?,
    var uiApproved: Boolean = false,
    val itemMods: ArrayList<ModifierItem>,
    val salesCategory: String,
    var ticketItemPrice: Double,
    var tax: Double,
    var selected: Boolean = false
): Parcelable{
    fun approve(){
        ticketItemPrice = discountPrice!!
        discountPrice = null
        approvalType = null
    }

    fun reject(){
        discountPrice = null
        approvalType = null
        priceModified = false
    }


}

//@Parcelize
//data class ItemPrice(
//    var isSelected: Boolean = false,
//    var quantity: Int = 1,
//    val size: String,
//    var price: Double,
//    val discountPrice: Double?,
//    var modifiedPrice: Double = price,
//    val tax: String
//): Parcelable

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