package com.fastertable.fastertable.data.models

import android.os.Parcelable
import android.util.Log
import com.fastertable.fastertable.utils.round
import com.google.gson.Gson
import kotlinx.parcelize.Parcelize


@Parcelize
data class MenuItem(
    val id: String,
    val itemName: String,
    val itemDescription: String,
    val prices: MutableList<ItemPrice>,
    var modifiers: ArrayList<Modifier>,
    var ingredients: MutableList<ItemIngredient>,
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
    fun addOrderMod(modifierItem: ModifierItem){
        modifiers.forEach { mod ->
            mod.modifierItems.forEach{ item ->
                if (item.itemName == modifierItem.itemName){
                    item.quantity = modifierItem.quantity
                }
            }
        }
    }

    private fun allModifierItems(): List<ModifierItem>{
        val list = mutableListOf<ModifierItem>()
        modifiers.forEach { mod ->
            mod.modifierItems.forEach {
                list.add(it)
            }
        }
        return list
    }

    private fun ingredientSurchargeList(): List<ItemIngredient>{
        return ingredients.filter{ it.orderValue == 2}
    }


    fun sumSurcharges(){
        val modSum = allModifierItems().sumOf { it.quantity.times(it.surcharge) }
        val ingSum = ingredientSurchargeList().sumOf{ it.surcharge }
        val total = modSum.plus(ingSum).round(2)
        prices.forEach {
            it.modifiedPrice = total
        }
    }

    fun requirementsMet(): Boolean{
        var requireMet = true
        modifiers.forEach { mod ->
            var count = 0
            mod.modifierItems.forEach { mi ->
                if (mi.quantity > 0){
                    count = count.plus(mi.quantity)
                }
            }
            if (count < mod.selectionLimitMin){
                requireMet = false
            }
        }
        return requireMet
    }

}

@Parcelize
data class ItemPrice(
        var isSelected: Boolean = false,
        var quantity: Int = 1,
        val size: String,
        var price: Double,
        val discountPrice: Double?,
        var modifiedPrice: Double = price,
        val tax: String
): Parcelable

@Parcelize
data class ItemIngredient(
        val name: String,
        val surcharge: Double,
        var orderValue: Int,
): Parcelable{
    fun add(){
        if (orderValue < 2){
            orderValue = orderValue.plus(1)
        }
    }

    fun subtract(){
        if (orderValue > 0){
            orderValue = orderValue.minus(1)
        }
    }
}

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
): Parcelable{
        private fun sumQuantity(): Int{
            return modifierItems.sumOf { it.quantity }
        }

        private fun maxReached(): Boolean{
            return if (selectionLimitMax != 0){
                sumQuantity() >= selectionLimitMax
            }else{
                false
            }
        }

        fun addQuantity(item: ModifierItem){
            if (maxReached() || item.quantity == 3){
                item.quantity = 0
                return
            }
            if (!maxReached()){
                item.quantity = item.quantity.plus(1)
            }
        }
}

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
