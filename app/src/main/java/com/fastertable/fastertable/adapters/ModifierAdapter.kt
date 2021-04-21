package com.fastertable.fastertable.adapters

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.RadioButton
import androidx.constraintlayout.helper.widget.Flow
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fastertable.fastertable.R
import com.fastertable.fastertable.data.models.Modifier
import com.fastertable.fastertable.data.models.OrderMod
import com.fastertable.fastertable.databinding.ModifierLineItemBinding
import com.google.android.material.button.MaterialButton

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
            binding.layoutModtestbuttons.removeAllViews()
            if (modifier.selectionLimitMin == 0){
                binding.txtModRequired.visibility = View.GONE
            }

            createButtons(modifier, clickListener)

        }

        private fun createRadioButtons(modifier: Modifier, clickListener: ModifierListener){
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

        private fun createCheckBoxes(modifier: Modifier, clickListener: ModifierListener){
            modifier.modifierItems.forEach { item ->
                val cb = CheckBox(parent.context)
                cb.id = ViewCompat.generateViewId()
                cb.textSize = 24f
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

        @SuppressLint("SetTextI18n")
        private fun createButtons(modifier: Modifier, clickListener: ModifierListener){
            val flow = Flow(parent.context)
            flow.id = ViewCompat.generateViewId()
            val flowParams = ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.MATCH_PARENT,
                    ConstraintLayout.LayoutParams.WRAP_CONTENT
            )
            flow.layoutParams = flowParams
            flow.setOrientation(Flow.HORIZONTAL)
            flow.setWrapMode(Flow.WRAP_CHAIN)
            flow.setVerticalGap(2)
            flow.setVerticalStyle(Flow.CHAIN_SPREAD_INSIDE)
            flow.setHorizontalStyle(Flow.CHAIN_PACKED)
            flow.setHorizontalBias(0.0F)

            modifier.modifierItems.forEachIndexed{int, item ->
                val btn = MaterialButton(parent.context, null, R.attr.materialButtonOutlinedStyle)
                btn.id = ViewCompat.generateViewId()
                btn.tag = int
                val ordMod = OrderMod(item = item, mod = modifier)
                btn.setOnClickListener { clickListener.onClick(ordMod) }
                val mod1 = ContextCompat.getColor(parent.context, R.color.mod1)
                val mod2 = ContextCompat.getColor(parent.context, R.color.mod2)
                val mod3 = ContextCompat.getColor(parent.context, R.color.mod3)
                when (item.quantity){
                    1 -> btn.backgroundTintList = ColorStateList.valueOf(mod1)
                    2 -> btn.backgroundTintList = ColorStateList.valueOf(mod2)
                    3 -> btn.backgroundTintList = ColorStateList.valueOf(mod3)
                    else -> null
                }
                if (item.surcharge == 0.0){
                    btn.text = item.itemName
                }else{
                    btn.text = item.itemName + ": (+" + "$%.${2}f".format(item.surcharge) + ")"
                }
                binding.layoutModtestbuttons.addView(btn)
                flow.addView(btn)
            }
            binding.layoutModtestbuttons.addView(flow)

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