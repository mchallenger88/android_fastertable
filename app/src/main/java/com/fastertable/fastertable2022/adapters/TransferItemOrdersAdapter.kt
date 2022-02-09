package com.fastertable.fastertable2022.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fastertable.fastertable2022.data.models.Order
import com.fastertable.fastertable2022.databinding.TransferItemOrdersLineItemBinding

class TransferItemOrdersAdapter(val clickListener: TransferOrdersListListener): ListAdapter<Order, TransferItemOrdersAdapter.OrderViewHolder>(
    DiffCallback
) {

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = getItem(position)
        holder.bind(order, clickListener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        return OrderViewHolder(TransferItemOrdersLineItemBinding.inflate(LayoutInflater.from(parent.context)))
    }


    class OrderViewHolder(private var binding: TransferItemOrdersLineItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(order: Order, clickListener: TransferOrdersListListener) {
            binding.order = order
            binding.executePendingBindings()

            binding.constraintTransferOrder.setOnClickListener{
                clickListener.onClick(order)
            }
        }

    }

    companion object DiffCallback : DiffUtil.ItemCallback<Order>() {
        override fun areItemsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem.id == newItem.id
        }
    }

    class TransferOrdersListListener(val clickListener: (order: Order) -> Unit) {
        fun onClick(order: Order) = clickListener(order)
    }

}

