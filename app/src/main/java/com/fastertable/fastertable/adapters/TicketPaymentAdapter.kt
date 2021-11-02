package com.fastertable.fastertable.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.fastertable.fastertable.R
import com.fastertable.fastertable.data.models.TicketPayment
import com.fastertable.fastertable.databinding.CheckoutAddTipLineItemBinding

class TicketPaymentAdapter(private val clickListener: AddTipListener) : ListAdapter<TicketPayment, TicketPaymentAdapter.TicketPaymentViewHolder>(DiffCallback)
{

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TicketPaymentViewHolder {
        return TicketPaymentViewHolder(CheckoutAddTipLineItemBinding.inflate(LayoutInflater.from(parent.context)), parent)
    }

    override fun onBindViewHolder(holder: TicketPaymentViewHolder, position: Int) {
        val ticketPayment = getItem(position)
        holder.bind(ticketPayment, clickListener)
    }

    class TicketPaymentViewHolder(private var binding: CheckoutAddTipLineItemBinding, private val parent: ViewGroup) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(payment: TicketPayment, clickListener: AddTipListener){
            binding.payment = null
            binding.payment = payment
            binding.executePendingBindings()
            if (payment.paymentType == "Cash"){
                binding.txtCreditCardNumber.text = parent.context.getString(R.string.cash_payment)
            }else{
                val card = payment.creditCardTransactions?.get(0)?.creditTransaction?.AccountNumber
                binding.txtCreditCardNumber.text = parent.context.getString(R.string.credit_payment, "$card")
            }

            binding.fabAddTip.setOnClickListener {
                if ( binding.editAddTip.text.toString() != ""){
                    payment.gratuity = binding.editAddTip.text.toString().toDouble()
                    binding.editAddTip.setText("")
                    clickListener.onClick(payment)
                }
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

    class AddTipListener(val clickListener: (item: TicketPayment) -> Unit) {
        fun onClick(item: TicketPayment) = clickListener(item)
    }
}