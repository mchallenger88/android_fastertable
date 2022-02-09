package com.fastertable.fastertable2022.adapters

import android.content.res.ColorStateList
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fastertable.fastertable2022.R
import com.fastertable.fastertable2022.data.models.Ticket
import com.fastertable.fastertable2022.databinding.TicketButtonBinding

class TicketSideBarAdapter(private val clickListener: TicketSideBarListener) : ListAdapter<Ticket, TicketSideBarAdapter.TicketSideBarViewHolder>(TicketSideBarAdapter) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TicketSideBarViewHolder {
        return TicketSideBarViewHolder(TicketButtonBinding.inflate(LayoutInflater.from(parent.context)), parent)
    }

    override fun onBindViewHolder(holder: TicketSideBarAdapter.TicketSideBarViewHolder, position: Int) {
        val orderItem = getItem(position)
        holder.bind(orderItem, clickListener)
    }

    class TicketSideBarViewHolder(private var binding: TicketButtonBinding, private val parent: ViewGroup):
            RecyclerView.ViewHolder(binding.root) {
                fun bind(ticket: Ticket, clickListener: TicketSideBarListener){
                    binding.ticket = ticket
                    binding.clickListener = clickListener
                    binding.executePendingBindings()

                    val white = ContextCompat.getColor(parent.context, R.color.white)
                    val offwhite = ContextCompat.getColor(parent.context, R.color.offWhite)

                    binding.ticketButton.text = parent.context.getString(R.string.ticket_with_number, ticket.id.toString())
                    if (ticket.uiActive){
                        binding.ticketButton.setTextColor(ColorStateList.valueOf(white))
                        binding.ticketButton.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_receipt_white, 0, 0)
                    }else{
                        binding.ticketButton.setTextColor(ColorStateList.valueOf(offwhite))
                        binding.ticketButton.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_receipt_offwhite, 0, 0)
                    }
                    binding.ticketButton.textSize = 16f

                    binding.ticketButton.textAlignment = View.TEXT_ALIGNMENT_CENTER

                    val layoutParams = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    layoutParams.gravity = Gravity.CENTER_HORIZONTAL
                    layoutParams.setMargins(0, 40, 0, 0)
                    binding.ticketButton.setOnClickListener{
                        clickListener.onClick(ticket)
                    }
                }
            }

    class TicketSideBarListener(val clickListener: (item: Ticket) -> Unit) {
        fun onClick(item: Ticket) = clickListener(item)
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Ticket>() {
        override fun areItemsTheSame(oldItem: Ticket, newItem: Ticket): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: Ticket, newItem: Ticket): Boolean {
            return oldItem.id == newItem.id
        }
    }
}