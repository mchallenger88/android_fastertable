package com.fastertable.fastertable.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fastertable.fastertable.data.models.RestaurantFloorplan
import com.fastertable.fastertable.data.models.RestaurantTable
import com.fastertable.fastertable.databinding.FloorplanLineItemBinding
import com.fastertable.fastertable.ui.floorplan.FloorplanTable

class FloorplanAdapter(private val clickListener: FloorplanListener) : ListAdapter<RestaurantFloorplan, FloorplanAdapter.FloorplanViewHolder>(DiffCallback){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FloorplanViewHolder {
        return FloorplanViewHolder(
            FloorplanLineItemBinding.inflate(
                LayoutInflater.from(
                    parent.context
                )
            ), parent
        )
    }

    override fun onBindViewHolder(holder: FloorplanViewHolder, position: Int) {
        val orderItem = getItem(position)
        holder.bind(orderItem, clickListener)
    }

    class FloorplanViewHolder(private var binding: FloorplanLineItemBinding, private val parent: ViewGroup)
        : RecyclerView.ViewHolder(binding.root){
        fun bind(floorplan: RestaurantFloorplan, clickListener: FloorplanListener) {
            floorplan.tables.forEach { table ->
                val btnTable = FloorplanTable(parent.context)
                btnTable.id = ViewCompat.generateViewId()
                btnTable.loadTable(table)

                btnTable.setOnClickListener {
                    clickListener.onClick(table)
                }

                val currentLayout = ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.MATCH_PARENT,
                    ConstraintLayout.LayoutParams.MATCH_PARENT
                )
                currentLayout.marginStart = table.left
                currentLayout.topMargin = table.top
                btnTable.layoutParams = currentLayout
            }
        }
    }

    class FloorplanListener(val clickListener: (item: RestaurantTable) -> Unit) {
        fun onClick(item: RestaurantTable) = clickListener(item)
    }

    companion object DiffCallback : DiffUtil.ItemCallback<RestaurantFloorplan>() {
        override fun areItemsTheSame(oldItem: RestaurantFloorplan, newItem: RestaurantFloorplan): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: RestaurantFloorplan, newItem: RestaurantFloorplan): Boolean {
            return oldItem.id == newItem.id
        }
    }

}