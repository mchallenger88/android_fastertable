package com.fastertable.fastertable.api

import com.fastertable.fastertable.data.Company
import com.fastertable.fastertable.utils.NullOnEmptyConverterFactory
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

interface CompanyService {

    @GET("companies/{loginCode}/{password}")
    suspend fun getCompany(@Path("loginCode") loginCode: String?,@Path("password") password: String? ): Company

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
                    .addConverterFactory( NullOnEmptyConverterFactory())
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build()
        }

        object ApiService {
            val retrofitService: CompanyService by lazy {
                retrofit.create(CompanyService::class.java)
            }
        }
    }
}

class CompanyHelper(private val apiService: CompanyService.Companion.ApiService,
                     private val loginCode: String?, private val password: String?){

    suspend fun geCompany() = apiService.retrofitService.getCompany(loginCode, password)
}