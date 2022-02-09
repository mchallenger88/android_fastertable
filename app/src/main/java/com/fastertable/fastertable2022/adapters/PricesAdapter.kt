package com.fastertable.fastertable2022.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fastertable.fastertable2022.R
import com.fastertable.fastertable2022.data.models.ItemPrice
import com.fastertable.fastertable2022.databinding.MenuItemPriceLineItemBinding

class PricesAdapter(private val priceListener: PriceListener) : ListAdapter<ItemPrice, PricesAdapter.PriceViewHolder>(PricesAdapter) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PriceViewHolder {
        return PriceViewHolder(
            MenuItemPriceLineItemBinding.inflate(
                LayoutInflater.from(parent.context)
            ), parent
        )
    }

    override fun onBindViewHolder(holder: PriceViewHolder, position: Int) {
        val price = getItem(position)
        holder.bind(price, priceListener, position)
    }

    class PriceViewHolder(private var binding: MenuItemPriceLineItemBinding, private val parent: ViewGroup):
        RecyclerView.ViewHolder(binding.root){
        @SuppressLint("SetTextI18n")
        fun bind(price: ItemPrice, priceListener: PriceListener, position: Int){
            binding.price = null
            binding.price = price
            val typeface = ResourcesCompat.getFont(parent.context, R.font.open_sans_semibold)
            binding.radioPrice.typeface = typeface
            binding.radioPrice.textSize = 24f
            val workingPrice = price.price.plus(price.modifiedPrice).times(price.quantity)
            val x = binding.radioPrice.context.getString(R.string.item_price, "%.${2}f".format(workingPrice))
            binding.radioPrice.text = "${price.size}: $x"
            binding.radioPrice.isChecked = price.isSelected

            binding.executePendingBindings()
            binding.radioPrice.setOnCheckedChangeListener{
                    _, isChecked ->
                priceListener.onClick(price)
            }
        }
    }

    class PriceListener(val clickListener: (item: ItemPrice) -> Unit) {
        fun onClick(item: ItemPrice) = clickListener(item)
    }

    companion object DiffCallback : DiffUtil.ItemCallback<ItemPrice>() {
        override fun areItemsTheSame(oldItem: ItemPrice, newItem: ItemPrice): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: ItemPrice, newItem: ItemPrice): Boolean {
            return oldItem == newItem
        }
    }
}