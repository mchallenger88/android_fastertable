package com.fastertable.fastertable.data.repository

import android.app.Application
import androidx.annotation.WorkerThread
import com.fastertable.fastertable.api.CompanyHelper
import com.fastertable.fastertable.api.CompanyService
import com.fastertable.fastertable.data.Company
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
}