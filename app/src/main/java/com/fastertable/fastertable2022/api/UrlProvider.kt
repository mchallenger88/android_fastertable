package com.fastertable.fastertable2022.api

import com.fastertable.fastertable2022.common.Constants

class UrlProvider {
    fun getBaseUrl(): String {
        return Constants.BASE_URL
    }

    fun getDebugURL(): String{
        return Constants.DEBUG_BASE_URL
    }
}

