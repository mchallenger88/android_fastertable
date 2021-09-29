package com.fastertable.fastertable.ui.dialogs

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.fastertable.fastertable.databinding.BottomSheetOrderMoreBinding
import com.fastertable.fastertable.ui.order.OrderViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class OrderMoreDialog: BaseDialog() {
    private lateinit var dialogListener: DialogListener
    private val viewModel: OrderViewModel by activityViewModels()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = BottomSheetOrderMoreBinding.inflate(inflater)
        binding.viewModel = viewModel

        binding.btnTransferOrder.setOnClickListener {
            dialogListener.returnValue("Transfer Order")
            dismiss()
        }

        binding.btnAdhocMenuItem.setOnClickListener {
            dialogListener.returnValue("Misc Menu Item")
            dismiss()
        }

        return binding.root
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