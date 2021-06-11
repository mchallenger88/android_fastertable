package com.fastertable.fastertable.adapters

import android.content.res.ColorStateList
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.fastertable.fastertable.R
import com.fastertable.fastertable.data.models.ConfirmEmployee
import com.fastertable.fastertable.data.models.Order
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@BindingAdapter("totalOwed")
fun totalOwed(textView: TextView, ce: ConfirmEmployee?) {
    if (ce != null){
        if (ce.totalNegative){
            textView.text = textView.context.getString(R.string.total_owed_you)
        }else{
            textView.text = textView.context.getString(R.string.total_owed_house)
        }
    }
}

@BindingAdapter("checkoutNull")
fun checkoutNull(constraintLayout: ConstraintLayout, ce: Boolean) {
    if (ce){
        constraintLayout.visibility = View.GONE
    }else{
        constraintLayout.visibility = View.VISIBLE
    }
}

@BindingAdapter("checkoutNotNull")
fun checkoutNotNull(constraintLayout: ConstraintLayout, ce: Boolean) {
    if (ce){
        constraintLayout.visibility = View.VISIBLE
    }else{
        constraintLayout.visibility = View.GONE
    }
}

@BindingAdapter("checkoutTitle")
fun checkoutTitle(textView: TextView, op: Boolean?) {
    if (op != null){
        if (op){
            textView.text = textView.context.getString(R.string.checkout_orders)
        }else{
            textView.text = textView.context.getString(R.string.checkout_payments)
        }

    }
}

@BindingAdapter("orderClosed")
fun orderClosed(textView: TextView, order: Order) {
    val white = ContextCompat.getColor(textView.context, R.color.white)
    val red = ContextCompat.getColor(textView.context, R.color.secondaryLightColor)
    val black = ContextCompat.getColor(textView.context, R.color.default_text_color)
    if (order.closeTime != null){
        textView.setTextColor(ColorStateList.valueOf(black))
        textView.setBackgroundColor(white)
    }else{
        textView.setTextColor(ColorStateList.valueOf(white))
        textView.setBackgroundColor(red)
    }
}



//@BindingAdapter("totalTips")
//fun totalTips(textView: TextView, ce: ConfirmEmployee?) {
//    if (ce != null){
//        textView.text = textView.context.getString(R.string.total_tips, "%.${2}f".format(ce.creditTips))
//    }
//}

//@BindingAdapter("orderCount")
//fun orderCount(textView: TextView, ce: ConfirmEmployee?) {
//    if (ce != null){
//        textView.text = textView.context.getString(R.string.order_count, ce.orders.size.toString())
//    }
//}

@BindingAdapter("timeToString")
fun timeToString(textView: TextView, time: Long?) {
    if (time != null){
        val formatter = DateTimeFormatter.ofPattern("h:mm a").withZone(ZoneId.systemDefault())
            .format(java.time.Instant.ofEpochSecond(time))
        textView.text = formatter
    }else{
        textView.text = ""
    }
}

@BindingAdapter("txtClockOut")
fun txtClockOut(textView: TextView, ce: ConfirmEmployee?) {
    if (ce != null){
        if (ce.shifts?.clockOutTime != null){
            textView.text = textView.context.getString(R.string.clock_out)
        }else{
            textView.text = textView.context.getString(R.string.still_on_shift)
        }
    }
}

//@BindingAdapter("totalVoids")
//fun totalVoids(textView: TextView, ce: ConfirmEmployee?) {
//    if (ce != null){
//        textView.text = textView.context.getString(R.string.total_voids, "%.${2}f".format(ce.voidTotal))
//    }
//}
//
//@BindingAdapter("totalDiscounts")
//fun totalDiscounts(textView: TextView, ce: ConfirmEmployee?) {
//    if (ce != null){
//        textView.text = textView.context.getString(R.string.total_discounts, "%.${2}f".format(ce.discountTotal))
//    }
//}

//@BindingAdapter("busTipShare")
//fun busTipShare(textView: TextView, ce: ConfirmEmployee?) {
//    if (ce != null){
//        textView.text = textView.context.getString(R.string.bus_tip_share, "%.${2}f".format(ce.busShare))
//    }
//}
//
//@BindingAdapter("barTipShare")
//fun barTipShare(textView: TextView, ce: ConfirmEmployee?) {
//    if (ce != null){
//        textView.text = textView.context.getString(R.string.bar_tip_share, "%.${2}f".format(ce.barShare))
//    }
//}
