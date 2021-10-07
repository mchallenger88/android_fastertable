package com.fastertable.fastertable.data.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class RestaurantFloorplan(
    var tables: ArrayList<RestaurantTable>,
    val walls: ArrayList<FloorplanWall>,
    val companyId: String,
    var name: String,
    val id: String,
    @SerializedName("locationid")
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
    var id: Int,
    var type: String,
    var rotate: Int,
    var locked: Boolean,
    val reserved: Boolean,
    var active: Boolean,
    var id_location: String,
    var maxSeats: Int,
    var minSeats: Int,
    var left: Int,
    var top: Int,
    var isCombination: Boolean,
    var combinationTables: ArrayList<RestaurantTable>?
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

enum class WallDirection{
    Vertical, Horizontal
}


@Parcelize
data class FloorplanWall (
    val id: Int?,
    var left: Int,
    var top: Int,
    val height: Int?,
    var width: Int?,
    var direction:String?,
    var thickness: Int?
): Parcelable

@Parcelize
data class TablePosition(
    val floorplan: RestaurantFloorplan,
    val table: TableClass,
    val newX: Int,
    val newY: Int,
    val new: Boolean,
    val oldId: Int,
): Parcelable

@Parcelize
data class WallPosition(
    val floorplan: RestaurantFloorplan,
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
    val floorplan: RestaurantFloorplan,
    val table: TableClass
): Parcelable

@Parcelize
data class WallDialogData(
    val floorplan: RestaurantFloorplan,
    val floorplanWall: FloorplanWall
): Parcelable
