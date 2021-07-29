package com.fastertable.fastertable.ui.dialogs

import androidx.fragment.app.DialogFragment
import com.fastertable.fastertable.data.models.DateDialog

open class BaseDialog: DialogFragment() {

}

interface DialogListener{
    fun returnValue(value: String)
}

interface DateListener {
    fun returnDate(value: DateDialog)
}


interface ItemNoteListener{
    fun returnNote(value: String)
}