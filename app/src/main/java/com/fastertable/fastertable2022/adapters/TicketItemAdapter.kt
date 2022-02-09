package com.fastertable.fastertable2022.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fastertable.fastertable2022.R
import com.fastertable.fastertable2022.data.models.TicketItem
import com.fastertable.fastertable2022.databinding.TicketLineItemBinding
import com.fastertable.fastertable2022.ui.dialogs.DialogListener

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

            binding.executePendingBindings()
            val typeface = ResourcesCompat.getFont(parent.context, R.font.open_sans)
            binding.txtItem.typeface = typeface
            binding.txtQuantity.typeface = typeface
            binding.txtPrice.typeface = typeface
            binding.txtMods.typeface = typeface

            if (ticketItem.split){
                binding.btnTicketItemMore.visibility = View.INVISIBLE
            }else{
                binding.btnTicketItemMore.visibility = View.VISIBLE
            }

            binding.btnTicketItemMore.setOnClickListener {
                clickListener.onClick(ticketItem)
            }

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