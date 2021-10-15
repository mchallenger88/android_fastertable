package com.fastertable.fastertable.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.fastertable.fastertable.databinding.BottomSheetErrorDialogBinding
import com.fastertable.fastertable.ui.error.ErrorViewModel

class ErrorAlertBottomSheet: BaseDialog() {
    private val viewModel: ErrorViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = BottomSheetErrorDialogBinding.inflate(inflater)
        binding.viewModel = viewModel

        binding.btnErrorClose.setOnClickListener{
            dismiss()
        }
        return binding.root
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