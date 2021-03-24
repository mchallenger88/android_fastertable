package com.fastertable.fastertable.api

import com.fastertable.fastertable.data.OpsAuth
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface UserService {
    @GET("login/{pin}/{cid}/{lid}/{time}/{midnight}")
    suspend fun loginUser(@Path("pin") pin: String,
                          @Path("cid") cid: String,
                          @Path("lid") lid: String,
                          @Path("time") time: Long,
                          @Path("midnight") midnight: Long): OpsAuth

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
            val retrofitService: UserService by lazy {
                retrofit.create(UserService::class.java)
            }
        }
    }
}

class UserHelper(private val apiService: UserService.Companion.ApiService,
                  private val pin: String, private val cid: String,
                 private val lid: String, private val now: Long, private val midnight: Long
){
    suspend fun loginUser() = apiService.retrofitService.loginUser(pin, cid, lid, now, midnight)

}