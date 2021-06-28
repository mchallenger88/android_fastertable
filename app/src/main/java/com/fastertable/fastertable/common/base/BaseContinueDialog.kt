package com.fastertable.fastertable.common.base

import androidx.fragment.app.DialogFragment

open class BaseContinueDialog : DialogFragment() {

    interface ContinueListener{
        fun returnContinue(value: String)
    }

}