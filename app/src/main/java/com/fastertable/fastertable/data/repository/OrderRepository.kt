package com.fastertable.fastertable.data.repository

import android.app.Application
import androidx.annotation.WorkerThread
import com.fastertable.fastertable.api.OrderService
import com.fastertable.fastertable.data.*
import com.fastertable.fastertable.utils.GlobalUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.File
import java.util.*
import kotlin.collections.ArrayList


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
    fun getOrdersFromFile(): List<Order>?{
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

    fun createNewOrder(orderType: String, settings: Settings, user: OpsAuth, tableNumber: Int?): Order{
        val newGuest: Guest = Guest(
            id = 1,
            startTime = GlobalUtils().getNowEpoch(),
            orderItems = null,
            subTotal = null,
            tax = null,
            gratuity = 0.00,
            total = null
        )

        val guests = ArrayList<Guest>()
        guests.add(newGuest)


        val order: Order = Order(
            orderType = orderType,
            orderNumber = 99,
            tableNumber = tableNumber,
            employeeId = user.employeeId,
            userName = user.userName,
            startTime = GlobalUtils().getNowEpoch(),
            closeTime = null,
            midnight = GlobalUtils().getMidnight(),
            orderStatus = "Started",
            kitchenStatus = false,
            rush = false,

            guests = guests,
            splitChecks = null,
            note = null,

            customer = null,
            takeOutCustomer = null,
            outsideDelivery = null,

            orderFees = null,
            orderDiscount = null,
            pendingApproval = false,

            gratuity = 0.00,
            subTotal = 0.00,
            tax = 0.00,
            total = 0.00,

            accepted = null,
            estReadyTime = null,
            estDeliveryTime = null,

            id = "O_" + UUID.randomUUID().toString(),
            locationId = settings.locationId,
            archived = false,
            type = "Order",
            _rid = null,
            _self = null,
            _etag = null,
            _attachments = null,
            _ts = null
        )

        val gson = Gson()
        val jsonString = gson.toJson(order)
        val file= File(app.filesDir, "new_order.json")
        file.writeText(jsonString)
        return order
    }

    fun getNewOrder(): Order?{
        var gson = Gson()
        if (File(app.filesDir, "new_order.json").exists()){
            val bufferedReader: BufferedReader = File(app.filesDir, "new_order.json").bufferedReader()
            val inputString = bufferedReader.use { it.readText() }
            return gson.fromJson(inputString, Order::class.java)
        }
        return null
    }
}