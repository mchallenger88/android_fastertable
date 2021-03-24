package com.fastertable.fastertable.adapters

import android.annotation.SuppressLint
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fastertable.fastertable.data.Order
import com.fastertable.fastertable.data.OrderItem
import com.fastertable.fastertable.data.Terminal


@SuppressLint("SetTextI18n")
@BindingAdapter("intToString")
fun intToString(textView: TextView, value: Int) {
    textView.text = value.toString();
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

//@BindingAdapter("timeStamp")
//fun timeStamp(textView: TextView, value: Long) {
//
//   textView.text = DateTimeFormatter.ofPattern("h:mm a").withZone(ZoneId.systemDefault())
//            .format(java.time.Instant.ofEpochSecond(value))
//}
//
//@BindingAdapter("itemListData")
//fun bindRecyclerView(recyclerView: RecyclerView, data: List<OrderItem>?) {
//    val adapter = recyclerView.adapter as MenuItemAdapter
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