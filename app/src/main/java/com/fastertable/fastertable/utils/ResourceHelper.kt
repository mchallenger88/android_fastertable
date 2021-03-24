package com.fastertable.fastertable.utils

import android.content.Context
import com.fastertable.fastertable.R

class ResourceHelper(private val applicationContext: Context) {

    val defaultHomeText
        get() = applicationContext.getString(R.string.app_name)
}