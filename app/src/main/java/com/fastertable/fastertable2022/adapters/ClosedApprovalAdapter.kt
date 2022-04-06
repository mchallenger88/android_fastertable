package com.fastertable.fastertable2022.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fastertable.fastertable2022.R
import com.fastertable.fastertable2022.data.models.ApprovalOrderPayment
import com.fastertable.fastertable2022.databinding.ClosedApprovalLineItemBinding

class ClosedApprovalAdapter() : ListAdapter<ApprovalOrderPayment, ClosedApprovalAdapter.ApprovalItemViewHolder>(ClosedApprovalAdapter){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApprovalItemViewHolder {
        return ApprovalItemViewHolder(
            ClosedApprovalLineItemBinding.inflate(LayoutInflater.from(parent.context)),
            parent
        )
    }

    override fun onBindViewHolder(holder: ApprovalItemViewHolder, position: Int) {
        val ticketItem = getItem(position)
        holder.bind(ticketItem)
    }

    class ApprovalItemViewHolder(
        private var binding: ClosedApprovalLineItemBinding,
        private val parent: ViewGroup
    ) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(aop: ApprovalOrderPayment) {
            binding.aop = null
            binding.aop = aop

            binding.executePendingBindings()
            val typeface = ResourcesCompat.getFont(parent.context, R.font.open_sans_semibold)
            val status: String = if (aop.approval.approved != null && aop.approval.approved == true){
                "Approved"
            }else{
                "Rejected"
            }
            binding.txtClosedApprovalOrderNumber.text = "$status: Order #${aop.order.orderNumber} - ${aop.approval.whoRequested}"
            binding.txtClosedApprovalOrderNumber.typeface = typeface

            when (aop.approval.approvalType){
                "Void Item" -> { itemBindings(binding, aop) }
                "Discount Item" -> { itemBindings(binding, aop) }
                "Modify Price" -> { itemBindings(binding, aop) }
                "Void Ticket" -> { ticketBindings(binding, aop)}
                "Discount Ticket" -> {ticketBindings(binding, aop)}
            }

        }

        @SuppressLint("SetTextI18n")
        private fun itemBindings(binding: ClosedApprovalLineItemBinding, aop: ApprovalOrderPayment){
            if (aop.approval.approvalType == "Discount Item" || aop.approval.approvalType == "Discount Ticket"){
                binding.txtClosedApprovalType.text = "${aop.approval.approvalType}: ${aop.approval.discount}"
            }else{
                binding.txtClosedApprovalType.text = aop.approval.approvalType
            }
            binding.txtClosedApprovalType.text = aop.approval.approvalType
            aop.payment?.let {
                val ticketItems = it.allTicketItems()
                val ticketItem = ticketItems.find { it.id == aop.approval.ticketItemId }
                if (ticketItem != null){
                    val txtApproved: String = if (aop.approval.approved != null && aop.approval.approved == true){
                        "Approved: "
                    }else{
                        "Rejected: "
                    }
                    binding.txtClosedItemQuantity.text = ticketItem.quantity.toString()
                    binding.txtClosedItem.text = ticketItem.itemName
                    binding.txtClosedItemPrice.text = "$txtApproved " + binding.txtClosedItemPrice.context.getString(R.string.item_price, "%.${2}f".format(aop.approval.newItemPrice))

                    val typeface = ResourcesCompat.getFont(parent.context, R.font.open_sans_semibold)
                    binding.txtClosedItemQuantity.typeface = typeface
                    binding.txtClosedItem.typeface = typeface
                    binding.txtClosedItemPrice.typeface = typeface
                }
            }

        }

        @SuppressLint("SetTextI18n")
        private fun ticketBindings(binding: ClosedApprovalLineItemBinding, aop: ApprovalOrderPayment){
            if (aop.approval.approvalType == "Discount Item" || aop.approval.approvalType == "Discount Ticket"){
                binding.txtClosedApprovalType.text = "${aop.approval.approvalType}: ${aop.approval.discount}"
            }else{
                binding.txtClosedApprovalType.text = aop.approval.approvalType
            }
            aop.payment?.let {
                val ticket = it.tickets?.find { it.id == aop.approval.ticketId }
                if (ticket != null){
                    val txtApproved: String = if (aop.approval.approved != null && aop.approval.approved == true){
                        "Approved: "
                    }else{
                        "Rejected: "
                    }
                    val typeface = ResourcesCompat.getFont(parent.context, R.font.open_sans_semibold)
                    val totalDiscount = ticket.getApprovedTotal()
                    binding.txtClosedItem.text = "$txtApproved " + binding.txtClosedItem.context.getString(R.string.item_price, "%.${2}f".format(totalDiscount))
                    binding.txtClosedItem.typeface = typeface
                }
            }

        }
    }
    companion object DiffCallback : DiffUtil.ItemCallback<ApprovalOrderPayment>() {
        override fun areItemsTheSame(oldItem: ApprovalOrderPayment, newItem: ApprovalOrderPayment): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: ApprovalOrderPayment, newItem: ApprovalOrderPayment): Boolean {
            return oldItem.approval.id == newItem.approval.id
        }
    }

}