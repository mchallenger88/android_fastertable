package com.fastertable.fastertable.adapters

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fastertable.fastertable.R
import com.fastertable.fastertable.data.models.*
import com.fastertable.fastertable.ui.order.MenusNavigation
import java.time.ZoneId
import java.time.format.DateTimeFormatter


@SuppressLint("SetTextI18n")
@BindingAdapter("intToString")
fun intToString(textView: TextView, value: Int) {
    textView.text = value.toString()
}


@SuppressLint("SetTextI18n")
@BindingAdapter("terminalUsing")
fun setTerminalName(textView: TextView, terminal: Terminal?){
    if (terminal != null) {
        textView.text = terminal.terminalName
    }
}

@SuppressLint("SetTextI18n")
@BindingAdapter("modSelection")
fun modSelectionText(textView: TextView, mod: Modifier){
    if (mod.selectionLimitMin == 0 && mod.selectionLimitMax == 0){
        textView.text = "Select as many options as needed"
    }

    if (mod.selectionLimitMin == 0 && mod.selectionLimitMax > 0){
        textView.text = "Select up to ${mod.selectionLimitMax}"
    }

    if (mod.selectionLimitMin == 1 && mod.selectionLimitMax == 1){
        textView.text = "Selection required"
    }

    if (mod.selectionLimitMin >= 1 && mod.selectionLimitMax > 1){
        textView.text = "Select at least 1 and up to ${mod.selectionLimitMax}"
    }

//    if (mod.selectionLimitMin > 1 && mod.selectionLimitMax > 1){
//        textView.text = "Select at least ${mod.selectionLimitMin}"
//    }
}


@BindingAdapter("priceDouble")
fun bindDouble(textView: TextView, value: Double){
    textView.text = "$%.${2}f".format(value)
}


@BindingAdapter("bindTableNumber")
fun bindTableNumber(textView: TextView, value: Int){
    textView.text = value.toString()
}


@BindingAdapter("itemSubTotal")
fun itemSubTotal(textView: TextView, item: Order){
    textView.text = "Subtotal: $%.${2}f".format(item.getSubtotal())
}

@BindingAdapter("orderTax")
fun getSalesTax(textView: TextView, total: Double){
    textView.text = "Tax: $%.${2}f".format(total)
}

@BindingAdapter("orderTotal")
fun getOrderTotal(textView: TextView, total: Double){
    textView.text = "Total: $%.${2}f".format(total)
}

@BindingAdapter("orderItemListData")
fun bindRecyclerView(recyclerView: RecyclerView?, order: Order?) {
    val adapter = recyclerView?.adapter as OrderItemAdapter
    val guest = order?.guests?.find{it -> it.uiActive}
    adapter.submitList(guest?.getOrderItems())
    adapter.notifyDataSetChanged()
}

@BindingAdapter("guestListData")
fun bindGuestRecyclerView(recyclerView: RecyclerView?, data: List<Guest>?) {
    val adapter = recyclerView?.adapter as GuestSideBarAdapter
    adapter.submitList(data)
    adapter.notifyDataSetChanged()
}


@BindingAdapter("itemModifiers")
fun bindModifierRecycler(recyclerView: RecyclerView?, data: List<Modifier>?){
    val adapter = recyclerView?.adapter as ModifierAdapter
    adapter.submitList(data)
    adapter.notifyDataSetChanged()
}




@SuppressLint("SetTextI18n")
@BindingAdapter("setGuestNumberTitle")
fun setGuestNumberTitle(textView: TextView, guests: List<Guest>?){
    guests?.forEach {
        if (it.uiActive){
            textView.text = "Guest ${it.id.plus(1)}"
        }
    }
}
//
//@SuppressLint("SetTextI18n")
//@BindingAdapter("bind:addItem", "bind:addQuantity")
//fun addItemToOrder(textView: TextView, addItem: MenuItem?, addQuantity: Int){
//    if (addItem?.prices?.size == 1){
//        val price = addItem.prices[0].price * addQuantity
//        textView.text = "$%.${2}f".format(price)
//    }
//
//}

@SuppressLint("SetTextI18n")
@BindingAdapter("addRemoveIngredient")
fun addRemoveIngredient(textView: TextView, item: ItemIngredient){
    when (item.orderValue) {
        0 -> textView.text = "Remove ${item.name}"
        1 -> textView.text = item.name
        2 -> textView.text = "Add ${item.name}"
    }
}

@BindingAdapter("checkIngredientHeader")
fun checkIngredientHeader(textView: TextView, item: ItemIngredient){
    if (item.name.isNullOrEmpty()){
        textView.visibility = View.GONE
    }
}

@SuppressLint("SetTextI18n")
@BindingAdapter("setGuestButton")
fun setGuestButton(btn: Button, guest: Guest){
    println("In the binding")
    println(guest.id)
    btn.text = "Guest ${guest.id.plus(1)}"
    val color = ContextCompat.getColor(btn.context, android.R.color.transparent)
    if (guest.uiActive){
        val white = ContextCompat.getColor(btn.context, R.color.white)
        btn.setTextColor(ColorStateList.valueOf(white))
        btn.setCompoundDrawablesRelativeWithIntrinsicBounds(
            0,
            R.drawable.ic_user_white,
            0,
            0
        )
    }else{
        val offWhite = ContextCompat.getColor(btn.context, R.color.offWhite)
        btn.setTextColor(ColorStateList.valueOf(color))
        btn.setCompoundDrawablesRelativeWithIntrinsicBounds(
            0,
            R.drawable.ic_user_offwhite,
            0,
            0
        )
    }
}