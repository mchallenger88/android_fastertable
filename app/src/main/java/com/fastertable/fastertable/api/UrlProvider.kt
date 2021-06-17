package com.fastertable.fastertable.api

import com.fastertable.fastertable.common.Constants

class UrlProvider {
    fun getBaseUrl(): String {
        return Constants.BASE_URL
    }

    fun getDebugURL(): String{
        return Constants.DEBUG_BASE_URL
    }
}

