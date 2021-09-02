package com.fastertable.fastertable.data.repository

import android.app.Application
import android.content.Context
import com.fastertable.fastertable.api.*
import com.fastertable.fastertable.data.models.*
import com.google.gson.Gson
import java.io.BufferedReader
import java.io.File
import javax.inject.Inject

class LoginCompany @Inject constructor(private val companyLoginUseCase: CompanyLoginUseCase,
                                        private val loginRepository: LoginRepository){
    suspend fun loginCompany(loginName: String, password: String) {
        var company: Company
        val result = companyLoginUseCase.companyLogin(loginName, password)
        if (result is CompanyLoginUseCase.Result.Success){
            company = result.company
            loginRepository.saveCompany(company)
        }else{
            throw RuntimeException("fetch failed")
        }
    }
}

class GetRestaurantSettings @Inject constructor(private val getSettingsUseCase: GetSettingsUseCase,
                                                private val loginRepository: LoginRepository){

    suspend fun getRestaurantSettings(rid: String) {
        val result = getSettingsUseCase.getSettings(rid)
        val settings: Settings

        if (result is GetSettingsUseCase.Result.Success){
            settings = result.settings
            loginRepository.saveRestaurantSettings(settings)
        }else{
            throw RuntimeException("fetch failed")
        }
    }

}

class LoginUser @Inject constructor(private val loginUserUseCase: LoginUserUseCase,
                                    private val loginRepository: LoginRepository){
    suspend fun loginUser(pin: String, cid: String, rid: String, now: Long, midnight: Long): OpsAuth{
        val result = loginUserUseCase.userLogin(pin, cid, rid, now, midnight)
        val user: OpsAuth

        if (result is LoginUserUseCase.Result.Success){
            user = result.user
            loginRepository.saveUserLogin(user)
            return user
        }else{
            throw RuntimeException("fetch failed")
        }
    }
}

class GetEmployeeById @Inject constructor(private val employeeUseCase: EmployeeUseCase){
    suspend fun getEmployee(employeeIds: GetEmployee): Employee{
        val result = employeeUseCase.getEmployee(employeeIds)
        if (result is EmployeeUseCase.Result.Success){
            return result.employee
        }else{
            throw java.lang.RuntimeException("fetch failed")
        }
    }
}

class ClockoutUser @Inject constructor(private val clockoutUseCase: ClockoutUseCase, private val loginRepository: LoginRepository){
    suspend fun clockout(clockoutRequest: ClockOutCredentials){
        val result = clockoutUseCase.clockOut(clockoutRequest)

        if (result is ClockoutUseCase.Result.Success){
            loginRepository.clockoutUser()
        }else{
            throw RuntimeException("fetch failed")
        }
    }
}

class LoginRepository @Inject constructor(private val app: Application) {

    fun saveCompany(company: Company) {
            //Save company json to file
            val gson = Gson()
            val jsonString = gson.toJson(company)
            val file = File(app.filesDir, "company.json")
            file.writeText(jsonString)
    }


    fun getCompany(): Company?{
        var gson = Gson()
        if (File(app.filesDir, "company.json").exists()){
            val bufferedReader: BufferedReader = File(app.filesDir, "company.json").bufferedReader()
            val inputString = bufferedReader.use { it.readText() }
            return gson.fromJson(inputString, Company::class.java)
        }
        return null
    }


    fun saveRestaurantSettings(settings: Settings) {
        //Save settings json to file
        val gson = Gson()
        val jsonString = gson.toJson(settings)
        val file= File(app.filesDir, "settings.json")
        file.delete()
        file.writeText(jsonString)
    }


    fun getSettings(): Settings?{
        var gson = Gson()
        if (File(app.filesDir, "settings.json").exists()){
            val bufferedReader: BufferedReader = File(app.filesDir, "settings.json").bufferedReader()
            val inputString = bufferedReader.use { it.readText() }
            return gson.fromJson(inputString, Settings::class.java)
        }
        return null
    }


    fun saveUserLogin(user: OpsAuth){
        val gson = Gson()
        val jsonString = gson.toJson(user)
        val file= File(app.filesDir, "user.json")
        file.writeText(jsonString)
    }

    fun clockoutUser(){
        val gson = Gson()
        val file = File(app.filesDir, "user.json")
        if (file.exists()){
            file.delete()
        }
    }

    fun getOpsUser(): OpsAuth?{
        var gson = Gson()
        if (File(app.filesDir, "user.json").exists()){
            val bufferedReader: BufferedReader = File(app.filesDir, "user.json").bufferedReader()
            val inputString = bufferedReader.use { it.readText() }
            val user = gson.fromJson(inputString, OpsAuth::class.java)
            return user
        }
        return null
    }

    fun clearUser(){
        val gson = Gson()
        val file= File(app.filesDir, "user.json")
        file.delete()
    }

    fun getTerminal(): Terminal?{
        var gson = Gson()
        if (File(app.filesDir, "terminal.json").exists()){
            val bufferedReader: BufferedReader = File(app.filesDir, "terminal.json").bufferedReader()
            val inputString = bufferedReader.use { it.readText() }
            return gson.fromJson(inputString, Terminal::class.java)
        }
        return null
    }

    fun saveTerminal(terminal: Terminal){
        val gson = Gson()
        val jsonString = gson.toJson(terminal)
        val file= File(app.filesDir, "terminal.json")
        file.writeText(jsonString)
    }

    fun getStringSharedPreferences(key: String): String?{
        val sp = app.getSharedPreferences("restaurant", Context.MODE_PRIVATE)
        return sp.getString(key, null)

    }

    fun setSharedPreferences(key: String, value: String){
        val sp = app.getSharedPreferences("restaurant", Context.MODE_PRIVATE)
        val editor = sp.edit()
        editor.putString(key, value)
        editor.apply()
    }
}