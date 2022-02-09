package com.fastertable.fastertable2022.data.repository

import android.app.Application
import com.fastertable.fastertable2022.api.GetMenusUseCase
import com.fastertable.fastertable2022.data.models.Menu
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.File
import java.lang.RuntimeException
import javax.inject.Inject

class GetMenus @Inject constructor(private val getMenusUseCase: GetMenusUseCase,
                                   private val menusRepository: MenusRepository){
    suspend fun getAllMenus(lid: String){
        val menus: List<Menu>
        val result = getMenusUseCase.getMenus(lid)
        if (result is GetMenusUseCase.Result.Success){
            menus = result.menus
            menusRepository.saveMenus(menus)
        }else{
            throw RuntimeException("fetch failed")
        }
    }
}

class MenusRepository @Inject constructor(private val app: Application) {

    fun saveMenus(menus: List<Menu>): List<Menu>{
        val gson = Gson()
        val jsonString = gson.toJson(menus)
        val file= File(app.filesDir, "menus.json")
        file.writeText(jsonString)
        return menus
    }


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