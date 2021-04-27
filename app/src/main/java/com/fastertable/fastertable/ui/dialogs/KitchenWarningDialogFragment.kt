package com.fastertable.fastertable.ui.dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import com.fastertable.fastertable.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class KitchenWarningDialogFragment : BaseDialog() {
    var onInputListener: OnInputListener? = null


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext()).let {
            it.setTitle(R.string.kitchen_warning_title)
            it.setMessage(R.string.kitchen_warning_message)
            it.setPositiveButton(R.string.dialog_close) { _, _ -> onClose() }
            it.create()
        }
    }

    interface OnInputListener: DialogInterface {
        fun sendInput(input: String?)
    }

    private fun onClose(){
        dismiss()
    }


    companion object {
        const val TAG = "KitchenWarningDialogFragment"
    }
}