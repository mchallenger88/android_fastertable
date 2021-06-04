package com.fastertable.fastertable.adapters

import android.content.res.ColorStateList
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fastertable.fastertable.R
import com.fastertable.fastertable.data.models.Payment
import com.fastertable.fastertable.data.models.Ticket
import com.fastertable.fastertable.data.models.TicketItem

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
    payment?.tickets?.forEach { item ->
        if (item.uiActive){
            textView.text = textView.context.getString(R.string.subtotal_price, "%.${2}f".format(item.subTotal))
        }
    }
}

@BindingAdapter("ticketTax")
fun getTicketTax(textView: TextView, payment: Payment?){
    payment?.tickets?.forEach { item ->
        if (item.uiActive){
            textView.text = textView.context.getString(R.string.tax_price, "%.${2}f".format(item.tax))
        }
    }
}

@BindingAdapter("ticketTotal")
fun getTicketTotal(textView: TextView, payment: Payment?){
    payment?.tickets?.forEach { item ->
        if (item.uiActive){
            textView.text = textView.context.getString(R.string.total_price, "%.${2}f".format(item.total))
        }
    }
}

@BindingAdapter("amountOwed")
fun getAmountOwed(textView: TextView, payment: Payment?){
    payment?.tickets?.forEach { item ->
        if (item.uiActive){
            textView.text = textView.context.getString(R.string.amount_owed, "%.${2}f".format(item.total))
        }
    }
}

@BindingAdapter("paidInFull")
fun setPaidInFull(textView: TextView, payment: Payment){
    payment.tickets.forEach { item ->
        if (item.uiActive){
            if (item.paymentTotal!! >= item.total){
                textView.visibility = View.VISIBLE
            }else{
                textView.visibility = View.GONE
            }
        }
    }
}

@BindingAdapter("approvalPending")
fun showApprovalPending(textView: TextView, payment: Payment){
    if (payment.statusApproval == "Pending"){
        textView.visibility = View.VISIBLE
    }else{
        textView.visibility = View.GONE
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

