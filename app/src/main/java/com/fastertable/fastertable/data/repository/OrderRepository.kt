package com.fastertable.fastertable.data.repository

import android.app.Application
import com.fastertable.fastertable.api.GetOrderUseCase
import com.fastertable.fastertable.api.GetOrdersUseCase
import com.fastertable.fastertable.data.models.Guest
import com.fastertable.fastertable.data.models.OpsAuth
import com.fastertable.fastertable.data.models.Order
import com.fastertable.fastertable.data.models.Settings
import com.fastertable.fastertable.utils.GlobalUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.File
import java.lang.RuntimeException
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class GetOrder @Inject constructor(private val getOrderUseCase: GetOrderUseCase,
                                   private val orderRepository: OrderRepository){

    suspend fun getOrder(id: String, lid: String){
        val order: Order
        val result = getOrderUseCase.getOrder(id, lid)
        if (result is GetOrderUseCase.Result.Success){
            order = result.order
            orderRepository.saveOrder(order)
        }else{
            throw RuntimeException("fetch failed")
        }
    }
}

class GetOrders @Inject constructor(private val getOrdersUseCase: GetOrdersUseCase,
                                    private val orderRepository: OrderRepository){

    suspend fun getOrders(midnight: Long, rid: String){
        val orders: List<Order>
        val result = getOrdersUseCase.getOrders(midnight, rid)
        if (result is GetOrdersUseCase.Result.Success){
            orders = result.orders
            orderRepository.saveOrders(orders)
        }else{
            throw RuntimeException("fetch failed")
        }
    }
}

class OrderRepository @Inject constructor(private val app: Application) {

    fun saveOrder(order: Order) {
        //Save order json to file
        val gson = Gson()
        val jsonString = gson.toJson(order)
        val file= File(app.filesDir, "order.json")
        file.writeText(jsonString)
    }

    fun getOrderFromFile(): Order?{
        val gson = Gson()
        if (File(app.filesDir, "orders.json").exists()){
            val bufferedReader: BufferedReader = File(app.filesDir, "order.json").bufferedReader()
            val inputString = bufferedReader.use { it.readText() }
            return gson.fromJson(inputString, Order::class.java)
        }
        return null
    }

    fun saveOrders(orders: List<Order>){
        //Save order json to file
        val gson = Gson()
        val jsonString = gson.toJson(orders)
        val file= File(app.filesDir, "orders.json")
        file.writeText(jsonString)
    }

    fun getOrdersFromFile(): List<Order>?{
        val gson = Gson()
        if (File(app.filesDir, "orders.json").exists()){
            val bufferedReader: BufferedReader = File(app.filesDir, "orders.json").bufferedReader()
            val inputString = bufferedReader.use { it.readText() }
            val arrayList: ArrayList<Order> = gson.fromJson(inputString, object : TypeToken<List<Order?>?>() {}.type)
            val list: List<Order> = arrayList
            return list
        }
        return null
    }

    fun createNewOrder(orderType: String, settings: Settings, user: OpsAuth, tableNumber: Int?): Order {
        val newGuest = Guest(
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


        val order = Order(
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
        file.writeText("")
        file.writeText(jsonString)
        return order
    }

    fun saveNewOrder(order: Order) {
        //Save order json to file
        val gson = Gson()
        val jsonString = gson.toJson(order)
        val file= File(app.filesDir, "new_order.json")
        file.writeText(jsonString)
    }

    fun getNewOrder(): Order?{
        val gson = Gson()
        if (File(app.filesDir, "new_order.json").exists()){
            val bufferedReader: BufferedReader = File(app.filesDir, "new_order.json").bufferedReader()
            val inputString = bufferedReader.use { it.readText() }
            return gson.fromJson(inputString, Order::class.java)
        }
        return null
    }
}