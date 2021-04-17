package com.fastertable.fastertable.api

import com.fastertable.fastertable.data.models.*
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface FastertableApi {

    //Company Login
    @GET("companies/{loginCode}/{password}")
    suspend fun getCompany(@Path("loginCode") loginCode: String?, @Path("password") password: String? ): Response<Company>

    //Location Settings
    @GET("settings/{lid}")
    suspend fun getLocationSettings(@Path("lid") lid: String?): Response<Settings>

    //Get Restaurant Menus
    @GET("menus/{lid}")
    suspend fun getMenus(@Path("lid") lid: String?): Response<List<Menu>>

    //User Login
    @GET("login/{pin}/{cid}/{lid}/{time}/{midnight}")
    suspend fun loginUser(@Path("pin") pin: String,
                          @Path("cid") cid: String,
                          @Path("lid") lid: String,
                          @Path("time") time: Long,
                          @Path("midnight") midnight: Long): Response<OpsAuth>

    //Order
    @GET("orders/")
    suspend fun getOrders(@Query("midnight") midnight: Long,
                          @Query("locationId") locationId: String): Response<List<Order>>

    @GET("orders/{id}/{lid}")
    suspend fun getOrder(@Path("id") id: String?, @Path("lid") lid: String?): Response<Order>
}


