package com.fastertable.fastertable.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fastertable.fastertable.R
import com.fastertable.fastertable.data.models.IngredientChange
import com.fastertable.fastertable.data.models.ItemIngredient
import com.fastertable.fastertable.databinding.IngredientLineItemBinding

enum class PlusMinus{
    PLUS,
    MINUS
}
class IngredientsAdapter(private val clickListener: IngredientListener) : ListAdapter<ItemIngredient, IngredientsAdapter.IngredientViewHolder>(IngredientsAdapter) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredientViewHolder {
        return IngredientViewHolder(IngredientLineItemBinding.inflate(LayoutInflater.from(parent.context)), parent)
    }

    override fun onBindViewHolder(holder: IngredientViewHolder, position: Int) {
        val ingredient = getItem(position)
        holder.bind(ingredient, clickListener, position)
    }

    class IngredientViewHolder(private var binding: IngredientLineItemBinding,
                               private val parent: ViewGroup): RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("TeSetTextI18n")
        fun bind(item: ItemIngredient, clickListener: IngredientListener, position: Int) {
            binding.layoutIngredient.removeAllViews()
            binding.ingredients = item
            binding.clickListener = clickListener
            binding.executePendingBindings()

            val btnAdd = createPlusMinusButton(PlusMinus.PLUS, item, clickListener)
            val btnMinus = createPlusMinusButton(PlusMinus.MINUS, item, clickListener)

            val textView = TextView(parent.context)
            textView.id = ViewCompat.generateViewId()
            val surcharge = "$%.${2}f".format(item.surcharge)
            when (item.orderValue){
                0 -> textView.text = "No ${item.name}"
                1 -> textView.text = if (item.surcharge > 0) "${item.name} (${surcharge})" else item.name
                2 -> textView.text = "Extra ${item.name}"
                else -> textView.text = item.name
            }

            textView.textSize = 24f
            val textColor = ContextCompat.getColor(parent.context, R.color.default_text_color)
            textView.setTextColor(textColor)
//                textView.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
            val tvLayoutParams: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
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

            if (position == 0){
                val headerTV = TextView(parent.context)
                headerTV.id = ViewCompat.generateViewId()
                headerTV.text = "Ingredients"
                headerTV.setTextAppearance(R.style.large_title_bold)
                val headerTVParams: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                headerTVParams.setMargins(6, 16, 0, 16)
                headerTV.layoutParams = headerTVParams

                binding.layoutIngredient.addView(headerTV)
            }


            binding.layoutIngredient.addView(layout)

        }

        private fun createPlusMinusButton(type: PlusMinus,
                                          item: ItemIngredient,
                                          clickListener: IngredientListener): ImageButton {

            val btn = ImageButton(parent.context)
            btn.id = ViewCompat.generateViewId()
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.width = 66
            params.height = 66
            params.topMargin = 15
            params.leftMargin = 35
            btn.layoutParams = params
            btn.contentDescription = "add"
            btn.background = ContextCompat.getDrawable(parent.context, R.drawable.customborder)
            btn.scaleType = ImageView.ScaleType.CENTER
            if (type == PlusMinus.PLUS){
                btn.setImageResource(R.drawable.ic_baseline_add_24)
                val ic = IngredientChange(
                        item = item,
                        value = 1
                )
                btn.setOnClickListener { clickListener.onClick(ic) }
            }

            if (type == PlusMinus.MINUS){
                btn.setImageResource(R.drawable.ic_baseline_horizontal_rule_24)
                val ic = IngredientChange(
                        item = item,
                        value = -1
                )
                btn.setOnClickListener { clickListener.onClick(ic) }
            }

            return btn
        }

    }

    class IngredientListener(val clickListener: (item: IngredientChange) -> Unit) {
        fun onClick(item: IngredientChange) = clickListener(item)
    }

    companion object DiffCallback : DiffUtil.ItemCallback<ItemIngredient>() {
        override fun areItemsTheSame(oldItem: ItemIngredient, newItem: ItemIngredient): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: ItemIngredient, newItem: ItemIngredient): Boolean {
            return oldItem.name == newItem.name
        }
    }
}



//class IngredientHeaderAdapter: ListAdapter<ItemIngredient, IngredientHeaderAdapter.HeaderViewHolder>(DiffCallback) {
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeaderViewHolder {
//        return HeaderViewHolder(IngredientLineItemHeaderBinding.inflate(LayoutInflater.from(parent.context)))
//    }
//
//    override fun onBindViewHolder(holder: HeaderViewHolder, position: Int) {
//        holder.bind()
//    }
//
//    class HeaderViewHolder(private var binding: IngredientLineItemHeaderBinding):
//            RecyclerView.ViewHolder(binding.root) {
//        fun bind(){
//            binding.executePendingBindings()
//        }
//    }
//
//    override fun getItemCount(): Int {
//        return 1
//    }
//
//    companion object DiffCallback : DiffUtil.ItemCallback<ItemIngredient>() {
//        override fun areItemsTheSame(oldItem: ItemIngredient, newItem: ItemIngredient): Boolean {
//            return oldItem === newItem
//        }
//
//        override fun areContentsTheSame(oldItem: ItemIngredient, newItem: ItemIngredient): Boolean {
//            return oldItem == newItem
//        }
//    }
//
//}