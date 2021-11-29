package com.fastertable.fastertable.common.base

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.fastertable.fastertable.LoginActivity
import com.fastertable.fastertable.MainApplication

open class BaseActivity() : AppCompatActivity(), LogoutListener{

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MainApplication().startUserSession(this)
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        MainApplication().onUserInteracted(this)
    }

    override fun onSessionLogout() {
        super.onSessionLogout()
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
