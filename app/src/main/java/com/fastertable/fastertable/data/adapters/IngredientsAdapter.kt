package com.fastertable.fastertable.data.adapters

import android.content.res.ColorStateList
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fastertable.fastertable.R
import com.fastertable.fastertable.data.IngredientList
import com.fastertable.fastertable.databinding.IngredientLineItemBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton

class IngredientsAdapter(private val clickListener: IngredientListener) : ListAdapter<IngredientList, IngredientsAdapter.IngredientViewHolder>(IngredientsAdapter) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredientViewHolder {
        return IngredientViewHolder(IngredientLineItemBinding.inflate(LayoutInflater.from(parent.context)), parent)
    }

    override fun onBindViewHolder(holder: IngredientViewHolder, position: Int) {
        val ingredient = getItem(position)
        holder.bind(ingredient, clickListener)
    }

    class IngredientViewHolder(private var binding: IngredientLineItemBinding,
                               private val parent: ViewGroup): RecyclerView.ViewHolder(binding.root) {

        fun bind(list: IngredientList, clickListener: IngredientListener) {
            binding.ingredients = list
            binding.clickListener = clickListener
            binding.executePendingBindings()

            list.ingredients.forEach { it ->
                val btnAdd = FloatingActionButton(parent.context)
                btnAdd.id = ViewCompat.generateViewId()
                val params = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
                btnAdd.layoutParams = params
                btnAdd.contentDescription = "add"
                btnAdd.elevation = 2f
                val color = ContextCompat.getColor(parent.context, R.color.guest_toolbar)
                btnAdd.backgroundTintList = ColorStateList.valueOf(color)
                btnAdd.customSize = 50
                btnAdd.scaleType = ImageView.ScaleType.CENTER
                btnAdd.setImageResource(R.drawable.ic_baseline_add_24)

                val btnMinus = FloatingActionButton(parent.context)
                btnMinus.id = ViewCompat.generateViewId()
                btnMinus.layoutParams = params
                btnMinus.contentDescription = "add"
                btnMinus.elevation = 2f
                btnMinus.backgroundTintList = ColorStateList.valueOf(color)
                btnMinus.customSize = 50
                btnMinus.scaleType = ImageView.ScaleType.CENTER
                btnMinus.setImageResource(R.drawable.ic_baseline_horizontal_rule_24)

                val textView = TextView(parent.context)
                textView.id = ViewCompat.generateViewId()
                textView.text = it.name
                textView.textSize = 24f
                val textColor = ContextCompat.getColor(parent.context, R.color.default_text_color)
                textView.setTextColor(textColor)
                textView.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                val tvLayoutParams: LinearLayout.LayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
//                val tvLayoutParams = textView.layoutParams as RelativeLayout.LayoutParams
                tvLayoutParams.setMargins(18, 18, 0, 0)
                textView.layoutParams = tvLayoutParams

                val layout = LinearLayout(parent.context)
                layout.id = ViewCompat.generateViewId()
                val layoutPars = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
                layout.layoutParams = layoutPars
                layout.orientation = LinearLayout.HORIZONTAL

                layout.addView(btnMinus)
                layout.addView(btnAdd)
                layout.addView(textView)


                binding.layoutIngredientTest.addView(layout)


            }
        }
    }

    class IngredientListener(val clickListener: (item: IngredientList) -> Unit) {
        fun onClick(item: IngredientList) = clickListener(item)
    }

    companion object DiffCallback : DiffUtil.ItemCallback<IngredientList>() {
        override fun areItemsTheSame(oldItem: IngredientList, newItem: IngredientList): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: IngredientList, newItem: IngredientList): Boolean {
            return oldItem.id == newItem.id
        }
    }
}