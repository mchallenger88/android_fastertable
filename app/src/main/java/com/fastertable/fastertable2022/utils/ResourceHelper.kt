package com.fastertable.fastertable2022.utils

import android.content.Context
import com.fastertable.fastertable2022.R


class ResourceHelper (private val applicationContext: Context) {

    val defaultHomeText
        get() = applicationContext.getString(R.string.app_name)

    val checkoutMessage
        get() = applicationContext.getString(R.string.your_checkout_is_complete)
}