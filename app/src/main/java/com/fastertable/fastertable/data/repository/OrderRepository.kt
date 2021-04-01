package com.fastertable.fastertable.data.repository

import android.app.Application
import androidx.annotation.WorkerThread
import com.fastertable.fastertable.api.OrderService
import com.fastertable.fastertable.data.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
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

    @WorkerThread
    suspend fun getOrdersFromFile(): List<Order>?{
        var gson = Gson()
        if (File(app.filesDir, "orders.json").exists()){
            val bufferedReader: BufferedReader = File(app.filesDir, "orders.json").bufferedReader()
            val inputString = bufferedReader.use { it.readText() }
            val arrayList: ArrayList<Order> = gson.fromJson(inputString, object : TypeToken<List<Order?>?>() {}.type)
            val list: List<Order> = arrayList
            return list
        }
        return null
    }
}