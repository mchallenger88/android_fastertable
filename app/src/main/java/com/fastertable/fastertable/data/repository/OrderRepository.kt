package com.fastertable.fastertable.data.repository

import android.app.Application
import com.fastertable.fastertable.api.*
import com.fastertable.fastertable.data.models.*
import com.fastertable.fastertable.utils.GlobalUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.File
import java.lang.RuntimeException
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class SaveOrder @Inject constructor(private val saveOrderUseCase: SaveOrderUseCase,
                                    private val orderRepository: OrderRepository){
    suspend fun saveOrder(order: Order): Order{
        val o: Order
        val result = saveOrderUseCase.saveOrder(order)
        if (result is SaveOrderUseCase.Result.Success){
            println("XXXXXXXXXXXXXXXXXXXXXX: Save Order Step 2")
            orderRepository.saveOrder(result.order)
            return result.order
        }
        else{
            throw RuntimeException("fetch failure")
        }
    }
}

class SaveGiftOrder @Inject constructor(private val saveOrderUseCase: SaveOrderUseCase,
                                    private val orderRepository: OrderRepository){
    suspend fun saveOrder(order: Order): Order?{
        val result = saveOrderUseCase.saveOrder(order)
        if (result is SaveOrderUseCase.Result.Success){
            return result.order
        }
        else{
            return null
        }
    }
}

class UpdateOrder @Inject constructor(private val updateOrderUseCase: UpdateOrderUseCase,
                                    private val orderRepository: OrderRepository){
    suspend fun saveOrder(order: Order): Order{
        val o: Order
        val result = updateOrderUseCase.saveOrder(order)
        if (result is UpdateOrderUseCase.Result.Success){
            o = result.order
            orderRepository.saveOrder(o)
            return o
        }else{
            throw RuntimeException("fetch failure")
        }
    }
}

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

class GetOnShiftEmployees @Inject constructor(private val getOnShiftServersUseCase: GetOnShiftServersUseCase){
    suspend fun getEmployees(companyTimeBasedRequest: CompanyTimeBasedRequest): List<Employee>{
        val result = getOnShiftServersUseCase.getEmployees(companyTimeBasedRequest)
        if (result is GetOnShiftServersUseCase.Result.Success){
            return result.employees
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


    fun getOrder(): Order?{
        val gson = Gson()
        if (File(app.filesDir, "order.json").exists()){
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

    fun getOrderById(id: String): Order? {
        val orders = getOrdersFromFile()
        if (orders != null) {
            val o = orders.find{it -> it.id == id}!!
            o.setActiveGuestFirst()
            return o
        }
        return null
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

    fun createNewOrder(orderType: String, settings: Settings, user: OpsAuth, tableNumber: Int?, takeOutCustomer: TakeOutCustomer?): Order {
        val newGuest = Guest(
            id = 0,
            startTime = GlobalUtils().getNowEpoch(),
            orderItems = null,
            subTotal = null,
            tax = null,
            gratuity = 0.00,
            total = null,
            uiActive = true
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
            note = "",

            customer = null,
            takeOutCustomer = takeOutCustomer,
            outsideDelivery = null,

            orderFees = null,
            orderDiscount = null,
            pendingApproval = false,

            gratuity = 0.00,
            subTotal = 0.00,
            tax = 0.00,
            taxRate = settings.taxRate.rate.times(0.01)    ,
            total = 0.00,

            accepted = null,
            estReadyTime = null,
            estDeliveryTime = null,
            transfer = null,

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
        if (file.exists()){
            file.delete()
        }
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

    fun clearNewOrder(){
        val gson = Gson()
        val file= File(app.filesDir, "new_order.json")
        file.delete()
    }

    fun clearOrder(){
        val gson = Gson()
        val file= File(app.filesDir, "order.json")
        file.delete()
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