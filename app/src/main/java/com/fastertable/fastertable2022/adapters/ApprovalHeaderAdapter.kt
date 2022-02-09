package com.fastertable.fastertable2022.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fastertable.fastertable2022.data.models.Approval
import com.fastertable.fastertable2022.databinding.ApprovalLineHeaderBinding

class ApprovalHeaderAdapter(private val approveListener: ApproveAllListener):
    ListAdapter<Approval, ApprovalHeaderAdapter.HeaderViewHolder>(ApprovalHeaderAdapter) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeaderViewHolder {
        return HeaderViewHolder(ApprovalLineHeaderBinding.inflate(
                LayoutInflater.from(parent.context)), parent)
    }

    override fun onBindViewHolder(holder: HeaderViewHolder, position: Int) {
        val approval = getItem(position)
        holder.bind(approval, approveListener)
    }

    /* ViewHolder for displaying header. */
    class HeaderViewHolder(private var binding: ApprovalLineHeaderBinding, private val parent:ViewGroup)
        : RecyclerView.ViewHolder(binding.root){
        fun bind(approval:Approval, approveAllListener: ApproveAllListener) {
            binding.approval = null
            binding.approval = approval
            binding.executePendingBindings()
            binding.switchApproveAll.setOnCheckedChangeListener{
                    _, isChecked ->

                if (isChecked){
                    approveAllListener.onClick(true)
                }else{
                    approveAllListener.onClick(false)
                }
            }
        }
    }



    class ApproveAllListener(val clickListener: (item: Boolean) -> Unit) {
        fun onClick(item: Boolean) = clickListener(item)
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Approval>() {
        override fun areItemsTheSame(oldItem: Approval, newItem: Approval): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: Approval, newItem: Approval): Boolean {
            return oldItem.id == newItem.id
        }
    }


    /* Returns number of items, since there is only one item in the header return one  */
    override fun getItemCount(): Int {
        return 1
    }

}