package com.fastertable.fastertable.adapters

import android.content.Context
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
import com.fastertable.fastertable.data.models.Guest
import com.fastertable.fastertable.databinding.GuestButtonBinding


class GuestSideBarAdapter(private val clickListener: GuestSideBarListener) : ListAdapter<Guest, GuestSideBarAdapter.GuestSideBarViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuestSideBarAdapter.GuestSideBarViewHolder {
        return GuestSideBarViewHolder(GuestButtonBinding.inflate(LayoutInflater.from(parent.context)), parent)
    }

    override fun onBindViewHolder(holder: GuestSideBarViewHolder, position: Int) {
        val orderItem = getItem(position)
        holder.bind(orderItem, clickListener)
    }

    class GuestSideBarViewHolder(private var binding: GuestButtonBinding, private val parent: ViewGroup):
        RecyclerView.ViewHolder(binding.root) {
        fun bind(guest: Guest, clickListener: GuestSideBarListener) {
            binding.guest = guest
            binding.clickListener = clickListener
            binding.executePendingBindings()


            val white = ContextCompat.getColor(parent.context, R.color.white)
            val offwhite = ContextCompat.getColor(parent.context, R.color.offWhite)

            binding.guestButton.text = parent.context.getString(R.string.guest_with_number, guest.id.plus(1).toString())
            if (guest.uiActive){
                binding.guestButton.setTextColor(ColorStateList.valueOf(white))
                binding.guestButton.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_user_white, 0, 0)
            }else{
                binding.guestButton.setTextColor(ColorStateList.valueOf(offwhite))
                binding.guestButton.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_user_offwhite, 0, 0)
            }
            binding.guestButton.textSize = 16f

            binding.guestButton.textAlignment = View.TEXT_ALIGNMENT_CENTER

            val layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            )
            layoutParams.gravity = Gravity.CENTER_HORIZONTAL
            layoutParams.setMargins(0, 40, 0, 0)
            binding.guestButton.setOnClickListener{
                clickListener.onClick(guest)
            }
        }
    }

    class GuestSideBarListener(val clickListener: (item: Guest) -> Unit) {
        fun onClick(item: Guest) = clickListener(item)
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Guest>() {
        override fun areItemsTheSame(oldItem: Guest, newItem: Guest): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: Guest, newItem: Guest): Boolean {
            return oldItem.id == newItem.id
        }
    }
}