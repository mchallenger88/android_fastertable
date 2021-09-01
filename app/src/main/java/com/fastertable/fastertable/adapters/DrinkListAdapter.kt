package com.fastertable.fastertable.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.fastertable.fastertable.data.models.ReorderDrink
import com.fastertable.fastertable.databinding.DrinkLineItemBinding

class DrinkListAdapter: ListAdapter<ReorderDrink, DrinkListAdapter.DrinkItemViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DrinkItemViewHolder {
        return DrinkItemViewHolder(
            DrinkLineItemBinding.inflate(
                LayoutInflater.from(
                    parent.context
                )
            ), parent
        )
    }

    override fun onBindViewHolder(holder: DrinkItemViewHolder, position: Int) {
        val drink = getItem(position)
        holder.bind(drink)
    }

    class DrinkItemViewHolder(private var binding: DrinkLineItemBinding, private val parent: ViewGroup): RecyclerView.ViewHolder(binding.root) {
        fun bind(drink: ReorderDrink){
            binding.drink = null
            binding.drink = drink
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<ReorderDrink>() {
        override fun areItemsTheSame(oldItem: ReorderDrink, newItem: ReorderDrink): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: ReorderDrink, newItem: ReorderDrink): Boolean {
            return oldItem.guestId == newItem.guestId
        }
    }
}