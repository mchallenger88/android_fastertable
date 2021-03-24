package com.fastertable.fastertable.utils

enum class ApiStatus {
    SUCCESS,
    ERROR,
    LOADING
}

data class Resource<out T>(val status: ApiStatus, val data: T?, val message: String?) {
    companion object {
        fun <T> success(data: T): Resource<T> = Resource(status = ApiStatus.SUCCESS, data = data, message = null)

        fun <T> error(data: T?, message: String): Resource<T> =
                Resource(status = ApiStatus.ERROR, data = data, message = message)

        fun <T> loading(data: T?): Resource<T> = Resource(status = ApiStatus.LOADING, data = data, message = null)
    }
}

