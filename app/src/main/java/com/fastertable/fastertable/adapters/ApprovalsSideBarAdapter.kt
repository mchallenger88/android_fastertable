package com.fastertable.fastertable.adapters

import android.content.res.ColorStateList
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fastertable.fastertable.R
import com.fastertable.fastertable.data.models.Approval
import com.fastertable.fastertable.data.models.ApprovalItem
import com.fastertable.fastertable.data.models.ApprovalOrderPayment
import com.fastertable.fastertable.databinding.ApprovalButtonBinding


class ApprovalsSideBarAdapter(private val clickListener: ApprovalSideBarListener) : ListAdapter<ApprovalOrderPayment, ApprovalsSideBarAdapter.ApprovalSideBarViewHolder>(ApprovalsSideBarAdapter) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApprovalSideBarViewHolder {
        return ApprovalSideBarViewHolder(
            ApprovalButtonBinding.inflate(
                LayoutInflater.from(parent.context)
            ), parent
        )
    }

    override fun onBindViewHolder(holder: ApprovalSideBarViewHolder, position: Int) {
        val approval = getItem(position)
        holder.bind(approval, clickListener)
    }

    class ApprovalSideBarViewHolder(private var binding: ApprovalButtonBinding, private val parent: ViewGroup):
        RecyclerView.ViewHolder(binding.root) {
        fun bind(approval: ApprovalOrderPayment, clickListener: ApprovalSideBarListener){
            binding.approval = approval.approval
            binding.clickListener = clickListener
            binding.executePendingBindings()

            val white = ContextCompat.getColor(parent.context, R.color.white)
            val offWhite = ContextCompat.getColor(parent.context, R.color.offWhite)
            binding.approvalButton.setTextColor(ColorStateList.valueOf(white))
            binding.approvalButton.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_outline_fact_check_24_white, 0, 0)

            binding.approvalButton.text = parent.context.getString(R.string.approval)
//            if (approvalItem.uiActive){
//                binding.approvalButton.setTextColor(ColorStateList.valueOf(white))
//                binding.approvalButton.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_outline_fact_check_24_white, 0, 0)
//            }else{
//                binding.approvalButton.setTextColor(ColorStateList.valueOf(offWhite))
//                binding.approvalButton.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_outline_fact_check_24_off_white, 0, 0)
//            }
            binding.approvalButton.textSize = 16f

            binding.approvalButton.textAlignment = View.TEXT_ALIGNMENT_CENTER

            val layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            layoutParams.gravity = Gravity.CENTER_HORIZONTAL
            layoutParams.setMargins(0, 40, 0, 0)
            binding.approvalButton.setOnClickListener{
                clickListener.onClick(approval)
            }
        }
    }

    class ApprovalSideBarListener(val clickListener: (item: ApprovalOrderPayment) -> Unit) {
        fun onClick(item: ApprovalOrderPayment) = clickListener(item)
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