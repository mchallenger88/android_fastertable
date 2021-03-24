package com.fastertable.fastertable.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Rating(
    val food: Int,
    val service: Int,
): Parcelable

@Parcelize
data class Review(
    val restaurantId: String,
    val rating: Rating,
    val user: ReviewUser,
    val visitDate: Long,
    val title: String,
    val description: String,
    val helpful: Int,
    val flag: Int,
    val approved: Boolean,
): Parcelable

@Parcelize
data class ReviewUser (
    val userId: String,
    val alias: String,
): Parcelable