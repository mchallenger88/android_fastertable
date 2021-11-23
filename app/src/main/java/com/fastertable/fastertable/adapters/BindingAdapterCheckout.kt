package com.fastertable.fastertable.adapters

import android.annotation.SuppressLint
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.fastertable.fastertable.R
import com.fastertable.fastertable.data.models.ConfirmEmployee
import com.fastertable.fastertable.data.models.Payment
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

@BindingAdapter("tipCaptureDisabled")
fun tipCaptureDisabled(button: Button, payment: Payment?) {
    button.isEnabled = true
    payment?.tickets?.forEach {
        if (it.uiActive){
            it.activePayment()?.creditCardTransactions?.forEach { cc ->
                if (cc.captureTotal != null){
                    button.isEnabled = false
                }
            }
        }
    }
}

@BindingAdapter("tipCapturedError")
fun tipCapturedError(textView: TextView, payment: Payment?) {
    textView.visibility = View.GONE
    payment?.tickets?.forEach {
        if (it.uiActive){
            it.activePayment()?.creditCardTransactions?.forEach { cc ->
                if (cc.captureTotal != null){
                    textView.visibility = View.VISIBLE
                }
            }
        }
    }
}

@BindingAdapter("confirmedNotConfirmed")
fun confirmedNotConfirmed(textView: TextView, ce: ConfirmEmployee?) {
    if (ce != null){
        if (ce.shifts?.checkout == null){
            textView.text = textView.context.getString(R.string.checkout_open)
        }

        if (!ce.shifts?.checkoutApproved!! && ce.shifts.checkout != null){
            textView.visibility = View.GONE
        }

        if (ce.shifts.checkout != null && ce.shifts.checkoutApproved){
            textView.text = textView.context.getString(R.string.confirmed)
        }
    }
}

@BindingAdapter("confirmedNotConfirmedButton")
fun confirmedNotConfirmedButton(button: Button, ce: ConfirmEmployee?) {
    if (ce != null){
        if (ce.shifts?.checkout == null){
            button.visibility = View.GONE
        }

        if (!ce.shifts?.checkoutApproved!! && ce.shifts.checkout != null){
            button.text = button.context.getString(R.string.confirm)
        }

        if (ce.shifts.checkout != null && ce.shifts.checkoutApproved){
            button.visibility = View.GONE
        }
    }
}

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

@SuppressLint("SetTextI18n")
@BindingAdapter("confirmTotalOwed")
fun confirmTotalOwed(textView: TextView, ce: ConfirmEmployee?) {
    val red = ContextCompat.getColor(textView.context, R.color.errorColor)
    val green = ContextCompat.getColor(textView.context, R.color.primary_payment_color)

    if (ce != null){
        textView.text = "$%.${2}f".format(ce.totalOwed)
        if (ce.totalNegative){
            textView.setTextColor(red)
        }else{
            textView.setTextColor(green)
        }
    }
}

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
