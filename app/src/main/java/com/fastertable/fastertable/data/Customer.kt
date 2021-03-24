package com.fastertable.fastertable.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Customer(
    val firstName: String,
    val lastName: String,
    val companyName: String,
    val addresses: ArrayList<Address>,
    val telephones: ArrayList<Telephone>,
    val email: String,
    val notes: ArrayList<String>,
    val password: String,
    val salt: String,
    val orders: ArrayList<TokenOrder>,
    val confirmed: Boolean,
    val reviews: ArrayList<Review>,
    val alias: String,
    val avatar: String,
    val type: String,
    val _rid: String?,
    val _self: String?,
    val _etag: String?,
    val _attachments: String?,
    val _ts: Long?
): Parcelable

@Parcelize
data class TempCustomer(
    val name: String,
    val email: String,
    val mobile: String,
): Parcelable

@Parcelize
data class TakeOutCustomer(
    val telephone: String,
    val name: String,
    val notes: String,
): Parcelable

@Parcelize
data class DeliveryCustomer(
    val deliveryService: String,
    val orderNumber: String,
    val telephone: String,
    val name: String,
    val notes: String,
    val address: Address,
): Parcelable

@Parcelize
data class CustomerLogin(
    val email: String,
    val password: String,
    val loginType: String,
): Parcelable