package com.fastertable.fastertable.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.fastertable.fastertable.data.Order

@Database(entities = [DatabaseEntities.DataOrder::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract val fastertableDatabaseDao: FastertableDatabaseDao
}

private lateinit var INSTANCE: AppDatabase

fun getDatabase(context: Context): AppDatabase {
    synchronized(AppDatabase::class.java) {
        if (!::INSTANCE.isInitialized) {
            INSTANCE = Room.databaseBuilder(context.applicationContext,
                AppDatabase::class.java,
                "app_database").build()
        }
    }
    return INSTANCE
}