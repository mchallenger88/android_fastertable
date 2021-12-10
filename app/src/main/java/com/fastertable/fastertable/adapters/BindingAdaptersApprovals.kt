package com.fastertable.fastertable.adapters

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.util.Log
import android.view.View
import android.widget.Switch
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fastertable.fastertable.R
import com.fastertable.fastertable.data.models.*
import com.fastertable.fastertable.utils.round
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.switchmaterial.SwitchMaterial


@SuppressLint("SetTextI18n")
@BindingAdapter("approvalHeader")
fun setApprovalNumber(textView: TextView, approval: ApprovalOrderPayment?){
    if (approval != null){
        if (approval.order.tableNumber == null){
            textView.text = "Order # ${approval.order.orderNumber.toString()}"
        }else{
            textView.text = "Table # ${approval.order.tableNumber.toString()}"
        }

    }else{
        textView.text = ""
    }
}

@SuppressLint("SetTextI18n")
@BindingAdapter("approvalItemQuantity")
fun approvalItemQuantity(textView: TextView, item: TicketItem?){
    if (item != null){
        textView.text = "${item.quantity}" + "x"
    }

}

@BindingAdapter("approvalItemDiscount")
fun getApprovalItemDiscount(textView: TextView, item: TicketItem?){
    if (item != null){
        if (item.discountPrice != null){
            textView.text = textView.context.getString(R.string.item_price, "%.${2}f".format(item.discountPrice))
        }else{
            textView.text = ""
        }
    }
}

@BindingAdapter("showSwitch")
fun showSwitch(switch: SwitchMaterial, item: TicketItem?){
    if (item != null){
        if (item.discountPrice != null){
            switch.visibility = View.VISIBLE
        }else{
            switch.visibility = View.GONE
        }
    }
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
            val tax = item.approval.newItemPrice?.times(item.ticket.taxRate)?.round(2)
            textView.text = textView.context.getString(R.string.subtotal_price, "%.${2}f".format(tax))
        }
    }

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
            tax?.let {
                val total = item.approval.newItemPrice.plus(tax).round(2)
                textView.text = textView.context.getString(
                    R.string.subtotal_price,
                    "%.${2}f".format(total))
            }
        }
    }

}

@BindingAdapter("approvalDiscount")
fun getApprovalDiscount(textView: TextView, item: Double?){
        if (item != null){
            textView.setTextColor(textView.context.getColor(R.color.secondaryColor))
            textView.text = textView.context.getString(R.string.discount_total_price, "%.${2}f".format(item))
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
fun getApprovalEmployee(textView: TextView, item: Approval?){
    if (item != null){
        textView.text = textView.context.getString(R.string.requested_by, item.whoRequested)
    }
}

@BindingAdapter("disableApprovalSave")
fun disableApprovalSave(button: FloatingActionButton, item: Approval?){
    button.isEnabled = item?.timeHandled == null
}

@BindingAdapter("approvalCard")
fun approvalCard(cardView: CardView, approval: Approval?){
    if (approval != null) {
        cardView.visibility = View.VISIBLE
    }else{
        cardView.visibility = View.GONE
    }
}

@BindingAdapter("showApproveAllSwitch")
fun showApproveAllSwitch(switch: SwitchMaterial, approval: Approval?){
    if (approval?.timeHandled == null){
        switch.visibility = View.VISIBLE
    }else{
        switch.visibility = View.GONE
    }
}

@BindingAdapter("showApprovedRejected")
fun showApprovedRejected(textView: TextView, approval: Approval?){
    if (approval?.timeHandled != null){
        textView.visibility = View.VISIBLE
        if (approval.approved == true){
            textView.text = textView.context.getString(R.string.approved)
        }else{
            textView.text = textView.context.getString(R.string.rejected)
        }
    }else{
        textView.visibility = View.GONE
    }
}

@BindingAdapter("showApprovalCard")
fun showApprovalCard(recylcer: RecyclerView, b: Boolean){
    if (b){
        recylcer.visibility = View.VISIBLE
    }else{
        recylcer.visibility = View.GONE
    }
}

@BindingAdapter("showClosedLayout")
fun showClosedLayout(layout: ConstraintLayout, b: Boolean){
    if (!b){
        layout.visibility = View.VISIBLE
    }else{
        layout.visibility = View.GONE
    }
}

@BindingAdapter("showApprovalSideCard")
fun showApprovalSideCard(card: CardView, approval: Approval?){
    if (approval != null) {
        if (approval.timeHandled == null){
            card.visibility = View.VISIBLE
        }else{
            card.visibility = View.GONE
        }
    }else{
        card.visibility = View.GONE
    }
}

@BindingAdapter("enableApprove")
fun enableApprove(btn: FloatingActionButton, b: Boolean){
    btn.isEnabled = b
}