package com.fastertable.fastertable.ui.floorplan

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fastertable.fastertable.common.base.BaseViewModel
import com.fastertable.fastertable.data.models.IdLocation
import com.fastertable.fastertable.data.models.Order
import com.fastertable.fastertable.data.models.RestaurantTable
import com.fastertable.fastertable.data.models.TableType
import com.fastertable.fastertable.data.repository.LoginRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FloorplanViewModel @Inject constructor
    (private val loginRepository: LoginRepository,
    ) : BaseViewModel() {

    private val _tables = MutableLiveData<List<RestaurantTable>>()
    val tables: LiveData<List<RestaurantTable>>
        get() = _tables

    init {
        loadTables()
    }

    fun loadTables(){
        val table1 = RestaurantTable(
            id = 12,
            type = TableType.Round_Booth,
            rotate = 0,
            locked = false,
            reserved = false,
            active = false,
            id_location = IdLocation.TopCenter,
            maxSeats = 4,
            minSeats = 2,
            left = 500,
            top = 500
        )

        val table2 = RestaurantTable(
            id = 13,
            type = TableType.Booth,
            rotate = 0,
            locked = false,
            reserved = false,
            active = false,
            id_location = IdLocation.TopCenter,
            maxSeats = 4,
            minSeats = 2,
            left = 100,
            top = 100
        )

        val list = mutableListOf<RestaurantTable>()
        list.add(table1)
        list.add(table2)
        _tables.value = list
    }

    fun tableClicked(table: RestaurantTable){
        println("This is my table number ${table.id}")
    }
}