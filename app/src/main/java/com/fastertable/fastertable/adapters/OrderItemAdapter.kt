package com.fastertable.fastertable.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fastertable.fastertable.data.models.OrderItem
import com.fastertable.fastertable.databinding.OrderLineItemBinding

class OrderItemAdapter : ListAdapter<OrderItem, OrderItemAdapter.MenuItemViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuItemViewHolder {
        return MenuItemViewHolder(OrderLineItemBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: MenuItemViewHolder, position: Int) {
        val orderItem = getItem(position)
        holder.bind(orderItem)
    }
    class MenuItemViewHolder(private var binding: OrderLineItemBinding):
        RecyclerView.ViewHolder(binding.root) {
        fun bind(orderItem: OrderItem) {
            binding.orderItem = orderItem
            binding.executePendingBindings()
            var mods: String = String()
            orderItem.orderMods?.forEach { mod ->
                mods += mod.itemName + ", "
            }

            var ingredients: String = String()
            orderItem.ingredients?.forEach{ ing ->
                if (ing.orderValue == 0){
                    ingredients += "Remove $ing.name, "
                }
                if (ing.orderValue == 2){
                    ingredients += "Extra $ing.name, "
                }

            }

            if (mods == ""){
                binding.txtMods.visibility = View.GONE
            }else{
                mods = "- $mods"
                mods = mods.dropLast(2)
                binding.txtMods.text = mods
            }

            if (ingredients == ""){
                binding.txtIngredients.visibility = View.GONE
            }else{
                ingredients = ingredients.dropLast(2)
                ingredients = "- $ingredients"
                binding.txtIngredients.text = ingredients
            }
        }
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