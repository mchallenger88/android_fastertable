package com.fastertable.fastertable.utils

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.widget.AutoCompleteTextView
import com.fastertable.fastertable.R
import java.time.*


class GlobalUtils() {


    fun getNowEpoch(): Long{
        val now: OffsetDateTime = OffsetDateTime.now()
        return now.toEpochSecond()
    }

    fun getMidnight(): Long{
        val now: OffsetDateTime = OffsetDateTime.now()
        val startOfDay: OffsetDateTime = now.withHour(0).withMinute(0).withSecond(0)
        return startOfDay.toEpochSecond()
    }

    fun unixMidnight(date: LocalDate): Long{
        val zoneId: ZoneId = ZoneId.systemDefault()
        return date.atStartOfDay(zoneId).toEpochSecond()
    }

    fun getMidnightfromUnix(unix: Long): LocalDateTime{
        val zoneId: ZoneId = ZoneId.systemDefault()
        val date: LocalDate = Instant.ofEpochSecond(unix).atZone(zoneId).toLocalDate()
        return date.atStartOfDay()
    }

    @SuppressLint("ResourceAsColor")
    fun textAsBitmap(text: String?, textSize: Float, textColor: Int): Bitmap? {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.textSize = textSize
        paint.color = R.color.white
        paint.textAlign = Paint.Align.RIGHT
        val baseline: Float = -paint.ascent() // ascent() is negative
        val width = (paint.measureText(text) + 0.0f).toInt() // round
        val height = (baseline + paint.descent() + 0.0f).toInt()
        val image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(image)
        canvas.drawText(text!!, 0F, baseline, paint)
        return image
    }
}

fun Double.round(decimals: Int): Double {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    return kotlin.math.round(this * multiplier) / multiplier
}
