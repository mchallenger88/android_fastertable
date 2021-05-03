package com.fastertable.fastertable.adapters

import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fastertable.fastertable.R
import com.fastertable.fastertable.data.models.Order
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
    println(payment)
    adapter.submitList(ticket?.ticketItems)
    adapter.notifyDataSetChanged()
}

@BindingAdapter("ticketItemPrice")
fun getItemPrice(textView: TextView, item: TicketItem){
    textView.text = "$%.${2}f".format(item.ticketItemPrice)
}

@BindingAdapter("ticketSubTotal")
fun getTicketSubtotal(textView: TextView, payment: Payment){
    payment.tickets.forEach { item ->
        if (item.uiActive){
            textView.text = "Subtotal:  $%.${2}f".format(item.subTotal)
        }
    }
}

@BindingAdapter("ticketTax")
fun getTicketTax(textView: TextView, payment: Payment){
    payment.tickets.forEach { item ->
        if (item.uiActive){
            textView.text = "Tax:  $%.${2}f".format(item.tax)
        }
    }
}

@BindingAdapter("ticketTotal")
fun getTicketTotal(textView: TextView, payment: Payment){
    payment.tickets.forEach { item ->
        if (item.uiActive){
            textView.text = "Total:  $%.${2}f".format(item.total)
        }
    }
}

