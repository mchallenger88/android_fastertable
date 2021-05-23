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
import com.fastertable.fastertable.data.models.ApprovalItem
import com.fastertable.fastertable.databinding.ApprovalButtonBinding


class ApprovalsSideBarAdapter(private val clickListener: ApprovalSideBarListener) : ListAdapter<ApprovalItem, ApprovalsSideBarAdapter.ApprovalSideBarViewHolder>(ApprovalsSideBarAdapter) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApprovalSideBarViewHolder {
        return ApprovalSideBarViewHolder(
            ApprovalButtonBinding.inflate(
                LayoutInflater.from(parent.context)
            ), parent
        )
    }

    override fun onBindViewHolder(holder: ApprovalSideBarViewHolder, position: Int) {
        val approvalItem = getItem(position)
        holder.bind(approvalItem, clickListener)
    }

    class ApprovalSideBarViewHolder(private var binding: ApprovalButtonBinding, private val parent: ViewGroup):
        RecyclerView.ViewHolder(binding.root) {
        fun bind(approvalItem: ApprovalItem, clickListener: ApprovalSideBarListener){
            binding.approval = approvalItem
            binding.clickListener = clickListener
            binding.executePendingBindings()

            val white = ContextCompat.getColor(parent.context, R.color.white)
            val offWhite = ContextCompat.getColor(parent.context, R.color.offWhite)

            binding.approvalButton.text = parent.context.getString(R.string.approval)
            if (approvalItem.uiActive){
                binding.approvalButton.setTextColor(ColorStateList.valueOf(white))
                binding.approvalButton.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_outline_fact_check_24_white, 0, 0)
            }else{
                binding.approvalButton.setTextColor(ColorStateList.valueOf(offWhite))
                binding.approvalButton.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_outline_fact_check_24_off_white, 0, 0)
            }
            binding.approvalButton.textSize = 16f

            binding.approvalButton.textAlignment = View.TEXT_ALIGNMENT_CENTER

            val layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            layoutParams.gravity = Gravity.CENTER_HORIZONTAL
            layoutParams.setMargins(0, 40, 0, 0)
            binding.approvalButton.setOnClickListener{
                clickListener.onClick(approvalItem)
            }
        }
    }

    class ApprovalSideBarListener(val clickListener: (item: ApprovalItem) -> Unit) {
        fun onClick(item: ApprovalItem) = clickListener(item)
    }

    companion object DiffCallback : DiffUtil.ItemCallback<ApprovalItem>() {
        override fun areItemsTheSame(oldItem: ApprovalItem, newItem: ApprovalItem): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: ApprovalItem, newItem: ApprovalItem): Boolean {
            return oldItem.id == newItem.id
        }
    }
}