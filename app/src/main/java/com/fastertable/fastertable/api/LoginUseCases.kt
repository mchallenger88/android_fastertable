package com.fastertable.fastertable.api

import com.fastertable.fastertable.data.models.Company
import com.fastertable.fastertable.data.models.Menu
import com.fastertable.fastertable.data.models.OpsAuth
import com.fastertable.fastertable.data.models.Settings
import com.fastertable.fastertable.utils.NullOnEmptyConverterFactory
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.io.File
import javax.inject.Inject
import kotlin.reflect.jvm.internal.impl.serialization.deserialization.LocalClassifierTypeSettings


class CompanyLoginUseCase @Inject constructor(private val fastertableApi: FastertableApi){

    sealed class Result {
        data class Success(val company: Company) : Result()
        object Failure: Result()
    }

    suspend fun companyLogin(loginCode: String, password: String): Result {
        return withContext(Dispatchers.IO) {
            try {
                val response = fastertableApi.getCompany(loginCode, password)
                if (response.isSuccessful && response.body() != null) {
                    return@withContext Result.Success(response.body()!!)
                } else {
                    return@withContext Result.Failure
                }
            } catch (t: Throwable) {
                if (t !is CancellationException) {
                    return@withContext Result.Failure
                } else {
                    throw t
                }
            }
        }
    }
}

class GetSettingsUseCase @Inject constructor(private val fastertableApi: FastertableApi){

    sealed class Result {
        data class Success(val settings: Settings) : Result()
        object Failure: Result()
    }

    suspend fun getSettings(lid: String): Result {
        return withContext(Dispatchers.IO) {
           try{
               val response = fastertableApi.getLocationSettings(lid)
               if (response.isSuccessful && response.body() != null){
                   return@withContext Result.Success(response.body()!!)
               }else{
                   return@withContext Result.Failure
               }
           }catch (t: Throwable){
               if (t !is CancellationException){
                   return@withContext Result.Failure
               }else{
                   throw t
               }
           }
        }
    }
}

class LoginUserUseCase @Inject constructor(private val fastertableApi: FastertableApi){
    sealed class Result {
        data class Success(val user: OpsAuth) : Result()
        object Failure: Result()
    }

    suspend fun userLogin(pin: String, cid: String, rid: String, now: Long, midnight: Long): Result {
        return withContext(Dispatchers.IO){
            try{
                val response = fastertableApi.loginUser(pin, cid, rid, now, midnight)
                if (response.isSuccessful && response.body() != null){
                    return@withContext Result.Success(response.body()!!)
                }else{
                    return@withContext Result.Failure
                }
            }catch (t: Throwable){
                if (t !is CancellationException){
                    return@withContext Result.Failure
                }else{
                    throw t
                }
            }
        }
    }
}

