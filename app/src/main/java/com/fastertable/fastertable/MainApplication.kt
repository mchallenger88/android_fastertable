package com.fastertable.fastertable

import android.app.Application
import android.util.Log
import com.fastertable.fastertable.common.base.LogoutListener
import dagger.hilt.android.HiltAndroidApp
import java.util.*
import kotlin.concurrent.timerTask
import kotlin.math.log

@HiltAndroidApp
class MainApplication() : Application(){
    private var timer: Timer = Timer()

    fun startUserSession(logoutListener: LogoutListener) {
        cancelTimer()
        timer = Timer()
        timer.schedule( timerTask {
            logoutListener.onSessionLogout()
        }, 10000 )
    }

    private fun cancelTimer(){
        timer.cancel()
    }

    fun onUserInteracted(logoutListener: LogoutListener) {
        startUserSession(logoutListener)
    }


}