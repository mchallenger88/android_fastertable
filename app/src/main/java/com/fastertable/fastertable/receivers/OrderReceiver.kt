package com.fastertable.fastertable.receivers

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.text.format.DateUtils
import androidx.core.app.AlarmManagerCompat
import androidx.core.content.ContextCompat

class OrderReceiver: BroadcastReceiver() {
    private val REQUEST_CODE = 0

    override fun onReceive(context: Context, intent: Intent) {
        val triggerTime = SystemClock.elapsedRealtime() + DateUtils.MINUTE_IN_MILLIS

        val notifyIntent = Intent(context, FasterReceiver::class.java)
        val notifyPendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            notifyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val orderManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        AlarmManagerCompat.setExactAndAllowWhileIdle(
            orderManager,
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            triggerTime,
            notifyPendingIntent
        )

        val notificationManager = ContextCompat.getSystemService(
            context,
            NotificationManager::class.java
        ) as NotificationManager
        notificationManager.cancelAll()
    }

}