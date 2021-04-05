package com.fastertable.fastertable.data.repository

import android.R
import android.app.Application
import androidx.annotation.WorkerThread
import com.fastertable.fastertable.api.MenuService
import com.fastertable.fastertable.api.MenusHelper
import com.fastertable.fastertable.data.Menu
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.File

class MenusRepository(private val app: Application) {
    @WorkerThread
    suspend fun saveMenus(rid: String): List<Menu>{
        val menus: List<Menu> = MenusHelper(MenuService.Companion.ApiService, rid).getMenus()
        val gson = Gson()
        val jsonString = gson.toJson(menus)
        val file= File(app.filesDir, "menus.json")
        file.writeText(jsonString)
        return menus
    }

    @WorkerThread
    fun getMenus(): List<Menu>? {
        var gson = Gson()
        if (File(app.filesDir, "menus.json").exists()){
            val bufferedReader: BufferedReader = File(app.filesDir, "menus.json").bufferedReader()
            val inputString = bufferedReader.use { it.readText() }
            val list = gson.fromJson<ArrayList<Menu>?>(inputString, object : TypeToken<List<Menu?>?>() {}.type)
            list.forEach{menu ->
                menu.categories.sortBy { it.category }
            }
            return list
        }
        return null
    }
}