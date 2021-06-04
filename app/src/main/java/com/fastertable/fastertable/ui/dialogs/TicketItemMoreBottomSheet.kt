package com.fastertable.fastertable.ui.dialogs

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fastertable.fastertable.databinding.BottomSheetTicketItemMoreBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class TicketItemMoreBottomSheet: BottomSheetDialogFragment() {
    private lateinit var dialogListener: DialogListener
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = BottomSheetTicketItemMoreBinding.inflate(inflater)

        binding.btnTicketVoidItem.setOnClickListener {
            dialogListener.returnValue("Void Item")
            dismiss()
        }

        binding.btnTicketDiscountItem.setOnClickListener {
            dialogListener.returnValue("Discount Item")
            dismiss()
        }

        binding.btnTicketModifyItemPrice.setOnClickListener {
            dialogListener.returnValue("Modify Price")
            dismiss()
        }

        binding.btnTicketItemClose.setOnClickListener {
            dismiss()
        }
        return binding.root
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
        val width = 800
        val height = 500
        dialog!!.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    companion object {
        fun newInstance(): TicketMoreBottomSheet {
            return TicketMoreBottomSheet()
        }

        const val TAG = "TicketMoreBottomSheet"
    }
}