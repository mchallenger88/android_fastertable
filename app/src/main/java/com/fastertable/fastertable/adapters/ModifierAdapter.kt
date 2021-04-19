package com.fastertable.fastertable.adapters

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.RadioButton
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fastertable.fastertable.data.models.Modifier
import com.fastertable.fastertable.data.models.OrderMod
import com.fastertable.fastertable.databinding.ModifierLineItemBinding

class ModifierAdapter(private val clickListener: ModifierListener) : ListAdapter<Modifier, ModifierAdapter.ModifierViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModifierViewHolder {
        return ModifierViewHolder(ModifierLineItemBinding.inflate(LayoutInflater.from(parent.context)), parent)
    }

    override fun onBindViewHolder(holder: ModifierViewHolder, position: Int) {
        val modifier = getItem(position)
        holder.bind(modifier, clickListener)
    }

    class ModifierViewHolder(private var binding: ModifierLineItemBinding,
                             private val parent: ViewGroup):
        RecyclerView.ViewHolder(binding.root){
        @SuppressLint("SetTextI18n")
        fun bind(modifier: Modifier, clickListener: ModifierListener){
            binding.modifier = modifier
            binding.clickListener = clickListener
            binding.executePendingBindings()
            binding.listCheckbox.removeAllViews()
            binding.modifierRadioGroup.removeAllViews()
            if (modifier.selectionLimitMin == 0){
                binding.txtModRequired.visibility = View.GONE
            }

            if (modifier.selectionLimitMin >= 1 && modifier.selectionLimitMax > 1){
                modifier.modifierItems.forEach { item ->
                    val cb = CheckBox(parent.context)
                    cb.id = ViewCompat.generateViewId()
                    cb.textSize = 24f
//                    cb.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                    if (item.surcharge == 0.0){
                        cb.text = item.itemName
                    }else{
                        cb.text = item.itemName + ": (+" + "$%.${2}f".format(item.surcharge) + ")"
                    }
                    val ordMod = OrderMod(item = item, mod = modifier)
                    cb.setOnClickListener { clickListener.onClick(ordMod) }
                    binding.listCheckbox.addView(cb)
                }
                binding.listCheckbox.visibility = View.VISIBLE

            }

            if (modifier.selectionLimitMin <= 1 && modifier.selectionLimitMax <= 1 ){
                modifier.modifierItems.forEach{ item ->
                    val rb = RadioButton(parent.context)
                    rb.id = ViewCompat.generateViewId()
                    rb.textSize = 24f
//                    rb.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                    if (item.surcharge == 0.0){
                        rb.text = item.itemName
                    }else{
                        rb.text = item.itemName + ": (+" + "$%.${2}f".format(item.surcharge) + ")"
                    }
                    val ordMod = OrderMod(item = item, mod = modifier)
                    rb.setOnClickListener { clickListener.onClick(ordMod) }
                    binding.modifierRadioGroup.addView(rb)
                }
                binding.modifierRadioGroup.visibility = View.VISIBLE
            }

        }

        private fun setRadios(answer: Int) {
            //bug fix: clear RadioGroup selection before setting the values
            // otherwise checked answers sometimes disappear on scroll
//            view.radioGroup.clearCheck()
//
//            if (answer == 0) return //skip setting checked if no answer is selected
//
//            when (answer) {
//                1 -> view.radioButton.isChecked = true
//                2 -> view.radioButton2.isChecked = true
//                3 -> view.radioButton3.isChecked = true
//                4 -> view.radioButton4.isChecked = true
//                5 -> view.radioButton5.isChecked = true
//            }
        }

    }

    class ModifierListener(val clickListener: (item: OrderMod) -> Unit) {
        fun onClick(item: OrderMod) = clickListener(item)
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Modifier>() {
        override fun areItemsTheSame(oldItem: Modifier, newItem: Modifier): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: Modifier, newItem: Modifier): Boolean {
            return oldItem.id == newItem.id
        }
    }
}