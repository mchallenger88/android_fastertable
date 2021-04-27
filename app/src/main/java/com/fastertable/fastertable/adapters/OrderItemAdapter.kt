package com.fastertable.fastertable.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fastertable.fastertable.data.models.IngredientChange
import com.fastertable.fastertable.data.models.Order
import com.fastertable.fastertable.data.models.OrderItem
import com.fastertable.fastertable.databinding.OrderLineItemBinding
import com.fastertable.fastertable.ui.dialogs.DialogListener
import com.fastertable.fastertable.ui.dialogs.ItemMoreBottomSheetDialog
import dagger.hilt.android.internal.managers.ViewComponentManager

class OrderItemAdapter(private val clickListener: OrderItemListener) : ListAdapter<OrderItem, OrderItemAdapter.MenuItemViewHolder>(DiffCallback), DialogListener {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuItemViewHolder {
        return MenuItemViewHolder(OrderLineItemBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: MenuItemViewHolder, position: Int) {
        val orderItem = getItem(position)
        holder.bind(orderItem, clickListener)
    }
    class MenuItemViewHolder(private var binding: OrderLineItemBinding):
        RecyclerView.ViewHolder(binding.root) {
        fun bind(orderItem: OrderItem, clickListener: OrderItemListener) {
            binding.orderItem = null
            binding.orderItem = orderItem
            binding.clickListener = clickListener
            binding.executePendingBindings()

            binding.btnItemMore.setOnClickListener{
                clickListener.onClick(orderItem)
            }

        }
    }

    class OrderItemListener(val clickListener: (item: OrderItem) -> Unit) {
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

    override fun returnValue(value: String) {
        println(value)
    }


}