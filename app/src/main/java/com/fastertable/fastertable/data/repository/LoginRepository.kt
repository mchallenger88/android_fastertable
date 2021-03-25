package com.fastertable.fastertable.data.repository

import android.app.Application
import androidx.annotation.WorkerThread
import com.fastertable.fastertable.api.*
import com.fastertable.fastertable.data.Company
import com.fastertable.fastertable.data.Menu
import com.fastertable.fastertable.data.Settings
import com.google.gson.Gson
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
    suspend fun getMenus(rid: String): List<Menu>{
        val menus: List<Menu> = MenusHelper(MenuService.Companion.ApiService, rid).getMenus()
        val gson = Gson()
        val jsonString = gson.toJson(menus)
        val file= File(app.filesDir, "menus.json")
        file.writeText(jsonString)
        return menus
    }

    @WorkerThread
    suspend fun loginUser(pin: String, cid: String, rid: String, now: Long, midnight: Long){
        val user = UserHelper(UserService.Companion.ApiService, pin, cid, rid, now, midnight).loginUser()
        val gson = Gson()
        val jsonString = gson.toJson(user)
        val file= File(app.filesDir, "user.json")
        file.writeText(jsonString)
    }
}