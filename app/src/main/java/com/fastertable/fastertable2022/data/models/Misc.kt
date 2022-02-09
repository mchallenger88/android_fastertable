package com.fastertable.fastertable2022.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.*
import kotlin.collections.ArrayList

@Parcelize
data class ReportDateRange (
    val startDate: Long,
    val endDate: Long,
): Parcelable

@Parcelize
data class DateDialog (
    val date: Date,
    val source: String,
): Parcelable

@Parcelize
data class MessageDialogData(
        val continueX: Boolean,
        val message: String,
): Parcelable

@Parcelize
data class HourMinute (
        val hour: Int,
        val minute: Int,
): Parcelable

@Parcelize
data class DeletePrinterDialogData(
    val deletedPrinter: Printer,
    val printers: ArrayList<Printer>,
    val continueX: Boolean,
    val message: String,
): Parcelable

@Parcelize
data class OrderPayment(
    val order: Order,
    val payment: Payment
): Parcelable