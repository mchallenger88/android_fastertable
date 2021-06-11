package com.fastertable.fastertable.adapters

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fastertable.fastertable.R
import com.fastertable.fastertable.data.models.Order
import com.fastertable.fastertable.databinding.CheckoutOrderListLineItemBinding

class CheckoutOrdersAdapter(val clickListener: CheckoutOrderListListener): ListAdapter<Order, CheckoutOrdersAdapter.OrderViewHolder>(
    DiffCallback
) {

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = getItem(position)
        holder.bind(order, clickListener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        return OrderViewHolder(CheckoutOrderListLineItemBinding.inflate(LayoutInflater.from(parent.context)), parent)
    }


    class OrderViewHolder(private var binding: CheckoutOrderListLineItemBinding, private val parent: ViewGroup) : RecyclerView.ViewHolder(binding.root) {
        fun bind(order: Order, clickListener: CheckoutOrderListListener) {
            binding.order = order
            binding.clickListener = clickListener
            binding.executePendingBindings()

            val white = ContextCompat.getColor(parent.context, R.color.white)
            val red = ContextCompat.getColor(parent.context, R.color.secondaryColor)
            val black = ContextCompat.getColor(parent.context, R.color.default_text_color)

            if (order.closeTime != null){
                binding.txtCloseTime.setTextColor(ColorStateList.valueOf(black))
                binding.txtOrderNumber.setTextColor(ColorStateList.valueOf(black))
                binding.txtOrderType.setTextColor(ColorStateList.valueOf(black))
                binding.txtStartTime.setTextColor(ColorStateList.valueOf(black))
                binding.txtTableNumber.setTextColor(ColorStateList.valueOf(black))

                binding.txtCloseTime.setBackgroundColor(white)
                binding.txtOrderNumber.setBackgroundColor(white)
                binding.txtOrderType.setBackgroundColor(white)
                binding.txtStartTime.setBackgroundColor(white)
                binding.txtTableNumber.setBackgroundColor(white)
            }else{
                binding.txtCloseTime.setTextColor(ColorStateList.valueOf(white))
                binding.txtOrderNumber.setTextColor(ColorStateList.valueOf(white))
                binding.txtOrderType.setTextColor(ColorStateList.valueOf(white))
                binding.txtStartTime.setTextColor(ColorStateList.valueOf(white))
                binding.txtTableNumber.setTextColor(ColorStateList.valueOf(white))

                binding.txtCloseTime.setBackgroundColor(red)
                binding.txtOrderNumber.setBackgroundColor(red)
                binding.txtOrderType.setBackgroundColor(red)
                binding.txtStartTime.setBackgroundColor(red)
                binding.txtTableNumber.setBackgroundColor(red)
            }

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

class CheckoutOrderListListener(val clickListener: (id: String) -> Unit) {
    fun onClick(order: Order) = clickListener(order.id)
}