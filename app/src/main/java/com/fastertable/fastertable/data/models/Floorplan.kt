package com.fastertable.fastertable.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class RestaurantFloorPlan(
    val tables: ArrayList<RestaurantTable>,
    val floorplanWalls: ArrayList<FloorplanWall>?,
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
data class RestaurantTable (
    val id: Int,
    val type: TableType,
    val rotate: Int,
    val locked: Boolean,
    val reserved: Boolean,
    val active: Boolean,
    val id_location: IdLocation,
    val maxSeats: Int,
    val minSeats: Int,
    val left: Int,
    val top: Int,

): Parcelable

enum class IdLocation{
    TopLeft, TopCenter, TopRight,
    MiddleLeft, MiddleCenter, MiddleRight,
    BottomLeft, BottomCenter, BottomRight
}

enum class TableType{
    Booth, Round_Booth, Rect_Four, Rect_Horz_Four,
    Rect_Horz_Eight, Rect_Horz_Six, Rect_Horz_Ten,
    Rect_Six, Rect_Two, Round_Stool, Round_Eight,
    Round_Four, Round_Ten, Round_Two, Square_Stool,
    Square_Four, Square_Two
}

@Parcelize
data class FloorplanWall (
    val id: Int?,
    val left: Int?,
    val top: Int?,
    val height: Int?,
    val width: Int?,
): Parcelable

@Parcelize
data class TablePosition(
    val floorplan: RestaurantFloorPlan,
    val table: TableClass,
    val newX: Int,
    val newY: Int,
    val new: Boolean,
    val oldId: Int,
): Parcelable

@Parcelize
data class WallPosition(
    val floorplan: RestaurantFloorPlan,
    val floorplanWall: FloorplanWall,
    val newX: Int,
    val newY: Int,
    val newWidth: Int,
    val newHeight: Int,
    val new: Boolean,
    val oldId: Int,
): Parcelable

@Parcelize
data class WallDialogSave(
    val floorplanWall: FloorplanWall,
    val action: String,
    val new: Boolean,
    val oldId: Int,
): Parcelable


@Parcelize
data class TableDialogData(
    val floorplan: RestaurantFloorPlan,
    val table: TableClass
): Parcelable

@Parcelize
data class WallDialogData(
    val floorplan: RestaurantFloorPlan,
    val floorplanWall: FloorplanWall
): Parcelable
