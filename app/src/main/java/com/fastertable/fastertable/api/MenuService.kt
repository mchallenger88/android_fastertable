package com.fastertable.fastertable.api

import com.fastertable.fastertable.data.Menu
import com.fastertable.fastertable.data.Settings
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

interface MenuService {

    @GET("menus/{lid}")
    suspend fun getMenus(@Path("lid") lid: String?): List<Menu>

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
            val retrofitService: MenuService by lazy {
                retrofit.create(MenuService::class.java)
            }
        }
    }
}

class MenusHelper(private val apiService: MenuService.Companion.ApiService,
                     private val lid: String){

    suspend fun getMenus() = apiService.retrofitService.getMenus(lid)
}