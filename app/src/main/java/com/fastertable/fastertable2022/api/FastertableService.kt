package com.fastertable.fastertable2022.api

import com.fastertable.fastertable2022.data.models.*
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
    suspend fun getApprovals(@Body timeBasedRequest: TimeBasedRequest): Response<List<ApprovalOrderPayment>>

    @POST("approval/getapprovalsbypaymentid")
    suspend fun getApprovalsById(@Body idRequest: IdRequest): Response<List<Approval>>

    //Credit
    @GET
    suspend fun startCredit(@Url url: String): Response<TerminalResponse>

    @GET
    suspend fun cancelCredit(@Url url: String): Response<TerminalResponse>

    @POST("payments/getkey")
    suspend fun stageTransaction(@Body transaction: CayanCardTransaction): Response<StageResponse>

    @POST("payments/authorization")
    suspend fun manualAuthorization(@Body authorizationRequest: AuthorizationRequest): Response<TransactionResponse45>

    @POST("payments/tipadjust")
    suspend fun tipAdjustment(@Body transaction: AdjustTipTest): Response<TransactionResponse45>

    @POST("payments/capture")
    suspend fun captureTicket(@Body transaction: CaptureRequest): Response<TransactionResponse45>

    @POST("payments/void")
    suspend fun voidCreditPayment(@Body refundRequest: RefundRequest): Response<TransactionResponse45>

    @GET
    suspend fun initiateTransaction(@Url url: String): Response<CayanTransaction>

    //Checkout
    @POST("employees/getcheckout")
    suspend fun getCheckout(@Body checkoutRequest: CheckoutRequest): Response<ConfirmEmployee>

    @POST("login/checkout")
    suspend fun checkout(@Body checkoutCredentials: CheckoutCredentials): Response<UserClock>

    @POST("login/clockout")
    suspend fun clockout(@Body clockoutRequest: ClockOutCredentials): Response<UserClock>

    @POST("employees/getconfirmemployees")
    suspend fun getConfirmList(@Body companyTimeBasedRequest: CompanyTimeBasedRequest): Response<List<ConfirmEmployee>>

    @POST("login/confirmcheckout")
    suspend fun confirmCheckout(@Body checkoutCredentials: CheckoutCredentials): Response<UserClock>

    @POST("login/reopencheckout")
    suspend fun reopenCheckout(@Body reopenCheckoutRequest: ReopenCheckoutRequest): Response<UserClock>

    @POST("employees/getOnShiftEmployees")
    suspend fun getOnShiftEmployees(@Body companyTimeBasedRequest: CompanyTimeBasedRequest): Response<List<Employee>>

    //Floorplans
    @GET("restaurantfloorplans/getfloorplans/{lid}/{cid}")
    suspend fun getFloorplans(@Path("lid") lid: String?, @Path("cid") cid: String?): Response<List<RestaurantFloorplan>>

    @POST("restaurantfloorplans/createfloorplan")
    suspend fun saveFloorplan(@Body restaurantFloorplan: RestaurantFloorplan): Response<RestaurantFloorplan>

    @POST("restaurantfloorplans/updatefloorplan")
    suspend fun updateFloorplan(@Body restaurantFloorplan: RestaurantFloorplan): Response<RestaurantFloorplan>

    @DELETE("restaurantfloorplans/{id}/{cid}")
    suspend fun deleteFloorplan(@Path("id") id: String?, @Path("cid") cid: String?): Response<Void>

    @POST("employees/getemployeebyid")
    suspend fun getEmployee(@Body getEmployee: GetEmployee): Response<Employee>

}


