package com.fastertable.fastertable.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ReportDateRange (
    val startDate: Long,
    val endDate: Long,
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
