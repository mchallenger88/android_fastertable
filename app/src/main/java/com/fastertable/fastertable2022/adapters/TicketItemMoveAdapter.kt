package com.fastertable.fastertable2022.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fastertable.fastertable2022.R
import com.fastertable.fastertable2022.data.models.TicketItem
import com.fastertable.fastertable2022.databinding.TicketLineItemMoveBinding

class TicketItemMoveAdapter(private val addListener: TicketItemMoveListener) :
    ListAdapter<TicketItem, TicketItemMoveAdapter.TicketItemMoveViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TicketItemMoveViewHolder {
        return TicketItemMoveViewHolder(TicketLineItemMoveBinding.inflate(LayoutInflater.from(parent.context)), parent)
    }

    override fun onBindViewHolder(holder: TicketItemMoveViewHolder, position: Int) {
        val ticketItem = getItem(position)
        holder.bind(ticketItem, addListener)
    }

    class TicketItemMoveViewHolder(private var binding: TicketLineItemMoveBinding, private val parent:ViewGroup):
        RecyclerView.ViewHolder(binding.root) {
        fun bind(ticketItem: TicketItem, addListener: TicketItemMoveListener) {
            binding.ticketItem = null
            binding.ticketItem = ticketItem
            binding.executePendingBindings()
            val typeface = ResourcesCompat.getFont(parent.context, R.font.open_sans)
            binding.txtMoveItem.typeface = typeface
            binding.txtMoveQuantity.typeface = typeface
            binding.txtMovePrice.typeface = typeface
            binding.txtMoveMods.typeface = typeface

            binding.layoutItemMove.setOnClickListener {
                ticketItem.selected = !ticketItem.selected
                addListener.onClick(ticketItem)
            }

        }
    }

    class TicketItemMoveListener(val clickListener: (item: TicketItem) -> Unit) {
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


}