package com.fastertable.fastertable.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.fastertable.fastertable.data.models.Employee
import com.fastertable.fastertable.databinding.TransferEmployeeLineItemBinding

class TransferEmployeeAdapter(val clickListener: TransferEmployeeListListener): ListAdapter<Employee, TransferEmployeeAdapter.TransferEmployeeViewHolder>(DiffCallback) {

    override fun onBindViewHolder(holder: TransferEmployeeAdapter.TransferEmployeeViewHolder, position: Int) {
        val order = getItem(position)
        holder.bind(order, clickListener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransferEmployeeAdapter.TransferEmployeeViewHolder {
        return TransferEmployeeAdapter.TransferEmployeeViewHolder(
            TransferEmployeeLineItemBinding.inflate(
                LayoutInflater.from(parent.context)
            )
        )
    }

class TransferEmployeeViewHolder(private var binding: TransferEmployeeLineItemBinding): RecyclerView.ViewHolder(binding.root){
    fun bind(employee: Employee, clickListener: TransferEmployeeListListener) {
        binding.employee = employee
        binding.clickListener = clickListener
        binding.executePendingBindings()

        binding.root.setOnClickListener{
            clickListener.onClick(employee)
        }
    }
}

companion object DiffCallback : DiffUtil.ItemCallback<Employee>() {
    override fun areItemsTheSame(oldItem: Employee, newItem: Employee): Boolean {
        return oldItem === newItem
    }

    override fun areContentsTheSame(oldItem: Employee, newItem: Employee): Boolean {
        return oldItem.id == newItem.id
    }
}

}

class TransferEmployeeListListener(val clickListener: (employee: Employee) -> Unit) {
    fun onClick(employee: Employee) = clickListener(employee)
}