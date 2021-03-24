package com.fastertable.fastertable.api

import com.fastertable.fastertable.data.Order
import com.fastertable.fastertable.data.Settings
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

interface SettingsService {

    @GET("settings/{lid}")
    suspend fun getLocationSettings(@Path("lid") lid: String?): Settings

    companion object {
        private const val BASE_URL = "https://datadev.fastertable.com/api/"
        private val gson: Gson by lazy {
            GsonBuilder().setLenient().create()
        }

        private val interceptor: HttpLoggingInterceptor by lazy {
            HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
        }

        private val httpClient: OkHttpClient by lazy {
            OkHttpClient.Builder().addInterceptor(interceptor).build()
        }

        private val retrofit: Retrofit by lazy {
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
        }

        object ApiService {
            val retrofitService: SettingsService by lazy {
                retrofit.create(SettingsService::class.java)
            }
        }
    }
}

class SettingsHelper(private val apiService: SettingsService.Companion.ApiService,
                     private val lid: String){

    suspend fun getLocationSettings() = apiService.retrofitService.getLocationSettings(lid)
}