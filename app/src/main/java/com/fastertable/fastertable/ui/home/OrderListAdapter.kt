package com.fastertable.fastertable.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fastertable.fastertable.data.models.Order
import com.fastertable.fastertable.databinding.OrderListLineItemBinding


class OrderListAdapter(val clickListener: OrderListListener): ListAdapter<Order, OrderListAdapter.OrderViewHolder>(
    DiffCallback
) {

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = getItem(position)
        holder.bind(order, clickListener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        return OrderViewHolder(OrderListLineItemBinding.inflate(LayoutInflater.from(parent.context)))
    }


    class OrderViewHolder(private var binding: OrderListLineItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(order: Order, clickListener: OrderListListener) {
            binding.order = order
            binding.clickListener = clickListener
            binding.executePendingBindings()

            binding.root.setOnClickListener{
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

}

class OrderListListener(val clickListener: (id: String) -> Unit) {
    fun onClick(order: Order) = clickListener(order.id)
}