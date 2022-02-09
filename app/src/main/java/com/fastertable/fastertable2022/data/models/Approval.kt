package com.fastertable.fastertable2022.data.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize



@Parcelize
data class Approval(
    val id: String,
    val approvalType: String,
    val ticketId: Int,
    val ticketItemId: Int?,
    val whoRequested: String,
    val timeRequested: Long,
    val newItemPrice: Double?,
    var approved: Boolean?,
    var timeHandled: Long?,
    var managerId: String?,
    val paymentId: String,
    var discount: String,
    @SerializedName("locationid")
    val locationId: String,
    val type: String,
    val _rid: String?,
    val _self: String?,
    val _etag: String?,
    val _attachments: String?,
    val _ts: Long?

): Parcelable{

}

@Parcelize
data class ApprovalOrderPayment(
    val approval: Approval,
    val order: Order,
    val payment: Payment
): Parcelable

@Parcelize
data class ApprovalTicket(
    val approval: Approval,
    val ticket: Ticket
): Parcelable

@Parcelize
data class ApprovalItem(
    val id: Int,
    val approvalType: String, //Price, Discount, Discount Ticket, Void Item, Void Ticket
    val discount: String?,
    val ticketItem: TicketItem?,
    val ticket: Ticket?,
    val amount: Double,
    val timeRequested: Long,
    var approved: Boolean?,
    var timeHandled: Long?,
    var managerId: String?,
    var uiActive: Boolean = false
): Parcelable {
    fun ticketSubtotal(): Double{
        var st = 0.00
        ticket?.let { it
         st = it.subTotal
        }
        return st
    }
    fun ticketSalesTax(): Double{
        var tax = 0.00
        ticket?.ticketItems?.forEach{ticketItem ->
            tax += tax.plus(ticketItem.tax)
        }
        return tax
    }
//    fun ticketTotal(): Double{
//        return ticket!!.total
//    }


}