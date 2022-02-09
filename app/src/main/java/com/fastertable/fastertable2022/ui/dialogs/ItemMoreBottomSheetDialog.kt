package com.fastertable.fastertable2022.ui.dialogs

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.fastertable.fastertable2022.R
import com.fastertable.fastertable2022.databinding.BottomSheetOrderLineItemBinding
import com.fastertable.fastertable2022.ui.order.OrderViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ItemMoreBottomSheetDialog: BaseDialog(R.layout.bottom_sheet_order_line_item) {
    private lateinit var dialogListener: DialogListener
    private val viewModel: OrderViewModel by activityViewModels()
    private val binding: BottomSheetOrderLineItemBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnItemDelete.setOnClickListener {
            viewModel.removeOrderItem()
            dismiss()
        }
        binding.btnItemRush.setOnClickListener {
            viewModel.toggleRush()
            dismiss()
        }
        binding.btnItemClose.setOnClickListener {
            dismiss()
        }
        binding.btnItemNoMake.setOnClickListener {
            viewModel.toggleNoMake()
            dismiss()
        }
        binding.btnItemTakeout.setOnClickListener {
            viewModel.toggleTakeout()
            dismiss()
        }
        binding.btnModifyItem.setOnClickListener {
            viewModel.modifyOrderItem()
            dismiss()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is DialogListener) {
            dialogListener = context
        } else {
            throw RuntimeException(requireContext().toString() + " must implement OnFragmentInteractionListener")
        }
    }


    override fun onStart() {
        super.onStart()
        val width = 700
        dialog!!.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    companion object {
        fun newInstance(): OrderNotesDialogFragment {
            return OrderNotesDialogFragment()
        }

        const val TAG = "ItemMoreBottomSheetDialog"
    }
}