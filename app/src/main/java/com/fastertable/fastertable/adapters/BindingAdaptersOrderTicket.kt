package com.fastertable.fastertable.adapters

import android.annotation.SuppressLint
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.fastertable.fastertable.data.models.Order
import com.fastertable.fastertable.data.models.OrderItem
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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

@BindingAdapter("timeStamp")
fun timeStamp(textView: TextView, value: Long?) {

    if (value != null){
        textView.text = DateTimeFormatter.ofPattern("h:mm a").withZone(ZoneId.systemDefault())
            .format(java.time.Instant.ofEpochSecond(value))
    }else{
        textView.text = ""
    }
}

@BindingAdapter("itemQuantity")
fun bindItemQuantity(textView: TextView, value: Int){
    textView.text = "$value" + "x"
}


@BindingAdapter("itemPrice")
fun getItemPrice(textView: TextView, item: OrderItem){
    textView.text = "$%.${2}f".format(item.getExtendedPrice())
}

@SuppressLint("SetTextI18n")
@BindingAdapter("setFullOrderType")
fun fullOrderType(textView: TextView, order: Order){
    textView.text = "${order.orderType} Order"

}

@SuppressLint("SetTextI18n")
@BindingAdapter("orderLineMods")
fun addOrderLineMods(textView: TextView, item: OrderItem){
    if (item.orderMods?.size!! > 0){
        var mods: String = String()
        item.orderMods.forEach { mod ->
            mods += mod.itemName + ", "
        }
        mods = "- $mods"
        mods = mods.dropLast(2)
        textView.text = mods
        textView.visibility = View.VISIBLE
    }else{
        textView.visibility = View.GONE
    }
}

@SuppressLint("SetTextI18n")
@BindingAdapter("orderLineIngredients")
fun addOrderLineIngredients(textView: TextView, item: OrderItem){
    val flat = item.ingredients?.filter { it -> it.orderValue != 1 }
    if (flat?.size!! > 0){
        var ingredients: String = String()
        item.ingredients?.forEach{ ing ->
            if (ing.orderValue == 0){
                ingredients += "No ${ing.name.trim()}, "
            }
            if (ing.orderValue == 2){
                ingredients += "Extra ${ing.name.trim()}, "
            }
        }
        ingredients = "- $ingredients"
        ingredients = ingredients.dropLast(2)
        textView.text = ingredients
        textView.visibility = View.VISIBLE
    }else{
        textView.visibility = View.GONE
    }
}


@SuppressLint("SetTextI18n")
@BindingAdapter("orderLineNote")
fun addOrderLineNote(textView: TextView, item: OrderItem){
    if (item.note.isNotEmpty() && item.note !== "null"){
        textView.text = "Notes: ${item.note}"
        textView.visibility = View.VISIBLE
    }else{
        textView.visibility = View.GONE
    }
}

@BindingAdapter("showItemMoreButton")
fun showItemMoreButton(btn: ImageButton, item: OrderItem){
    if (item.status != "Started"){
        btn.visibility = View.GONE
    }
}

@BindingAdapter("toggleItemRush")
fun toggleItemRush(image: ImageView, item: OrderItem){
    println("In the Binding Adapter: ${item.rush}")
    if (item.rush){
        image.visibility = View.VISIBLE
    }else{
        image.visibility = View.INVISIBLE
    }
}

@BindingAdapter("toggleItemTakeout")
fun toggleItemTakeout(image: ImageView, item: OrderItem){
    println("In the Binding Adapter: ${item.rush}")
    if (item.takeOutFlag){
        image.visibility = View.VISIBLE
    }else{
        image.visibility = View.INVISIBLE
    }
}

@BindingAdapter("toggleItemNoMake")
fun toggleItemNoMake(image: ImageView, item: OrderItem){
    println("In the Binding Adapter: ${item.rush}")
    if (item.dontMake){
        image.visibility = View.VISIBLE
    }else{
        image.visibility = View.INVISIBLE
    }
}