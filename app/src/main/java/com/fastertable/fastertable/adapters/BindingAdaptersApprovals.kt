package com.fastertable.fastertable.adapters

import android.annotation.SuppressLint
import android.view.View
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fastertable.fastertable.R
import com.fastertable.fastertable.data.models.*


@SuppressLint("SetTextI18n")
@BindingAdapter("approvalHeader")
fun setApprovalNumber(textView: TextView, approvals: List<ApprovalItem>?){
    approvals?.forEach {
        if (it.uiActive){
            if (it.approvalType == "Discount Ticket"){
                textView.text = "${it.approvalType} - ${it.discount}"
            }else{
                textView.text = it.approvalType
            }
        }
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
                adapter.submitList(ticket.ticketItems)
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
fun getApprovalSubtotal(textView: TextView, item: ApprovalItem?){
    if (item != null){
        textView.text = textView.context.getString(R.string.subtotal_price, "%.${2}f".format(item.ticketSubtotal()))
    }

}

@BindingAdapter("approvalTax")
fun getApprovalTax(textView: TextView, item: ApprovalItem?){
    if (item != null){
        textView.text = textView.context.getString(R.string.subtotal_price, "%.${2}f".format(item.ticketSalesTax()))
    }
}

@BindingAdapter("approvalTotal")
fun getApprovalTotal(textView: TextView, item: ApprovalItem?){
    if (item != null){
        textView.text = textView.context.getString(R.string.subtotal_price, "%.${2}f".format(item.ticketTotal()))
    }
}

@BindingAdapter("approvalDiscount")
fun getApprovalDiscount(textView: TextView, item: ApprovalItem?){
    if (item != null){
        val dis = item.ticket.subTotal - item.totalDiscount()
        textView.setTextColor(textView.context.getColor(R.color.secondaryColor))
        textView.text = textView.context.getString(R.string.discount_total_price, "%.${2}f".format(dis))
    }
}

@BindingAdapter("approvalExplanation")
fun getApprovalExplanation(textView: TextView, item: ApprovalItem?){
    var explain: String = ""
    if (item != null){
        when (item.approvalType){
            "Void Ticket" -> explain = "Void Ticket"
            "Discount Ticket" -> explain = "Discount Ticket - ${item.discount}"
            "Void Item" -> explain = "Void Item"
            "Discount Item" -> explain = "Discount Item"
        }
        textView.text = explain
    }
}

@BindingAdapter("approvalEmployee")
fun getApprovalEmployee(textView: TextView, item: Approval?){
    if (item != null){
        textView.text = textView.context.getString(R.string.requested_by, item.order.userName)
    }
}

@BindingAdapter("approvalCard")
fun showApprovalCard(cardView: CardView, list: List<ApprovalItem>?){
    if (list != null && list.isNotEmpty()){
        cardView.visibility = View.VISIBLE
    }else{
        cardView.visibility = View.GONE
    }
}
