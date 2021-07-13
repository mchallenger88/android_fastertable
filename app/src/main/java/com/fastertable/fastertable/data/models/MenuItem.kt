package com.fastertable.fastertable.data.models

import android.os.Parcelable
import com.google.gson.Gson
import kotlinx.parcelize.Parcelize


@Parcelize
data class MenuItem(
        val id: String,
        val itemName: String,
        val itemDescription: String,
        val prices: ArrayList<ItemPrice>,
        val modifiers: ArrayList<Modifier>,
        val ingredients: ArrayList<ItemIngredient>,
        val active: Boolean,
        val printer: Printer,
        val prepStation: PrepStation,
        val salesCategory: String,
        val kiosk: Boolean,
        val imageName: String,
        val categories: ArrayList<String>,
        val soldOut: Boolean,
        val alias: String,
        val avatar: String,
        val type: String,
        val _rid: String?,
        val _self: String?,
        val _etag: String?,
        val _attachments: String?,
        val _ts: Long?
): Parcelable{
    fun clone(): MenuItem{
        val menuItem: String = Gson().toJson(this, MenuItem::class.java)
        return Gson().fromJson(menuItem, MenuItem::class.java)
    }
}

@Parcelize
data class ItemPrice(
    val size: String,
    var price: Double,
    val discountPrice: Double?,
    val tax: String
): Parcelable

@Parcelize
data class ItemIngredient(
        val name: String,
        val surcharge: Double,
        var orderValue: Int,
): Parcelable

@Parcelize
data class MenuCategory(
        val id: String,
        val category: String,
        val displayOrder: Int,
        val menuItems: ArrayList<MenuItem>,
        val kioskDisplay: Boolean?,
        val imageName: String,
): Parcelable{
     fun getItems(){
          return menuItems.sortBy { it.itemName }
     }
}

@Parcelize
data class MenuItemSummary(
        val id: String,
        val name: String,
        val modifiers: ArrayList<String>,
): Parcelable

@Parcelize
data class MenuItemMenus
(
        val id: String,
        val name: String,
): Parcelable

@Parcelize
data class IngredientList(
        val id: String,
        val locationId: String,
        val ingredients: List<ItemIngredient>,

        ): Parcelable

@Parcelize
data class Modifier(
        val id: String,
        val modifierName: String,
        val purpose: String,
        val modifierItems: ArrayList<ModifierItem>,
        val selectionLimitMin: Int,
        val selectionLimitMax: Int,
        val active: Boolean,
        val type: String,
        val _rid: String?,
        val _self: String?,
        val _etag: String?,
        val _attachments: String?,
        val _ts: Long?,
        var arrayId: Double?
): Parcelable

@Parcelize
data class ModifierItem(
        val itemName: String,
        val surcharge: Double,
        var quantity: Int = 0
): Parcelable

@Parcelize
data class SalesCategory(
        val name: String,
        val taxCategory: TaxCategory,
): Parcelable

@Parcelize
data class TaxCategory(
        val id: String,
        val taxCategoryName: String,
        val taxCategoryPercentage: Double,
): Parcelable

@Parcelize
data class modValid(
        val modName: String,
        val valid: Boolean,
): Parcelable


enum class IngredientToggle {
    NONE,
    NORMAL,
    EXTRA
}
@Parcelize
data class addItemMenuCat(
        val menu: String,
        val category: String
): Parcelable

@Parcelize
data class DeleteRequest(
        val id: String,
        val locationId: String
): Parcelable

@Parcelize
data class IngredientChange(
        val item: ItemIngredient,
        val value: Int,
): Parcelable

@Parcelize
data class OrderOrderItem(
        val order: Order,
        val orderItem: OrderItem,
): Parcelable

@Parcelize
data class MenuLocation(
        val menuName: String,
        val categories: ArrayList<MenuCategory>,
        val catNames: ArrayList<String>,
): Parcelable
