package com.fastertable.fastertable.api

import com.fastertable.fastertable.data.Order
import com.fastertable.fastertable.data.TimeBasedRequest
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface OrderService{
    @GET("orders/")
    suspend fun getOrders(@Query("midnight") midnight: Long,
                              @Query("locationId") locationId: String): List<Order>

    @GET("orders/{id}/{lid}")
    suspend fun getOrder(@Path("id") id: String?, @Path("lid") lid: String?): Order


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
            val retrofitService: OrderService by lazy {
                retrofit.create(OrderService::class.java)
            }
        }
        }
    }

    class OrderHelper(private val apiService: OrderService.Companion.ApiService,
                          private val timeBasedRequest: TimeBasedRequest
    ){

        private fun createJSONObject(time: TimeBasedRequest): RequestBody {
            val jsonObject = JSONObject()
            jsonObject.put("midnight", time.midnight)
            jsonObject.put("locationId", time.locationId)


            // Convert JSONObject to String
            val jsonObjectString = jsonObject.toString()

            // Create RequestBody ( We're not using any converter, like GsonConverter, MoshiConverter e.t.c, that's why we use RequestBody )
            return jsonObjectString.toRequestBody("application/json".toMediaTypeOrNull())
        }

        suspend fun getOrders() = apiService.retrofitService.getOrders(timeBasedRequest.midnight, timeBasedRequest.locationId)

    }

