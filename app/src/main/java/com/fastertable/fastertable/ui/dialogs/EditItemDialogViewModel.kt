package com.fastertable.fastertable.ui.dialogs

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fastertable.fastertable.common.base.BaseViewModel
import com.fastertable.fastertable.data.models.*
import com.fastertable.fastertable.data.repository.LoginRepository
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

    private val _saveChanges = MutableLiveData<OrderItem?>()
    val saveChanges: LiveData<OrderItem?>
        get() = _saveChanges

    private val _showNotes = MutableLiveData(false)
    val showNotes: LiveData<Boolean>
        get() = _showNotes

    fun setMenuItem(menuItem: MenuItem){
        _menuItem.value = menuItem
    }

    fun setOrderItem(orderItem: OrderItem){
        _orderItem.value = orderItem
        orderItem.note?.let{
            if (it.isNotEmpty()){
                setShowNotes(true)
            }
        }
    }

    fun setShowNotes(b: Boolean){
        _showNotes.value = b
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

    fun onModItemClicked(item: OrderMod) {
        val menuItem = _menuItem.value
        item.mod.addQuantity(item.item)
        menuItem?.sumSurcharges()
        _menuItem.value = menuItem
    }

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

    fun saveChanges(){
        val menuItem = _menuItem.value
        val orderItem = _orderItem.value
        orderItem?.let{ oi ->
            oi.ingredients = menuItem?.ingredients
            oi.modifiers = menuItem?.modifiers
            val price = menuItem?.prices?.find { x -> x.isSelected }
            price?.let {
                oi.menuItemPrice = price
            }
            setSaveChanges(oi)
        }
    }

    fun setSaveChanges(orderItem: OrderItem?){
        _saveChanges.value = orderItem
    }

}