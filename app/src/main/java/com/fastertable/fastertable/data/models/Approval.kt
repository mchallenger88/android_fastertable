package com.fastertable.fastertable.data.models

import android.os.Parcelable
import com.fastertable.fastertable.utils.round
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class TestApproval(
    val id: String,
    val order: Order,
    val approvalItems: ArrayList<ApprovalItem>,
    val timeRequested: Long,
    val type: String,
    @SerializedName("locationid")
    val locationId: String,
    val archived: Boolean,
    val _rid: String?,
    val _self: String?,
    val _etag: String?,
    val _attachments: String?,
    val _ts: Long?
): Parcelable {
    fun getPending(): List<ApprovalItem>{
        val list = arrayListOf<ApprovalItem>()
        approvalItems.forEach { it ->
            if (it.approved == null){
                list.add(it)
            }
        }
        return list
    }

    fun getComplete(): List<ApprovalItem>{
        val list = arrayListOf<ApprovalItem>()
        approvalItems.forEach { it ->
            if (it.approved != null){
                list.add(it)
            }
        }
        return list
    }
}

@Parcelize
data class Approval(
    val id: String,
    val approvalType: String,
    val ticketId: Int,
    val ticketItemId: Int?,
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
        var price: Double = 0.00
//        ticket?.ticketItems?.forEach{ticketItem ->
//            if (ticketItem.discountPrice != null){
//                price += price.plus(ticketItem.discountPrice!!)
//            }else{
//                price += price.plus(ticketItem.ticketItemPrice)
//            }
//        }
        return ticket!!.subTotal
    }
    fun ticketSalesTax(): Double{
        var tax: Double = 0.00
        ticket?.ticketItems?.forEach{ticketItem ->
            tax += tax.plus(ticketItem.tax)
        }
        return ticket!!.tax
    }
    fun ticketTotal(): Double{
//        return this.ticketSubtotal().plus(this.ticketSalesTax())
        return ticket!!.total
    }

    fun totalDiscount(): Double{
        when (approvalType){
            "Void Ticket" -> {
                return ticketTotal()
            }
            "Discount Ticket" -> {
                return ticket?.ticketItems?.sumByDouble{ it -> it.discountPrice!!}!!.round(2)
            }
            "Void Item" -> {
                return ticketItem?.ticketItemPrice!!
            }
            "Modify Price" -> {
                return ticketItem?.ticketItemPrice?.minus(ticketItem?.discountPrice!!)!!.round(2)
            }
            "Discount Item" -> {
                return ticketItem?.ticketItemPrice?.minus(ticketItem?.discountPrice!!)!!.round(2)
            }
            else -> {return 0.00}
        }
    }
}