package com.fastertable.fastertable.utils

import android.content.Context
import com.fastertable.fastertable.R
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject


class ResourceHelper (private val applicationContext: Context) {

    val defaultHomeText
        get() = applicationContext.getString(R.string.app_name)

    val checkoutMessage
        get() = applicationContext.getString(R.string.your_checkout_is_complete)
}