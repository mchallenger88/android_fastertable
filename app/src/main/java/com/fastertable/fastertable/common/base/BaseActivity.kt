package com.fastertable.fastertable.common.base

import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity(){
//    var dialogReturn: String = ""
//    override fun getReturnValue(value: String) {
//        dialogReturn = value
//    }

}

interface DismissListener{
    fun getReturnValue(value: String)
}

