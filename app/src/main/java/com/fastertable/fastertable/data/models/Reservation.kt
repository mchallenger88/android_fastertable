package com.fastertable.fastertable.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Reservation (
    val reservationType: String,
    val table: TableClass,
    val employeeId: String,
    val userName: String,
    val customer: Customer,
    val guestCount: Int,
    val notes: String,
    val specialEvent: String,
    val specialRelationship: String,
    val seatingRequest: String,
    val foodAlerts: String,
    val source: String,
    val startTime: Long,
    val seatTime: Long,
    val clearTime: Long,
    val reservationStatus: String,
    val seatingStatus: String,
    val lid: String,
    val active: Boolean,
    val id: String,
    val locationId: String,
    val archived: Boolean,
    val type: String,
    val _rid: String?,
    val _self: String?,
    val _etag: String?,
    val _attachments: String?,
    val _ts: Long?
): Parcelable

@Parcelize
data class ReservationSettings(
    val seatingDuration: Int,
    val callPartySize: Int,
    val id: String,
    val locationId: String,
    val archived: Boolean,
    val type: String,
    val _rid: String?,
    val _self: String?,
    val _etag: String?,
    val _attachments: String?,
    val _ts: Long?
): Parcelable

@Parcelize
data class QuarterHourIncrement(
    val id: Int,
    val increment: Int,
    val selected: Boolean,
): Parcelable

@Parcelize
data class BlockedTable(
    val tableId: Int,
    val startTime: Long,
    val endTime: Long,
    val default: Boolean,
//    val weekDay: keypairNumber,
): Parcelable

@Parcelize
data class BlockedTables(
    val blocks: ArrayList<BlockedTable>,
    val blockType: String,
    val date: Long,
    val day: Int,
    val id: String,
    val locationId: String,
    val archived: Boolean,
    val type: String,
    val _rid: String?,
    val _self: String?,
    val _etag: String?,
    val _attachments: String?,
    val _ts: Long?
): Parcelable

@Parcelize
data class UserReservation(
    val reservation: Reservation,
    val restaurant: Location,
): Parcelable


enum class ReservationType {
    Reservation,
    Waitlist
}

enum class ReservationSource {
    Internal,
    Web
}