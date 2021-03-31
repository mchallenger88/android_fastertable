package com.fastertable.fastertable.data.repository

import android.app.Application
import androidx.annotation.WorkerThread
import com.fastertable.fastertable.api.CompanyHelper
import com.fastertable.fastertable.api.CompanyService
import com.fastertable.fastertable.api.OrderHelper
import com.fastertable.fastertable.api.OrderService
import com.fastertable.fastertable.data.Company
import com.fastertable.fastertable.data.Order
import com.fastertable.fastertable.data.TimeBasedRequest
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class OrderRepository(private val app: Application) {
    @WorkerThread
    suspend fun getOrder(orderId: String, lid: String): Order {
        val order = OrderService.Companion.ApiService.retrofitService.getOrder(orderId, lid)
        //Save order json to file
        val gson = Gson()
        val jsonString = gson.toJson(order)
        val file= File(app.filesDir, "order.json")
        file.writeText(jsonString)
        return order
    }

    @WorkerThread
    suspend fun getOrders(midnight: Long, rid: String): List<Order>{
        val orders = OrderService.Companion.ApiService.retrofitService.getOrders(midnight, rid)
        //Save order json to file
        val gson = Gson()
        val jsonString = gson.toJson(orders)
        val file= File(app.filesDir, "orders.json")
        file.writeText(jsonString)
        return orders
    }
}