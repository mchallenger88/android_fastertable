package com.fastertable.fastertable.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ftFloorPlan(
    val tables: ArrayList<TableClass>,
    val walls: ArrayList<WallClass>,
    val companyId: String,
    val name: String,
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
data class FloorPlanRequest (
    val locationId: String,
    val companyId: String,
): Parcelable

@Parcelize
data class TableClass (
    val id: Int?,
    val type: String,
    val left: Int,
    val top: Int,
    val rotate: Double?,
    val selected: String?,
    val maxSeats: Int?,
    val minSeats: Int?,
    val reserved: String?,
    val stationCSS: String?,
    val blocks: Boolean,
    val active: Boolean,
): Parcelable

@Parcelize
data class WallClass (
    val id: Int?,
    val left: Int?,
    val top: Int?,
    val height: Int?,
    val width: Int?,
    val css: String?,
): Parcelable

@Parcelize
data class TablePosition(
    val floorplan: ftFloorPlan,
    val table: TableClass,
    val newX: Int,
    val newY: Int,
    val new: Boolean,
    val oldId: Int,
): Parcelable

@Parcelize
data class WallPosition(
    val floorplan: ftFloorPlan,
    val wall: WallClass,
    val newX: Int,
    val newY: Int,
    val newWidth: Int,
    val newHeight: Int,
    val new: Boolean,
    val oldId: Int,
): Parcelable

@Parcelize
data class WallDialogSave(
    val wall: WallClass,
    val action: String,
    val new: Boolean,
    val oldId: Int,
): Parcelable


@Parcelize
data class TableDialogData(
    val floorplan: ftFloorPlan,
    val table: TableClass
): Parcelable

@Parcelize
data class WallDialogData(
    val floorplan: ftFloorPlan,
    val wall: WallClass
): Parcelable
