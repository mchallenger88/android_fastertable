package com.fastertable.fastertable2022.adapters

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fastertable.fastertable2022.R
import com.fastertable.fastertable2022.data.models.PayTicket
import com.fastertable.fastertable2022.databinding.CheckoutOrderListLineItemBinding

class CheckoutOrdersAdapter(val clickListener: CheckoutOrderListListener): ListAdapter<PayTicket, CheckoutOrdersAdapter.OrderViewHolder>(
    DiffCallback
) {

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val payTicket = getItem(position)
        holder.bind(payTicket, clickListener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        return OrderViewHolder(CheckoutOrderListLineItemBinding.inflate(LayoutInflater.from(parent.context)), parent)
    }


    class OrderViewHolder(private var binding: CheckoutOrderListLineItemBinding, private val parent: ViewGroup) : RecyclerView.ViewHolder(binding.root) {
        fun bind(payTicket: PayTicket, clickListener: CheckoutOrderListListener) {
            binding.payTicket = payTicket
            binding.clickListener = clickListener
            binding.executePendingBindings()

            val white = ContextCompat.getColor(parent.context, R.color.white)
            val red = ContextCompat.getColor(parent.context, R.color.secondaryColor)
            val black = ContextCompat.getColor(parent.context, R.color.default_text_color)

            if (payTicket.order.closeTime != null){
                binding.txtOrderTotal.setTextColor(ColorStateList.valueOf(black))
                binding.txtOrderNumber.setTextColor(ColorStateList.valueOf(black))
                binding.txtPaymentType.setTextColor(ColorStateList.valueOf(black))
                binding.txtStartTime.setTextColor(ColorStateList.valueOf(black))
                binding.txtTableNumber.setTextColor(ColorStateList.valueOf(black))

                binding.txtOrderTotal.setBackgroundColor(white)
                binding.txtOrderNumber.setBackgroundColor(white)
                binding.txtPaymentType.setBackgroundColor(white)
                binding.txtStartTime.setBackgroundColor(white)
                binding.txtTableNumber.setBackgroundColor(white)

            }else{
                binding.txtOrderTotal.setTextColor(ColorStateList.valueOf(white))
                binding.txtOrderNumber.setTextColor(ColorStateList.valueOf(white))
                binding.txtPaymentType.setTextColor(ColorStateList.valueOf(white))
                binding.txtStartTime.setTextColor(ColorStateList.valueOf(white))
                binding.txtTableNumber.setTextColor(ColorStateList.valueOf(white))

                binding.txtOrderTotal.setBackgroundColor(red)
                binding.txtOrderNumber.setBackgroundColor(red)
                binding.txtPaymentType.setBackgroundColor(red)
                binding.txtStartTime.setBackgroundColor(red)
                binding.txtTableNumber.setBackgroundColor(red)
            }

            binding.root.setOnClickListener{
                clickListener.onClick(payTicket)
            }
        }

    }

    companion object DiffCallback : DiffUtil.ItemCallback<PayTicket>() {
        override fun areItemsTheSame(oldItem: PayTicket, newItem: PayTicket): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: PayTicket, newItem: PayTicket): Boolean {
            return oldItem.order.id == newItem.order.id
        }
    }
}

class CheckoutOrderListListener(val clickListener: (id: String) -> Unit) {
    fun onClick(payTicket: PayTicket) = clickListener(payTicket.order.id)
}