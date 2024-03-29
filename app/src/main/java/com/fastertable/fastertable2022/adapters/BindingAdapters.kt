package com.fastertable.fastertable2022.adapters

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fastertable.fastertable2022.R
import com.fastertable.fastertable2022.data.models.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*


@SuppressLint("SetTextI18n")
@BindingAdapter("intToString")
fun intToString(textView: TextView, value: Int) {
    textView.text = value.toString()
}

@BindingAdapter( "dateToString")
fun dateToString(textView: TextView, date: LocalDate?){
    if (date != null){
        val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
        textView.text = date.format(formatter)
    }
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

@BindingAdapter("booleanToYesNo")
fun booleanToYesNo(textView: TextView, value: Boolean){
    if (value){
        textView.text = textView.context.getString(R.string.yes)
    }else{
        textView.text = textView.context.getString(R.string.no)
    }
}


@BindingAdapter("bindInt")
fun bindInt(textView: TextView, value: Int){
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
fun orderItemListData(recyclerView: RecyclerView?, order: Order?) {
    val adapter = recyclerView?.adapter as OrderItemAdapter
    val list = order?.orderItems?.filter { it.guestId == order.activeGuest }
    if (list != null){
        adapter.submitList(list)
        adapter.notifyDataSetChanged()
    }
}

@BindingAdapter("drinkListData")
fun drinkListData(recyclerView: RecyclerView?, data: List<ReorderDrink>?){
    if (data != null){
        val adapter = recyclerView?.adapter as DrinkListAdapter
        adapter.submitList(data)
        adapter.notifyDataSetChanged()
    }
}

@BindingAdapter("showClosedOrderMessage")
fun showClosedOrderMessage(textView: TextView, order: Order?){
    if (order?.closeTime != null){
        textView.visibility = View.VISIBLE
    }else{
        textView.visibility = View.GONE
    }
}

@BindingAdapter("setOrderNotes")
fun setOrderNotes(textView: TextView, order: Order?){
    if (order != null && order.orderType == "Takeout" && order.takeOutCustomer?.notes != ""){
        val note = order.takeOutCustomer?.notes.toString()
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

        textView.text = note
        textView.visibility = View.VISIBLE
    }else{
        textView.visibility = View.GONE
    }
}


@SuppressLint("SetTextI18n")
@BindingAdapter("setGuestNumberTitle")
fun setGuestNumberTitle(textView: TextView, activeGuest: Int){
    textView.text = "Guest $activeGuest"
}

@SuppressLint("SetTextI18n")
@BindingAdapter("setTableNumber")
fun setTableNumber(textView: TextView, order: Order?){
    if (order != null){
        if (order.tableNumber != null){
            textView.text = "Table ${order.tableNumber}"
        }else{
            textView.visibility = View.GONE
        }
    }else{
        textView.visibility = View.GONE
    }

}

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

@BindingAdapter("alertDialog")
fun setAlertDialog(textView: TextView, message: String?){
    if (message != null){
        textView.text = message
    }
}

@BindingAdapter("showProgress")
fun showProgress(progressBar: ProgressBar, b: Boolean){
    if (b){
        progressBar.visibility = View.VISIBLE
    }else{
        progressBar.visibility = View.GONE
    }
}

@BindingAdapter("disableTransfer")
fun disableTransfer(checkBox: CheckBox, order: Order){
    checkBox.isEnabled = order.closeTime == null
}

@BindingAdapter("drinkGuest")
fun drinkGuest(textView: TextView, drink: ReorderDrink?){
    if (drink != null){
        textView.text = "Guest ${drink.guestId}: "
    }
}

@BindingAdapter("disableTransferText")
fun disableTransferText(textView: TextView, order: Order){
    val disabled = ContextCompat.getColor(textView.context, R.color.disabled_text)
    if (order.closeTime != null){
        textView.setTextColor(disabled)
    }
}

@BindingAdapter("spinnerVisibility")
fun spinnerVisibility(progressBar: ProgressBar, b: Boolean){
    if (b){
        progressBar.visibility = View.VISIBLE
    }else{
        progressBar.visibility = View.INVISIBLE
    }
}

@BindingAdapter("enableButton")
fun enableButton(btn: Button, b: Boolean){
    btn.isEnabled = b
}

