package com.fastertable.fastertable2022.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fastertable.fastertable2022.data.models.Terminal
import com.fastertable.fastertable2022.databinding.TerminalLineItemBinding

class TerminalAdapter(private val clickListener: TerminalItemListener): ListAdapter<Terminal, TerminalAdapter.TerminalViewHolder>(DiffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TerminalViewHolder {
        return TerminalViewHolder(TerminalLineItemBinding.inflate(LayoutInflater.from(parent.context)), parent)
    }

    override fun onBindViewHolder(holder: TerminalViewHolder, position: Int) {
        val orderItem = getItem(position)
        holder.bind(orderItem, clickListener)
    }
    class TerminalViewHolder(private var binding: TerminalLineItemBinding, private val parent: ViewGroup):
        RecyclerView.ViewHolder(binding.root) {
        fun bind(terminal: Terminal, clickListener: TerminalItemListener) {
            binding.terminal = terminal
            binding.clickListener = clickListener
            binding.executePendingBindings()

            binding.layoutTerminal.setOnClickListener{
                clickListener.onClick(terminal)
            }
        }
    }

    class TerminalItemListener(val clickListener: (item: Terminal) -> Unit) {
        fun onClick(item: Terminal) = clickListener(item)
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Terminal>() {
        override fun areItemsTheSame(oldItem: Terminal, newItem: Terminal): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: Terminal, newItem: Terminal): Boolean {
            return oldItem.terminalId == newItem.terminalId
        }
    }

}