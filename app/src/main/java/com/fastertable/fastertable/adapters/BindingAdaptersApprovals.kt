package com.fastertable.fastertable.adapters

import android.annotation.SuppressLint
import android.view.View
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fastertable.fastertable.R
import com.fastertable.fastertable.data.models.*
import com.fastertable.fastertable.utils.round


@SuppressLint("SetTextI18n")
@BindingAdapter("approvalHeader")
fun setApprovalNumber(textView: TextView, approval: Approval?){
//    approvals?.forEach {
//        if (it.uiActive){
//            if (it.approvalType == "Discount Ticket"){
//                textView.text = "${it.approvalType} - ${it.discount}"
//            }else{
//                textView.text = it.approvalType
//            }
//        }
//    }
    if (approval != null){
        textView.text = approval.approvalType
    }else{
        textView.text = ""
    }
}

@BindingAdapter("approvalItemListData")
fun bindApprovalItemTicketRecyclerView(recyclerView: RecyclerView?, approvalItems: List<ApprovalItem>?) {
    if (recyclerView != null){
        val adapter = recyclerView.adapter as ApprovalAdapter
        if (approvalItems != null){
            val approvalItem = approvalItems.find { it -> it.uiActive }
            if (approvalItem != null){
                val ticket = approvalItem.ticket
                adapter.submitList(ticket?.ticketItems)
                adapter.notifyDataSetChanged()
            }
        }
    }
}

@BindingAdapter("approvalItemDiscount")
fun getApprovalItemDiscount(textView: TextView, item: TicketItem){
    textView.text = textView.context.getString(R.string.item_price, "%.${2}f".format(item.discountPrice))
}

@BindingAdapter("approvalSubTotal")
fun getApprovalSubtotal(textView: TextView, item: ApprovalTicket?){
    if (item != null){
        if (item.approval.approvalType == "Void Ticket" || item.approval.approvalType == "Discount Ticket"){
            textView.text = textView.context.getString(R.string.subtotal_price, "%.${2}f".format(item.ticket.subTotal))
        }else{
            textView.text = textView.context.getString(R.string.subtotal_price, "%.${2}f".format(item.approval.newItemPrice))
        }
    }

}

@BindingAdapter("approvalTax")
fun getApprovalTax(textView: TextView, item: ApprovalTicket?){
    if (item != null) {
        if (item.approval.approvalType == "Void Ticket" || item.approval.approvalType == "Discount Ticket") {
            textView.text = textView.context.getString(
                R.string.subtotal_price,
                "%.${2}f".format(item.ticket.tax)
            )
        } else {
            val tax = item.approval.newItemPrice?.times(item.ticket.taxRate)!!.round(2)
            textView.text = textView.context.getString(R.string.subtotal_price, "%.${2}f".format(tax))
        }
    }
//    if (item != null){
//        textView.text = textView.context.getString(R.string.subtotal_price, "%.${2}f".format(item.ticketSalesTax()))
//    }
}

@BindingAdapter("approvalTotal")
fun getApprovalTotal(textView: TextView, item: ApprovalTicket?){
    if (item != null) {
        if (item.approval.approvalType == "Void Ticket" || item.approval.approvalType == "Discount Ticket") {
            textView.text = textView.context.getString(
                R.string.subtotal_price,
                "%.${2}f".format(item.ticket.total)
            )
        } else {
            val tax = item.approval.newItemPrice?.times(item.ticket.taxRate)
            val total = item.approval.newItemPrice?.plus(tax!!)!!.round(2)
            textView.text = textView.context.getString(
                R.string.subtotal_price,
                "%.${2}f".format(total))
        }
    }
//    if (item != null){
//        textView.text = textView.context.getString(R.string.subtotal_price, "%.${2}f".format(item.ticketTotal()))
//    }
}

@BindingAdapter("approvalDiscount")
fun getApprovalDiscount(textView: TextView, item: Approval?){
        if (item != null){
//        if (item.ticket != null){
//            dis = item.ticket.subTotal.minus(item.totalDiscount())
//        }
//
//        if (item.ticketItem != null){
//            dis = item.ticketItem.ticketItemPrice.minus(item.totalDiscount())
//        }

        textView.setTextColor(textView.context.getColor(R.color.secondaryColor))
        textView.text = textView.context.getString(R.string.discount_total_price, "%.${2}f".format(item.newItemPrice))
    }
}

@BindingAdapter("approvalExplanation")
fun getApprovalExplanation(textView: TextView, approval: Approval?){
    var explain: String = ""
    if (approval != null){
        when (approval.approvalType){
            "Void Ticket" -> explain = "Void Ticket"
            "Discount Ticket" -> explain = "Discount Ticket - ${approval.discount}"
            "Void Item" -> explain = "Void Item"
            "Discount Item" -> explain = "Discount Item"
        }
        textView.text = explain
    }
}

@BindingAdapter("approvalEmployee")
fun getApprovalEmployee(textView: TextView, item: Payment?){
    if (item != null){
        textView.text = textView.context.getString(R.string.requested_by, item.userName)
    }
}

//@BindingAdapter("approvalEmployee")
//fun getApprovalEmployee(textView: TextView, item: Approval?){
//    if (item != null){
//        textView.text = textView.context.getString(R.string.requested_by, item.order.userName)
//    }
//}

@BindingAdapter("approvalCard")
fun showApprovalCard(cardView: CardView, approval: Approval?){
    if (approval != null){
        cardView.visibility = View.VISIBLE
    }else{
        cardView.visibility = View.GONE
    }
}

//@BindingAdapter("approvalCard")
//fun showApprovalCard(cardView: CardView, list: List<ApprovalItem>?){
//    if (list != null && list.isNotEmpty()){
//        cardView.visibility = View.VISIBLE
//    }else{
//        cardView.visibility = View.GONE
//    }
//}
