package com.fastertable.fastertable.data.repository

import android.app.Application
import androidx.annotation.WorkerThread
import com.fastertable.fastertable.api.CompanyService
import com.fastertable.fastertable.data.Company
import com.google.gson.Gson
import java.io.File

class LoginRepository(app: Application) {
    private val app = app
    @WorkerThread
    suspend fun loginCompany(loginName: String?, password: String?): Company{
        val company = CompanyService.Companion.ApiService.retrofitService.getCompany(loginName, password);//Save company json to file
        val gson = Gson()
        val jsonString = gson.toJson(company)
        val file= File(app.filesDir, "company.json")
        file.writeText(jsonString)
        return company

    }
}