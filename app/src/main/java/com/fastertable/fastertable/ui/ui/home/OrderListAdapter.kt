package com.fastertable.fastertable.ui.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fastertable.fastertable.R
import com.fastertable.fastertable.data.Order
import com.fastertable.fastertable.databinding.OrderListLineItemBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class OrderListAdapter(val clickListener: OrderListListener): ListAdapter<Order, OrderListAdapter.OrderViewHolder>(DiffCallback) {
    private val adapterScope = CoroutineScope(Dispatchers.Default)


    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = getItem(position)
        holder.bind(order)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderListAdapter.OrderViewHolder {
        return OrderViewHolder(OrderListLineItemBinding.inflate(LayoutInflater.from(parent.context)))
    }


    class OrderViewHolder(private var binding: OrderListLineItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(order: Order) {
            binding.order = order
            binding.executePendingBindings()
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