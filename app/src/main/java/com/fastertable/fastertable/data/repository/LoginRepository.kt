package com.fastertable.fastertable.data.repository

import android.app.Application
import android.content.Context
import androidx.annotation.WorkerThread
import com.fastertable.fastertable.api.*
import com.fastertable.fastertable.data.Company
import com.fastertable.fastertable.data.Menu
import com.fastertable.fastertable.data.Settings
import com.fastertable.fastertable.data.Terminal
import com.google.gson.Gson
import java.io.BufferedReader
import java.io.File


class LoginRepository(private val app: Application) {
    @WorkerThread
    suspend fun loginCompany(loginName: String?, password: String?): Company{
        val company = CompanyHelper(CompanyService.Companion.ApiService, loginName, password).geCompany()
        //Save company json to file
        val gson = Gson()
        val jsonString = gson.toJson(company)
        val file= File(app.filesDir, "company.json")
        file.writeText(jsonString)
        return company
    }

    @WorkerThread
    fun getCompany(): Company?{
        var gson = Gson()
        if (File(app.filesDir, "company.json").exists()){
            val bufferedReader: BufferedReader = File(app.filesDir, "company.json").bufferedReader()
            val inputString = bufferedReader.use { it.readText() }
            return gson.fromJson(inputString, Company::class.java)
        }
        return null
    }

    @WorkerThread
    suspend fun getRestaurantSettings(rid: String): Settings{
        val settings: Settings = SettingsHelper(SettingsService.Companion.ApiService, rid).getLocationSettings()
        //Save settings json to file
        val gson = Gson()
        val jsonString = gson.toJson(settings)
        val file= File(app.filesDir, "settings.json")
        file.writeText(jsonString)
        return settings
    }

    @WorkerThread
    suspend fun getSettings(): Settings?{
        var gson = Gson()
        if (File(app.filesDir, "settings.json").exists()){
            val bufferedReader: BufferedReader = File(app.filesDir, "settings.json").bufferedReader()
            val inputString = bufferedReader.use { it.readText() }
            return gson.fromJson(inputString, Settings::class.java)
        }
        return null
    }

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
    fun getMenus(): Menu? {
        var gson = Gson()
        if (File(app.filesDir, "menus.json").exists()){
            val bufferedReader: BufferedReader = File(app.filesDir, "menus.json").bufferedReader()
            val inputString = bufferedReader.use { it.readText() }
            return gson.fromJson(inputString, Menu::class.java)
        }
        return null
    }

    @WorkerThread
    suspend fun loginUser(pin: String, cid: String, rid: String, now: Long, midnight: Long){
        val user = UserHelper(UserService.Companion.ApiService, pin, cid, rid, now, midnight).loginUser()
        val gson = Gson()
        val jsonString = gson.toJson(user)
        val file= File(app.filesDir, "user.json")
        file.writeText(jsonString)
    }

    @WorkerThread
    fun getTerminal(): Terminal?{
        var gson = Gson()
        if (File(app.filesDir, "terminal.json").exists()){
            val bufferedReader: BufferedReader = File(app.filesDir, "terminal.json").bufferedReader()
            val inputString = bufferedReader.use { it.readText() }
            return gson.fromJson(inputString, Terminal::class.java)
        }
        return null
    }

    @WorkerThread
    fun saveTerminal(terminal: Terminal){
        val gson = Gson()
        val jsonString = gson.toJson(terminal)
        val file= File(app.filesDir, "terminal.json")
        file.writeText(jsonString)
    }

    @WorkerThread
    fun getStringSharedPreferences(key: String): String?{
        val sp = app.getSharedPreferences("restaurant", Context.MODE_PRIVATE)
        return sp.getString(key, null)

    }

    @WorkerThread
    fun setSharedPreferences(key: String, value: String){
        val sp = app.getSharedPreferences("restaurant", Context.MODE_PRIVATE)
        val editor = sp.edit()
        editor.putString(key, value)
        editor.apply()
    }
}