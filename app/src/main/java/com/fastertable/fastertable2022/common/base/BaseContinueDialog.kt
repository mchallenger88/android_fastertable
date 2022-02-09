package com.fastertable.fastertable2022.common.base

import androidx.fragment.app.DialogFragment

open class BaseContinueDialog : DialogFragment() {

    interface ContinueListener{
        fun returnContinue(value: String)
    }

}