package com.fastertable.fastertable.data.models

import android.os.Parcelable
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
    val orderCloseTime: Long?,
    val orderType: String,
    val guestCount: Int,
    val splitType: String,
    val splitTicket: Int?,
    val employeeId: String,
    val userName: String,
    val tickets: ArrayList<Ticket>,
    val terminalId: String,
    val statusApproval: String?,
    val newApproval: Approval?,
    val closed: Boolean,
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
): Parcelable

@Parcelize
data class Ticket(
    val orderId: String,
    val id: Int,
    val ticketItems: ArrayList<TicketItem>,
    val subTotal: Double,
    val tax: Double,
    val total: Double,
    var paymentType: String,
    val gratuity: Double,
    val deliveryFee: Double,
    var paymentTotal: Double?,
    val stageResponse: ArrayList<StageResponse>,
    val creditCardTransactions: ArrayList<CreditCardTransaction>,
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
    }

@Parcelize
data class CreditCardTransaction(
    val ticketId: Int,
    val creditTotal: Double?,
    val captureTotal: Double?,
    val refundTotal: Double?,
    val voidTotal: Double?,
    val creditTransaction: CayanTransaction,
    val captureTransaction: TransactionResponse45,
    val refundTransaction: TransactionResponse45,
    val voidTransaction: TransactionResponse45,
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
    val discountPrice: Double?,
    val priceModified: Boolean,
    val itemMods: ArrayList<ModifierItem>,
    val salesCategory: String,
    val ticketItemPrice: Double,
    val tax: Double,
): Parcelable

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
data class ManagerApproval(
    val id: String,
    val order: Order,
    val approvalItem: ApprovalItem,
    val timeRequested: Long,
    val approved: Boolean?,
    val timeHandled: Long,
    val managerId: String,
    val locationId: String,
    val archived: Boolean,
    val type: String,
    val _rid: String?,
    val _self: String?,
    val _etag: String?,
    val _attachments: String?,
    val _ts: Long?

): Parcelable

@Parcelize
data class Approval(
    val id: String,
    val order: Order,
    val approvalItems: ArrayList<ApprovalItem>,
    val timeRequested: Long,
    val type: String,
    val _rid: String?,
    val _self: String?,
    val _etag: String?,
    val _attachments: String?,
    val _ts: Long?
): Parcelable

@Parcelize
data class ApprovalItem(
    val id: Int,
    val approvalType: String, //Price, Discount, Discount Ticket, Void Item, Void Order
    val discount: String,
    val ticketItem: TicketItem,
    val ticket: Ticket,
    val amount: Double,
    val timeRequested: Long,
    val approved: Boolean?,
    val timeHandled: Long,
    val managerId: String,
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
    val p: Payment,
    val t: Ticket
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