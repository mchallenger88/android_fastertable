package com.fastertable.fastertable.ui.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import com.fastertable.fastertable.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class KitchenWarningDialogFragment : BaseDialog() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(activity).let {
            it.setTitle(R.string.kitchen_warning_title)
            it.setMessage(R.string.kitchen_warning_message)
            it.setPositiveButton(R.string.dialog_close) { _, _ -> dismiss() }
            it.create()
        }
    }

    companion object {
        const val TAG = "KitchenWarningDialogFragment"

    }
}