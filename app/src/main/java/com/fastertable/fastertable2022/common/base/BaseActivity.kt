package com.fastertable.fastertable2022.common.base

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.fastertable.fastertable2022.LoginActivity

open class BaseActivity() : AppCompatActivity(), LogoutListener{
    private var timeoutHandler: Handler? = null
    private var interactionTimeoutRunnable: Runnable? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        timeoutHandler =  Handler();
        interactionTimeoutRunnable =  Runnable {
            onSessionLogout()
        }
        //start countdown
        startHandler()
    }

    //TODO: remove this api call
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onUserInteraction() {
        super.onUserInteraction()
        resetHandler()
    }

    //restart countdown
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun resetHandler() {
        interactionTimeoutRunnable?.let { timeoutHandler?.removeCallbacks(it) }
        interactionTimeoutRunnable?.let { timeoutHandler?.postDelayed(it, 300*1000) }; //for 10 second

    }

    // start countdown
    private fun startHandler() {
        interactionTimeoutRunnable?.let { timeoutHandler?.removeCallbacks(it) }
        interactionTimeoutRunnable?.let { timeoutHandler?.postDelayed(it, 300*1000) }; //for 10 second
    }

    override fun onSessionLogout() {
        super.onSessionLogout()
        interactionTimeoutRunnable?.let { timeoutHandler?.removeCallbacks(it) }
        timeoutHandler = null
        interactionTimeoutRunnable = null
        finish()
        val intent = Intent(this, LoginActivity::class.java)
        intent.putExtra("fragmentToLoad", "User")
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }
}

interface DismissListener{
    fun getReturnValue(value: String)
}

interface LogoutListener {
    fun onSessionLogout(){}
}
