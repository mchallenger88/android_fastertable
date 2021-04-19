package com.fastertable.fastertable.adapters

import android.annotation.SuppressLint
import android.widget.Button
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fastertable.fastertable.data.models.*
import java.time.ZoneId
import java.time.format.DateTimeFormatter


@SuppressLint("SetTextI18n")
@BindingAdapter("intToString")
fun intToString(textView: TextView, value: Int) {
    textView.text = value.toString()
}


@BindingAdapter("orderTypeTableNumber")
fun orderTypeTableNumber(textView: TextView, order: Order) {
    if (order.orderType == "Takeout"){
        textView.text = order.takeOutCustomer?.name
    }

    if (order.orderType == "Counter"){
        textView.text = ""
    }

    if (order.orderType == "Table"){
        textView.text = order.tableNumber.toString()
    }
}

@SuppressLint("SetTextI18n")
@BindingAdapter("telephone")
fun telephoneFormat(textView: TextView, value: String?){
    if (value?.contains("-") == true){
        textView.text = value
    }else{
        textView.text = "${value?.substring(0, 3)}-${value?.substring(3, 6)}-${value?.substring(6, 10)}"
    }
}

@SuppressLint("SetTextI18n")
@BindingAdapter("terminalUsing")
fun setTerminalName(textView: TextView, terminal: Terminal?){
    if (terminal != null) {
        textView.text = "${terminal.terminalName}"
    }
}

@BindingAdapter("timeStamp")
fun timeStamp(textView: TextView, value: Long?) {

    if (value != null){
        textView.text = DateTimeFormatter.ofPattern("h:mm a").withZone(ZoneId.systemDefault())
            .format(java.time.Instant.ofEpochSecond(value))
    }else{
        textView.text = ""
    }

}

@SuppressLint("SetTextI18n")
@BindingAdapter("modSelection")
fun modSelectionText(textView: TextView, mod: Modifier){
    if (mod.selectionLimitMin == 0 && mod.selectionLimitMax == 0){
        textView.text = ""
    }

    if (mod.selectionLimitMin == 0 && mod.selectionLimitMax > 0){
        textView.text = "Select as many options as needed"
    }

    if (mod.selectionLimitMin == 1 && mod.selectionLimitMax == 1){
        textView.text = "Selection required"
    }

    if (mod.selectionLimitMin >= 1 && mod.selectionLimitMin > 1){
        textView.text = "Select at least one"
    }

//    if (mod.selectionLimitMin > 1 && mod.selectionLimitMax > 1){
//        textView.text = "Select at least ${mod.selectionLimitMin}"
//    }
}

//@BindingAdapter("orderListData")
//fun bindRecyclerView(recyclerView: RecyclerView, data: List<Order>?) {
//    val adapter = recyclerView.adapter as OrderListAdapter
//    adapter.submitList(data)
//}

@BindingAdapter("priceDouble")
fun bindDouble(textView: TextView, value: Double){
    textView.text = "$%.${2}f".format(value)
}

@BindingAdapter("itemQuantity")
fun bindInteger(textView: TextView, value: Int){

    textView.text = "$value" + "x"
}

@BindingAdapter("itemPrice")
fun getItemPrice(textView: TextView, item: OrderItem){
     textView.text = "$%.${2}f".format(item.getExtendedPrice())
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

@BindingAdapter("itemListData")
fun bindRecyclerView(recyclerView: RecyclerView?, data: List<OrderItem>?) {
    val adapter = recyclerView?.adapter as OrderItemAdapter
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
@BindingAdapter("setFullOrderType")
fun fullOrderType(textView: TextView, orderType: String?){
    if (orderType != null){
        textView.text = "$orderType Order"
    }else{
        textView.text = "Order"
    }

}

@SuppressLint("SetTextI18n")
@BindingAdapter("setGuestNumberTitle")
fun setGuestNumberTitle(textView: TextView, guestNumber: Int?){
    if (guestNumber != null){
        textView.text = "Guest ${guestNumber.toString()}"
    }else{
        textView.text = "Guest"
    }
}

@SuppressLint("SetTextI18n")
@BindingAdapter("bind:addItem", "bind:addQuantity")
fun addItemToOrder(textView: TextView, addItem: MenuItem?, addQuantity: Int){
    if (addItem?.prices?.size == 1){
        val price = addItem.prices[0].price * addQuantity
        textView.text = "$%.${2}f".format(price)
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