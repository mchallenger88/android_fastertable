package com.fastertable.fastertable.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fastertable.fastertable.R
import com.fastertable.fastertable.data.models.ApprovalItem
import com.fastertable.fastertable.data.models.TicketItem
import com.fastertable.fastertable.databinding.ApprovalLineItemBinding
import com.fastertable.fastertable.databinding.TicketLineItemBinding
import com.fastertable.fastertable.ui.dialogs.DialogListener

class ApprovalAdapter(private val clickListener: ApprovalListener) : ListAdapter<TicketItem, ApprovalAdapter.ApprovalItemViewHolder>(ApprovalAdapter), DialogListener {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApprovalItemViewHolder {
        return ApprovalItemViewHolder(ApprovalLineItemBinding.inflate(LayoutInflater.from(parent.context)), parent)
    }

    override fun onBindViewHolder(holder: ApprovalItemViewHolder, position: Int) {
        val ticketItem = getItem(position)
        holder.bind(ticketItem, clickListener)
    }
    class ApprovalItemViewHolder(private var binding: ApprovalLineItemBinding, private val parent:ViewGroup):
        RecyclerView.ViewHolder(binding.root) {
        fun bind(ticketItem: TicketItem, clickListener: ApprovalListener) {
            binding.ticketItem = null
            binding.ticketItem = ticketItem
            binding.executePendingBindings()

            val typeface = ResourcesCompat.getFont(parent.context, R.font.open_sans)
            binding.txtApprovalItem.typeface = typeface
            binding.txtApprovalItemPrice.typeface = typeface
            binding.txtApprovalItemQuantity.typeface = typeface
            binding.txtItemDiscount.typeface = typeface

        }
    }

    class ApprovalListener(val clickListener: (item: TicketItem) -> Unit) {
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
        TODO("Not yet implemented")
    }
}