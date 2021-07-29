package com.fastertable.fastertable.utils

import android.widget.DatePicker
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type
import java.util.*


enum class ApiStatus {
    SUCCESS,
    ERROR,
    LOADING
}

data class Resource<out T>(val status: ApiStatus, val data: T?, val message: String?) {
    companion object {
        fun <T> success(data: T): Resource<T> = Resource(
            status = ApiStatus.SUCCESS,
            data = data,
            message = null
        )

        fun <T> error(data: T?, message: String): Resource<T> =
                Resource(status = ApiStatus.ERROR, data = data, message = message)

        fun <T> loading(data: T?): Resource<T> = Resource(
            status = ApiStatus.LOADING,
            data = data,
            message = null
        )
    }
}

class NullOnEmptyConverterFactory : Converter.Factory() {
    override fun responseBodyConverter(
        type: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, *> {
        val delegate: Converter<ResponseBody, *> =
            retrofit.nextResponseBodyConverter<Any>(this, type, annotations)
        return Converter { body -> if (body.contentLength() == 0L) null else delegate.convert(body) }
    }
}

fun DatePicker.getDate(): Date {
    val calendar = Calendar.getInstance()
    calendar.set(year, month, dayOfMonth)
    return calendar.time
}