package com.fastertable.fastertable.adapters

import android.content.res.ColorStateList
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fastertable.fastertable.R
import com.fastertable.fastertable.data.models.Payment
import com.fastertable.fastertable.data.models.Ticket
import com.fastertable.fastertable.data.models.TicketItem
import com.fastertable.fastertable.ui.payment.ShowCreditPayment

@BindingAdapter("ticketsData")
fun bindTicketsRecyclerView(recyclerView: RecyclerView?, payment: Payment?) {
    val adapter = recyclerView?.adapter as TicketSideBarAdapter
    if (payment != null) {
        adapter.submitList(payment.tickets)
        adapter.notifyDataSetChanged()
    }
}

@BindingAdapter("ticketNumber")
fun setTicketNumber(textView: TextView, tickets: List<Ticket>?){
    tickets?.forEach {
        if (it.uiActive){
            textView.text = textView.context.getString(R.string.ticket_with_number,  it.id.plus(1).toString())
        }
    }
}

@BindingAdapter("ticketListData")
fun bindTicketRecyclerView(recyclerView: RecyclerView?, payment: Payment?) {
    val adapter = recyclerView?.adapter as TicketItemAdapter
    val ticket = payment?.tickets?.find{ it -> it.uiActive }
    adapter.submitList(ticket?.ticketItems)
    adapter.notifyDataSetChanged()
}

@BindingAdapter("ticketItemPrice")
fun getItemPrice(textView: TextView, item: TicketItem){
    textView.text = textView.context.getString(R.string.item_price, "%.${2}f".format(item.ticketItemPrice))
}

@BindingAdapter("ticketSubTotal")
fun getTicketSubtotal(textView: TextView, payment: Payment?){
    if (payment != null){
        payment?.tickets?.forEach { item ->
            if (item.uiActive){
                textView.text = textView.context.getString(R.string.subtotal_price, "%.${2}f".format(item.subTotal))
            }
        }
    }else{
        textView.text = ""
    }
}

@BindingAdapter("splitGuestEnabled")
fun splitGuestEnabled(btn: Button, payment: Payment?){
    if (payment != null){
        btn.isEnabled = payment.splitType != "Evenly"
        if (!btn.isEnabled){
            val offWhite = ContextCompat.getColor(btn.context, R.color.offWhite)
            btn.setTextColor(ColorStateList.valueOf(offWhite))
        }else{
            val white = ContextCompat.getColor(btn.context, R.color.white)
            btn.setTextColor(ColorStateList.valueOf(white))
        }
    }
}

@BindingAdapter("splitEvenEnabled")
fun splitEvenEnabled(btn: Button, payment: Payment?){
    if (payment != null){
        btn.isEnabled = payment.splitType != "Guest"
        if (!btn.isEnabled){
            val offWhite = ContextCompat.getColor(btn.context, R.color.offWhite)
            btn.setTextColor(ColorStateList.valueOf(offWhite))
        }else{
            val white = ContextCompat.getColor(btn.context, R.color.white)
            btn.setTextColor(ColorStateList.valueOf(white))
        }
    }
}

@BindingAdapter("ticketTax")
fun getTicketTax(textView: TextView, payment: Payment?){
    if (payment != null){
        payment?.tickets?.forEach { item ->
            if (item.uiActive){
                textView.text = textView.context.getString(R.string.tax_price, "%.${2}f".format(item.tax))
            }
        }
    }else{
        textView.text = ""
    }
}

@BindingAdapter("ticketGratuity")
fun ticketGratuity(textView: TextView, payment: Payment?){
    if (payment != null){
        payment.tickets!!.forEach { item ->
            if (item.uiActive){
                if (item.allGratuities() != null){
                    textView.text = textView.context.getString(R.string.tax_price, "%.${2}f".format(item.allGratuities()))
                    textView.visibility = View.VISIBLE
                }else{
                    textView.visibility = View.GONE
                }
            }
        }
    }else{
        textView.visibility = View.GONE
    }

}

@BindingAdapter("ticketGratuityText")
fun ticketGratuityText(textView: TextView, payment: Payment?){
    if (payment != null){
        payment?.tickets?.forEach { item ->
            if (item.uiActive){
                if (item.allGratuities() != null){
                    textView.visibility = View.VISIBLE
                }else{
                    textView.visibility = View.GONE
                }
            }
        }
    }else{
        textView.visibility = View.GONE
    }
}

@BindingAdapter("ticketTotal")
fun getTicketTotal(textView: TextView, payment: Payment?){
    if (payment != null){
        payment.tickets!!.forEach { item ->
            if (item.uiActive){
                textView.text = textView.context.getString(R.string.total_price, "%.${2}f".format(item.ticketTotal()))
            }
        }
    }else{
        textView.text = ""
    }
}

@BindingAdapter("partialPaymentText")
fun partialPaymentText(textView: TextView, payment: Payment?){
    if (payment != null){
        payment.tickets!!.forEach { item ->
            if (item.uiActive && item.partialPayment){
                textView.visibility = View.VISIBLE
            }else{
                textView.visibility = View.GONE
            }
        }
    }
}

@BindingAdapter("partialPaymentAmount")
fun partialPaymentAmount(textView: TextView, payment: Payment?){
    if (payment != null){
        payment.tickets!!.forEach { item ->
            if (item.uiActive && item.partialPayment){
                textView.text = textView.context.getString(R.string.total_price, "%.${2}f".format(item.paymentTotal))
                textView.visibility = View.VISIBLE
            }else{
                textView.visibility = View.GONE
            }
        }
    }
}

@BindingAdapter("amountOwed")
fun getAmountOwed(textView: TextView, payment: Payment?){
    if (payment != null){
        payment.tickets!!.forEach { item ->
            if (item.uiActive){
                val owed = item.total.minus(item.paymentTotal)
                textView.text = textView.context.getString(R.string.amount_owed, "%.${2}f".format(owed))
            }
        }
    }else{
        textView.text = textView.context.getString(R.string.amount_owed_empty)
    }

}

@BindingAdapter("paidInFull")
fun setPaidInFull(textView: TextView, payment: Payment?){
    if (payment != null){
        payment.tickets!!.forEach { item ->
            if (item.uiActive){
                if (item.paymentTotal >= item.total){
                    textView.visibility = View.VISIBLE
                }else{
                    textView.visibility = View.GONE
                }
            }
        }
    }else{
        textView.visibility = View.GONE
    }
}

@BindingAdapter("approvalPending")
fun showApprovalPending(textView: TextView, payment: Payment?){
    if (payment != null){
        if (payment.statusApproval == "Pending"){
            textView.visibility = View.VISIBLE
        }else{
            textView.visibility = View.GONE
        }
    }

}

@BindingAdapter("disablePayButton")
fun disablePayButton(button: Button, value: String?){
    if (value != null){
        button.isEnabled = value != "Pending"
        if (value == "Pending"){
            button.isEnabled = false
            val offWhite = ContextCompat.getColor(button.context, R.color.offWhite)
            button.setTextColor(ColorStateList.valueOf(offWhite))
        }else{
            button.isEnabled = true
            val white = ContextCompat.getColor(button.context, R.color.white)
            button.setTextColor(ColorStateList.valueOf(white))
        }
    }
}

@BindingAdapter("hideItemMore")
fun hideItemMore(imageButton: ImageButton, payment: Payment?){
    if (payment != null && payment.closed){
        imageButton.visibility = View.GONE
    }
}

@BindingAdapter("showCreditPayment")
fun showCreditPayment(button: Button, scp: ShowCreditPayment){
    if (scp == ShowCreditPayment.DEFAULT){
        button.visibility = View.VISIBLE
    }else{
        button.visibility = View.GONE
    }
}

@BindingAdapter("showPartialCreditPayment")
fun showPartialCreditPayment(layout: ConstraintLayout, scp: ShowCreditPayment){
    if (scp == ShowCreditPayment.PARTIAL){
        layout.visibility = View.VISIBLE
    }else{
        layout.visibility = View.GONE
    }
}

@BindingAdapter("showManualCredit")
fun showManualCredit(layout: ConstraintLayout, scp: ShowCreditPayment){
    if (scp == ShowCreditPayment.MANUAL){
        layout.visibility = View.VISIBLE
    }else{
        layout.visibility = View.GONE
    }
}

@BindingAdapter("showPriceModified")
fun showPriceModified(textView: TextView, item: TicketItem){
    if (item.priceModified){
        textView.visibility = View.VISIBLE
        textView.text = textView.context.getString(R.string.price_modified)
    }else{
        textView.visibility = View.GONE
    }
}