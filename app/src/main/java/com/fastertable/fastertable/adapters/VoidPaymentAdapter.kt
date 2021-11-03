package com.fastertable.fastertable.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fastertable.fastertable.R
import com.fastertable.fastertable.data.models.TicketPayment
import com.fastertable.fastertable.databinding.VoidPaymentLineItemBinding

class VoidPaymentAdapter(private val clickListener: VoidPaymentListener) : ListAdapter<TicketPayment, VoidPaymentAdapter.VoidPaymentViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VoidPaymentViewHolder {
        return VoidPaymentViewHolder(VoidPaymentLineItemBinding.inflate(LayoutInflater.from(parent.context)), parent)
    }

    override fun onBindViewHolder(holder: VoidPaymentViewHolder, position: Int) {
        val ticketPayment = getItem(position)
        holder.bind(ticketPayment, clickListener)
    }

    class VoidPaymentViewHolder(private var binding: VoidPaymentLineItemBinding, private val parent: ViewGroup) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(payment: TicketPayment, clickListener: VoidPaymentListener){
            Log.d("VoidTeset", "I'm here")
            binding.payment = null
            binding.payment = payment
            binding.executePendingBindings()
            if (payment.paymentType == "Cash"){
                binding.txtVoidCreditCardNumber.text = parent.context.getString(R.string.cash_payment)
            }else{
                val card = payment.creditCardTransactions?.get(0)?.creditTransaction?.AccountNumber
                binding.txtVoidCreditCardNumber.text = parent.context.getString(R.string.credit_payment, "$card")
            }

            binding.fabVoidPayment.setOnClickListener {
                    clickListener.onClick(payment)
                }
            }

        }

    companion object DiffCallback : DiffUtil.ItemCallback<TicketPayment>() {
        override fun areItemsTheSame(oldItem: TicketPayment, newItem: TicketPayment): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: TicketPayment, newItem: TicketPayment): Boolean {
            return oldItem.id == newItem.id
        }
    }
    class VoidPaymentListener(val clickListener: (item: TicketPayment) -> Unit) {
        fun onClick(item: TicketPayment) = clickListener(item)
    }
}