package com.fastertable.fastertable.ui.dialogs

import androidx.fragment.app.DialogFragment

open class BaseDialog: DialogFragment() {

}

interface DialogListener{
    fun returnValue(value: String)
}


interface ItemNoteListener{
    fun returnNote(value: String)
}