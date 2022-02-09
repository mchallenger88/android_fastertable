package com.fastertable.fastertable2022.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.fastertable.fastertable2022.databinding.DialogErrorBinding
import com.fastertable.fastertable2022.ui.payment.PaymentViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AlertDialogBottomSheet: BottomSheetDialogFragment() {
    private val viewModel: PaymentViewModel by activityViewModels()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = DialogErrorBinding.inflate(inflater)
        binding.viewModel = viewModel
        binding.btnAltertClose.setOnClickListener{
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
        fun newInstance(): AlertDialogBottomSheet {
            return AlertDialogBottomSheet()
        }

        const val TAG = "AlertDialogBottomSheet"
    }

}