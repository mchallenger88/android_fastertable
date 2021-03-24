package com.fastertable.fastertable.data.repository

import com.fastertable.fastertable.api.OrderHelper
import com.fastertable.fastertable.api.OrderService
import com.fastertable.fastertable.data.TimeBasedRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OrderRepository() {
    companion object {
        //    val orders: LiveData<List<Order>> = Transformations.map(database.fastertableDatabaseDao.get()) {
        //        it.asDomainModel()
        //    }
            suspend fun refreshOrders(time: TimeBasedRequest) {
                withContext(Dispatchers.IO) {

//                    val result = OrderHelper(OrderService.Companion.OrdersApi, time).getOrders()
                    val result = OrderHelper(OrderService.Companion.ApiService, time).getOrders()
                    println(result.size.toString())
        //            val data = JSONObject(result.toString())

        //            val data = JSONObject(result.toString())
        //            val asteroids = parseAsteroidsJsonResult(data)


                }
            }

            suspend fun getOrderById(id: String, lid: String){
                withContext(Dispatchers.IO){
                    val order = OrderService.Companion.ApiService.retrofitService.getOrder(id, lid)
                }
            }
    }
}