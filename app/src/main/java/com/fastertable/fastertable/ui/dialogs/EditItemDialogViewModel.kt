package com.fastertable.fastertable.ui.dialogs

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fastertable.fastertable.common.base.BaseViewModel
import com.fastertable.fastertable.data.models.*
import com.fastertable.fastertable.data.repository.LoginRepository
import com.fastertable.fastertable.ui.order.AddSubtract
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EditItemDialogViewModel @Inject constructor (
    private val loginRepository: LoginRepository,): BaseViewModel() {

    private val _menuItem = MutableLiveData<MenuItem?>()
    val menuItem: LiveData<MenuItem?>
        get() = _menuItem

    private val _orderItem = MutableLiveData<OrderItem?>()
    val orderItem: LiveData<OrderItem?>
        get() = _orderItem

    fun setMenuItem(menuItem: MenuItem){
        _menuItem.value = menuItem
    }

    fun setOrderItem(orderItem: OrderItem){
        _orderItem.value = orderItem
        val menuItem = _menuItem.value!!
//        menuItem.modifiers.forEach { mod ->
//            if (mod.modifierItems.)
//         }
    }

    fun decreaseQuantity() {
        _menuItem.value?.prices?.forEach { price ->
            if (price.quantity > 1) {
                price.quantity = price.quantity.minus(1)
            }
        }
        _menuItem.value = _menuItem.value
    }


    fun increaseQuantity() {
        _menuItem.value?.prices?.forEach { price ->
            price.quantity = price.quantity.plus(1)
        }
        _menuItem.value = _menuItem.value
    }

    fun changeSelectedPrice(price: ItemPrice){
        _menuItem.value?.prices?.forEach { p ->
            p.isSelected = p.size == price.size
        }
        _menuItem.value = _menuItem.value
    }

    // region Modifier Functions

    fun onModItemClicked(item: OrderMod) {
        val menuItem = _menuItem.value
        item.mod.addQuantity(item.item)
        menuItem?.sumSurcharges()
        _menuItem.value = menuItem
    }

    //endregion

    //region Ingredient Functions

    fun onIngredientClicked(item: IngredientChange){
        val menuItem = _menuItem.value

        if (item.value == 1){
            item.item.add()
        }

        if (item.value == -1){
            item.item.subtract()
        }
        menuItem?.sumSurcharges()
        _menuItem.value = menuItem
    }
    //endregion
}