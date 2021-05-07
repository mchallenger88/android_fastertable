package com.fastertable.fastertable.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fastertable.fastertable.R
import com.fastertable.fastertable.data.models.TicketItem
import com.fastertable.fastertable.databinding.TicketLineItemBinding
import com.fastertable.fastertable.ui.dialogs.DialogListener

class TicketItemAdapter(private val clickListener: TicketItemListener) : ListAdapter<TicketItem, TicketItemAdapter.TicketItemViewHolder>(DiffCallback), DialogListener {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TicketItemViewHolder {
        return TicketItemViewHolder(TicketLineItemBinding.inflate(LayoutInflater.from(parent.context)), parent)
    }

    override fun onBindViewHolder(holder: TicketItemViewHolder, position: Int) {
        val ticketItem = getItem(position)
        holder.bind(ticketItem, clickListener)
    }
    class TicketItemViewHolder(private var binding: TicketLineItemBinding, private val parent:ViewGroup):
            RecyclerView.ViewHolder(binding.root) {
        fun bind(ticketItem: TicketItem, clickListener: TicketItemListener) {
            binding.ticketItem = null
            binding.ticketItem = ticketItem
            binding.clickListener = clickListener
            binding.executePendingBindings()

            val typeface = ResourcesCompat.getFont(parent.context, R.font.open_sans)
            binding.txtItem.typeface = typeface
            binding.txtQuantity.typeface = typeface
            binding.txtPrice.typeface = typeface
            binding.txtMods.typeface = typeface

        }
    }

    class TicketItemListener(val clickListener: (item: TicketItem) -> Unit) {
        fun onClick(item: TicketItem) = clickListener(item)
    }

    companion object DiffCallback : DiffUtil.ItemCallback<TicketItem>() {
        override fun areItemsTheSame(oldItem: TicketItem, newItem: TicketItem): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: TicketItem, newItem: TicketItem): Boolean {
            return oldItem.id == newItem.id
        }
    }

    override fun returnValue(value: String) {
        println(value)
    }
}