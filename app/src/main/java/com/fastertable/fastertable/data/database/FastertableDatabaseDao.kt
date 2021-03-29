package com.fastertable.fastertable.data.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fastertable.fastertable.data.Order

//@Dao
//interface  FastertableDatabaseDao {
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    fun insertAll(vararg orders: DatabaseEntities.DataOrder)
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    fun insertOrder(order: DatabaseEntities.DataOrder)
//}