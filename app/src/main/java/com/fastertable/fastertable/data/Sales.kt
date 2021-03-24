package com.fastertable.fastertable.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PaymentSelected (
    val payment: Payment,
    val selected: Boolean
): Parcelable

@Parcelize
data class Refund (
        val t: PayTicket,
        val amount: Double
): Parcelable