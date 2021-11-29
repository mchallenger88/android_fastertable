package com.fastertable.fastertable.ui.dialogs

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.fastertable.fastertable.R
import com.fastertable.fastertable.databinding.BottomSheetOrderMoreBinding
import com.fastertable.fastertable.ui.order.OrderViewModel

class OrderMoreDialog: BaseDialog(R.layout.bottom_sheet_order_more) {
    private lateinit var dialogListener: DialogListener
    private val viewModel: OrderViewModel by activityViewModels()
    private val binding: BottomSheetOrderMoreBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel

        binding.btnTransferOrder.setOnClickListener {
            dialogListener.returnValue("Transfer Order")
            dismiss()
        }

        binding.btnTransferOrderItem.setOnClickListener {
            dialogListener.returnValue("Transfer Order Item")
            dismiss()
        }

        binding.btnAdhocMenuItem.setOnClickListener {
            dialogListener.returnValue("Misc Menu Item")
            dismiss()
        }

        binding.btnResendToKitchen.setOnClickListener {
            viewModel.resendKitchenTicket()
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        val width = 800
        val height= 500
        dialog!!.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is DialogListener) {
            dialogListener = context
        } else {
            throw RuntimeException(requireContext().toString() + " must implement OnFragmentInteractionListener")
        }
    }

    companion object {
        fun newInstance(): OrderMoreDialog {
            return OrderMoreDialog()
        }

        const val TAG = "OrderMoreDialog"
    }
}