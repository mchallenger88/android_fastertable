package com.fastertable.fastertable2022.ui.dialogs

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.fastertable.fastertable2022.R
import com.fastertable.fastertable2022.databinding.BottomSheetCheckoutBinding
import com.fastertable.fastertable2022.ui.checkout.CheckoutViewModel

class CheckoutDialogBottomSheet: BaseDialog(R.layout.bottom_sheet_checkout) {
    private val viewModel: CheckoutViewModel by activityViewModels()
    private val binding: BottomSheetCheckoutBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.txtDialogCheckoutMessage.text = viewModel.checkoutComplete.value

        binding.btnDialogCheckoutClose.setOnClickListener{
            dismiss()
        }
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