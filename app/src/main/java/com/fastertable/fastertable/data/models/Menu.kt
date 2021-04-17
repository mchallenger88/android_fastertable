package com.fastertable.fastertable.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Menu(
    val name:String,
    val startTime: String,
    val endTime: String,
    val isActive:Boolean,
    val categories: ArrayList<MenuCategory>,
    val menuItemIds: ArrayList<String>,
    val default: Boolean,
    val kioskDisplay: Boolean,
    val displayOrder: Int,
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
data class ShortMenu(
    val id: String,
    val name: String,
): Parcelable

@Parcelize
data class CategoryTreeNode (
    val children: ArrayList<CategoryMenuItems>,
    val category: String,
    val displayOrder: Int,
): Parcelable

@Parcelize
data class CategoryMenuItems(
    val id: String,
    val name: String,
): Parcelable



@Parcelize
data class doubleCategory(
    val Cat1: MenuCategory,
    val Cat2: MenuCategory,
): Parcelable

@Parcelize
data class CategoryIcon(
    val name: String,
    val code: String,
): Parcelable


val IconList = mutableMapOf<String, String>().apply { 
    this["Burgers"] = "e91d"; 
    this["Hot Dogs"] = "e91e"
    this["Sandwiches"] = "e91f"
    this["Eggs"] = "e920"
    this["Pancakes"] = "e921"
    this["Waffles"] = "e922"
    this["Breads"] = "e923"
    this["Pastries"] = "e924"
    this["Pasta"] = "e925"
    this["Pizza"] = "e926"
    this["Skillets"] = "e927"
    this["Omelettes"] = "e928"
    this["Crepes"] = "e929"
    this["Shakes"] = "e92a"
    this["Panini"] = "e92b"
    this["French Toast"] = "e92c"
    this["Healthy"] = "e92d"
    this["Rice"] = "e92e"
    this["Curry"] = "e92f"
    this["Appetizers"] = "e900"
    this["Salads"] = "e901"
    this["Soups"] = "e902"
    this["Entrees"] = "e903"
    this["Meats"] = "e904"
    this["Fish"] = "e905"
    this["Enchiladas"] = "e906"
    this["Burritos"] = "e907"
    this["Tacos"] = "e908"
    this["Side Dishes"] = "e909"
    this["Desserts"] = "e90a"
    this["Beverages"] = "e90b"
    this["Margaritas"] = "e90c"
    this["Beer"] = "e90d"
    this["Spirits"] = "e90e"
    this["Wine"] = "e90f"
    this["Vegetarian"] = "e910"
    this["Children"] = "e911"}



