package com.fastertable.fastertable.ui.dialogs

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.fastertable.fastertable.R
import com.fastertable.fastertable.databinding.DialogAddGuestsAdhocSplitBinding
import com.fastertable.fastertable.ui.payment.PaymentViewModel
import com.fastertable.fastertable.ui.payment.SplitPaymentViewModel

class AddGuestAdhocSplitDialogFragment : BaseDialog(R.layout.dialog_add_guests_adhoc_split) {
    private val binding: DialogAddGuestsAdhocSplitBinding by viewBinding()
    private val paymentViewModel: PaymentViewModel by activityViewModels()
    private val viewModel: SplitPaymentViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        paymentViewModel.activePayment.value?.let{
            viewModel.setActivePayment(it.clone())
        }

        binding.btnAdhocSaveItem.setOnClickListener {
            viewModel.ticketCount.value?.let {
                viewModel.setAdhocGuestCount(viewModel.ticketCount.value!!)
                dismiss()
            }
        }

        binding.btnAdhocCancelItem.setOnClickListener {
            dismiss()
        }

    }

    override fun onStart() {
        super.onStart()
        val width = 800
        dialog!!.window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    companion object {
        fun newInstance(): AddGuestAdhocSplitDialogFragment {
            return AddGuestAdhocSplitDialogFragment()
        }

        const val TAG = "AddGuestAdhocSplitDialogFragment"
    }
}