package com.fastertable.fastertable.utils

import android.content.Context
import java.time.OffsetDateTime

class GlobalUtils(context: Context) {


    fun getNowEpoch(): Long{
        val now: OffsetDateTime = OffsetDateTime.now()
        return now.toEpochSecond()
    }

    fun getMidnight(): Long{
        val now: OffsetDateTime = OffsetDateTime.now()
        val startOfDay: OffsetDateTime = now.withHour(0).withMinute(0).withSecond(0)
        return startOfDay.toEpochSecond()
    }
}