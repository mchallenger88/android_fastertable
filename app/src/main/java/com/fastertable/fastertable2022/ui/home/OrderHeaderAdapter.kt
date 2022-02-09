package com.fastertable.fastertable2022.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fastertable.fastertable2022.data.models.Order
import com.fastertable.fastertable2022.databinding.OrderListLineHeaderBinding

class OrderHeaderAdapter: ListAdapter<Order, OrderHeaderAdapter.HeaderViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeaderViewHolder {
        return HeaderViewHolder(OrderListLineHeaderBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: HeaderViewHolder, position: Int) {
        val order = getItem(position)
        holder.bind(order)
    }

    class HeaderViewHolder(private var binding: OrderListLineHeaderBinding):
        RecyclerView.ViewHolder(binding.root) {
            fun bind(order: Order){
                binding.order = order
                binding.executePendingBindings()
            }
        }

    override fun getItemCount(): Int {
        return 1
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