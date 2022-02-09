package com.fastertable.fastertable2022.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fastertable.fastertable2022.R
import com.fastertable.fastertable2022.data.models.OrderItem
import com.fastertable.fastertable2022.databinding.TransferOrderLineItemBinding

class OrderItemTransferAdapter(private val addListener: TransferAddListener, private val removeListener: TransferRemoveListener) :
    ListAdapter<OrderItem, OrderItemTransferAdapter.MenuItemViewHolder>(DiffCallback)  {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuItemViewHolder {
        return MenuItemViewHolder(
            TransferOrderLineItemBinding.inflate(
                LayoutInflater.from(
                    parent.context
                )
            ), parent
        )
    }

    override fun onBindViewHolder(holder: MenuItemViewHolder, position: Int) {
        val orderItem = getItem(position)
        holder.bind(orderItem, addListener, removeListener)
    }

    class MenuItemViewHolder(private var binding: TransferOrderLineItemBinding, private val parent: ViewGroup):
        RecyclerView.ViewHolder(binding.root) {
        fun bind(orderItem: OrderItem, addListener: TransferAddListener, removeListener: TransferRemoveListener) {
            binding.item = null
            binding.item = orderItem

            binding.executePendingBindings()


            val typeface = ResourcesCompat.getFont(parent.context, R.font.open_sans_semibold)
            binding.txtTransferItem.typeface = typeface
            binding.txtTransferQuantity.typeface = typeface

            binding.switchTransferItem.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked){
                    addListener.onClick(orderItem)
                }else{
                    removeListener.onClick(orderItem)
                }
            }
        }
    }

    class TransferAddListener(val clickListener: (item: OrderItem) -> Unit) {
        fun onClick(item: OrderItem) = clickListener(item)
    }

    class TransferRemoveListener(val clickListener: (item: OrderItem) -> Unit) {
        fun onClick(item: OrderItem) = clickListener(item)
    }

    companion object DiffCallback : DiffUtil.ItemCallback<OrderItem>() {
        override fun areItemsTheSame(oldItem: OrderItem, newItem: OrderItem): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: OrderItem, newItem: OrderItem): Boolean {
            return oldItem.id == newItem.id
        }
    }
}