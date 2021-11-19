package com.fastertable.fastertable.ui.dialogs

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import by.kirich1409.viewbindingdelegate.viewBinding
import com.fastertable.fastertable.R
import com.fastertable.fastertable.databinding.BottomSheetOrderLineItemBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ItemMoreBottomSheetDialog: BaseDialog(R.layout.bottom_sheet_order_line_item) {
    private lateinit var dialogListener: DialogListener
    private val binding: BottomSheetOrderLineItemBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnItemDelete.setOnClickListener {
            dialogListener.returnValue("Delete")
            dismiss()
        }
        binding.btnItemRush.setOnClickListener {
            dialogListener.returnValue("Toggle Rush")
            dismiss()
        }
        binding.btnItemClose.setOnClickListener {
            dismiss()
        }
        binding.btnItemNoMake.setOnClickListener {
            dialogListener.returnValue("Toggle No Make")
            dismiss()
        }
        binding.btnItemTakeout.setOnClickListener {
            dialogListener.returnValue("Toggle Takeout")
            dismiss()
        }
        binding.btnModifyItem.setOnClickListener {
            dialogListener.returnValue("Modify Order Item")
            dismiss()
        }
    }
//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
//        val binding = BottomSheetOrderLineItemBinding.inflate(inflater)
//
//
//        return binding.root
//    }

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