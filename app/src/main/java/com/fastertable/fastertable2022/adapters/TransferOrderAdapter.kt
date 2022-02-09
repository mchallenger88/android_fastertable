package com.fastertable.fastertable2022.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fastertable.fastertable2022.R
import com.fastertable.fastertable2022.data.models.Order
import com.fastertable.fastertable2022.databinding.TransferOrderListLineItemBinding

class TransferOrderAdapter(val clickListener: TransferOrderListListener): ListAdapter<Order, TransferOrderAdapter.TransferOrderViewHolder>(
    DiffCallback
) {

    override fun onBindViewHolder(holder: TransferOrderViewHolder, position: Int) {
        val order = getItem(position)
        holder.bind(order, clickListener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransferOrderViewHolder {
        return TransferOrderViewHolder(TransferOrderListLineItemBinding.inflate(LayoutInflater.from(parent.context)))
    }


    class TransferOrderViewHolder(private var binding: TransferOrderListLineItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(order: Order, clickListener: TransferOrderListListener) {
            binding.order = order
            binding.clickListener = clickListener
            binding.executePendingBindings()

            order.transfer?.let{
                binding.chkTransferOrder.isChecked = it
            }

            binding.chkTransferOrder.setOnClickListener {
                order.transfer?.let {
                    order.transfer = !it
                }
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


class TransferOrderListListener(val clickListener: (order: Order) -> Unit) {
    fun onClick(order: Order) = clickListener(order)
}

class TransferOrderListHeaderAdapter: RecyclerView.Adapter<TransferOrderListHeaderAdapter.HeaderViewHolder>() {
    private var orderCount: Int = 0

    /* ViewHolder for displaying header. */
    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view){
        fun bind() {}
    }

    /* Inflates view and returns HeaderViewHolder. */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeaderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.transfer_order_header_line_item, parent, false)
        return HeaderViewHolder(view)
    }

    /* Binds number of flowers to the header. */
    override fun onBindViewHolder(holder: HeaderViewHolder, position: Int) {
        holder.bind()
    }

    /* Returns number of items, since there is only one item in the header return one  */
    override fun getItemCount(): Int {
        return 1
    }
}
