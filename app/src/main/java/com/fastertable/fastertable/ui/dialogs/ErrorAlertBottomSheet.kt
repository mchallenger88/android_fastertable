package com.fastertable.fastertable.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.fastertable.fastertable.R
import com.fastertable.fastertable.databinding.BottomSheetErrorDialogBinding
import com.fastertable.fastertable.ui.error.ErrorViewModel

class ErrorAlertBottomSheet: BaseDialog(R.layout.bottom_sheet_error_dialog) {
    private val viewModel: ErrorViewModel by activityViewModels()
    private val binding: BottomSheetErrorDialogBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel

        binding.btnErrorClose.setOnClickListener{
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        val width = 800
        dialog!!.window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    companion object {
        fun newInstance(): ErrorAlertBottomSheet {
            return ErrorAlertBottomSheet()
        }

        const val TAG = "ErrorAlertBottomSheet"
    }
}