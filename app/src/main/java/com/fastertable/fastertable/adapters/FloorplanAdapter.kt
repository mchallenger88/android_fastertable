package com.fastertable.fastertable.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fastertable.fastertable.data.models.RestaurantFloorPlan
import com.fastertable.fastertable.data.models.RestaurantTable
import com.fastertable.fastertable.databinding.FloorplanLineItemBinding
import com.fastertable.fastertable.ui.floorplan.FloorplanTable

class FloorplanAdapter(private val clickListener: FloorplanListener) : ListAdapter<RestaurantFloorPlan, FloorplanAdapter.FloorplanViewHolder>(DiffCallback){

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
        fun bind(floorplan: RestaurantFloorPlan, clickListener: FloorplanListener) {
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

    companion object DiffCallback : DiffUtil.ItemCallback<RestaurantFloorPlan>() {
        override fun areItemsTheSame(oldItem: RestaurantFloorPlan, newItem: RestaurantFloorPlan): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: RestaurantFloorPlan, newItem: RestaurantFloorPlan): Boolean {
            return oldItem.id == newItem.id
        }
    }

}