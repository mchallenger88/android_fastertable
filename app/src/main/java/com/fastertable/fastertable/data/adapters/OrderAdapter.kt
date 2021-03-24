package com.fastertable.fastertable.data.adapters

import com.fastertable.fastertable.data.Order
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonClass
import com.squareup.moshi.JsonQualifier
import com.squareup.moshi.ToJson
import okhttp3.ResponseBody

@Retention(AnnotationRetention.RUNTIME)
@JsonQualifier
annotation class WrappedOrderList

@JsonClass(generateAdapter = true)
data class OrderList(val items: List<Order>)

class OrderListAdapter {
    @WrappedOrderList
    @FromJson
    fun fromJson(json: OrderList): List<Order> {
        return json.items
    }

    @ToJson
    fun toJson(@WrappedOrderList value: List<Order>): OrderList {
        throw UnsupportedOperationException()
    }
}