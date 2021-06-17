package com.fastertable.fastertable.api

import com.fastertable.fastertable.data.models.*
import retrofit2.Response
import retrofit2.http.*

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

    @POST("orders/")
    suspend fun saveOrder(@Body order: Order): Response<Order>

    @POST("orders/{id}")
    suspend fun updateOrder(@Path("id") id: String, @Body order: Order): Response<Order>

    //Payment
    @POST("payments")
    suspend fun savePayment(@Body payment: Payment): Response<Payment>

    @POST("payments/{id}")
    suspend fun updatePayment(@Path("id")id: String, @Body payment: Payment): Response<Payment>

    @GET("payments/")
    suspend fun getPayments(@Query("midnight") midnight: Long,
                          @Query("locationId") locationId: String): Response<List<Payment>>

    @GET("payments/{id}/{lid}")
    suspend fun getPayment(@Path("id") id: String, @Path("lid") lid: String): Response<Payment>

    //Approvals
    //Payment
    @POST("approval")
    suspend fun saveApproval(@Body approval: Approval): Response<Approval>

    @POST("approval/{id}")
    suspend fun updateApproval(@Path("id")id: String, @Body approval: Approval): Response<Approval>

    @POST("approval/getapprovalsnew")
    suspend fun getApprovals(@Body timeBasedRequest: TimeBasedRequest): Response<List<Approval>>

    @GET("approval/{id}/{lid}")
    suspend fun getApproval(@Path("id") id: String?, @Path("lid") lid: String?): Response<Approval>

    //Credit
    @GET
    suspend fun startCredit(@Url url: String): Response<TerminalResponse>

    @GET
    suspend fun cancelCredit(@Url url: String): Response<TerminalResponse>

    @POST("payments/getkey")
    suspend fun stageTransaction(@Body transaction: CayanCardTransaction): Response<StageResponse>

    @POST("payments/tipadjust")
    suspend fun tipAdjustment(@Body transaction: AdjustTipTest): Response<TransactionResponse45>

    @POST("payments/capture")
    suspend fun captureTicket(@Body transaction: CaptureRequest): Response<TransactionResponse45>

    @GET
    suspend fun initiateTransaction(@Url url: String): Response<CayanTransaction>

    //Checkout
    @POST("employees/getcheckout")
    suspend fun getCheckout(@Body checkoutRequest: CheckoutRequest): Response<ConfirmEmployee>

}


