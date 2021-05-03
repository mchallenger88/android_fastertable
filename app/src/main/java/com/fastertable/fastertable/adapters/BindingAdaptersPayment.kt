package com.fastertable.fastertable.adapters

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fastertable.fastertable.data.models.Payment

@BindingAdapter("ticketsData")
fun bindTicketsRecyclerView(recyclerView: RecyclerView?, payment: Payment?) {
    val adapter = recyclerView?.adapter as TicketSideBarAdapter
    if (payment != null) {
        adapter.submitList(payment.tickets)
        adapter.notifyDataSetChanged()
    }
}