package com.fastertable.fastertable2022.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fastertable.fastertable2022.R
import com.fastertable.fastertable2022.data.models.TicketItem
import com.fastertable.fastertable2022.databinding.TicketLineItemSplitBinding
import com.fastertable.fastertable2022.ui.dialogs.DialogListener

class SplitTicketItemAdapter() : ListAdapter<TicketItem, SplitTicketItemAdapter.TicketItemViewHolder>(DiffCallback),
    DialogListener {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TicketItemViewHolder {
        return TicketItemViewHolder(TicketLineItemSplitBinding.inflate(LayoutInflater.from(parent.context)), parent)
    }

    override fun onBindViewHolder(holder: TicketItemViewHolder, position: Int) {
        val ticketItem = getItem(position)
        holder.bind(ticketItem)
    }
    class TicketItemViewHolder(private var binding: TicketLineItemSplitBinding, private val parent: ViewGroup):
        RecyclerView.ViewHolder(binding.root) {
        fun bind(ticketItem: TicketItem) {
            binding.ticketItem = null
            binding.ticketItem = ticketItem

            binding.executePendingBindings()
            val typeface = ResourcesCompat.getFont(parent.context, R.font.open_sans)
            binding.txtSplitItem.typeface = typeface
            binding.txtSplitItem.typeface = typeface
            binding.txtSplitItem.typeface = typeface
            binding.txtSplitItem.typeface = typeface


        }
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