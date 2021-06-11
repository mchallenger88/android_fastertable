package com.fastertable.fastertable.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fastertable.fastertable.data.models.Payment
import com.fastertable.fastertable.databinding.CheckoutPaymentListLineItemBinding

class CheckoutPaymentsAdapter (val clickListener: CheckoutPaymentListListener): ListAdapter<Payment, CheckoutPaymentsAdapter.PaymentViewHolder>(
        DiffCallback
    ) {

        override fun onBindViewHolder(holder: PaymentViewHolder, position: Int) {
            val payment = getItem(position)
            holder.bind(payment, clickListener)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentViewHolder {
            return PaymentViewHolder(CheckoutPaymentListLineItemBinding.inflate(LayoutInflater.from(parent.context)))
        }


        class PaymentViewHolder(private var binding: CheckoutPaymentListLineItemBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bind(payment: Payment, clickListener: CheckoutPaymentListListener) {
                binding.payment = payment
                binding.clickListener = clickListener
                binding.executePendingBindings()

                binding.root.setOnClickListener{
                    clickListener.onClick(payment)
                }
            }

        }

        companion object DiffCallback : DiffUtil.ItemCallback<Payment>() {
            override fun areItemsTheSame(oldItem: Payment, newItem: Payment): Boolean {
                return oldItem === newItem
            }

            override fun areContentsTheSame(oldItem: Payment, newItem: Payment): Boolean {
                return oldItem.id == newItem.id
            }
        }

    }

    class CheckoutPaymentListListener(val clickListener: (id: String) -> Unit) {
        fun onClick(payment: Payment) = clickListener(payment.id)
    }
