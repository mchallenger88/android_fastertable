package com.fastertable.fastertable.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.fastertable.fastertable.databinding.BottomSheetCheckoutBinding
import com.fastertable.fastertable.databinding.BottomSheetErrorDialogBinding
import com.fastertable.fastertable.ui.checkout.CheckoutViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CheckoutDialogBottomSheet(): BaseDialog() {
    private val viewModel: CheckoutViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = BottomSheetCheckoutBinding.inflate(inflater)
        binding.viewModel = viewModel

        binding.txtDialogCheckoutMessage.text = viewModel.checkoutComplete.value

        binding.btnDialogCheckoutClose.setOnClickListener{
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
        fun newInstance(): CheckoutDialogBottomSheet {
            return CheckoutDialogBottomSheet()
        }

        const val TAG = "CheckoutDialogBottomSheet"
    }
}