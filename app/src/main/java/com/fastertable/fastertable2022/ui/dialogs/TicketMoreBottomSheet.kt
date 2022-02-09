package com.fastertable.fastertable2022.ui.dialogs

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.fastertable.fastertable2022.databinding.BottomSheetTicketMoreBinding
import com.fastertable.fastertable2022.ui.payment.PaymentViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class TicketMoreBottomSheet: BottomSheetDialogFragment() {
    private lateinit var dialogListener: DialogListener
    private val viewModel: PaymentViewModel by activityViewModels()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = BottomSheetTicketMoreBinding.inflate(inflater)

        binding.btnTicketVoid.setOnClickListener {
            viewModel.initialVoidTicket()
            dismiss()
        }

        binding.btnTicketDiscount.setOnClickListener {
            dialogListener.returnValue("Discount")
            dismiss()
        }

        binding.btnItemClose.setOnClickListener {
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
        dialog?.let {
            it.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }

    companion object {
        fun newInstance(): TicketMoreBottomSheet {
            return TicketMoreBottomSheet()
        }

        const val TAG = "TicketMoreBottomSheet"
    }
}