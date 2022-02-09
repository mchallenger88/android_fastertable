package com.fastertable.fastertable2022.ui.dialogs

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.fastertable.fastertable2022.R
import com.fastertable.fastertable2022.data.models.OpsAuth
import com.fastertable.fastertable2022.data.models.Permissions
import com.fastertable.fastertable2022.databinding.BottomSheetOrderMoreBinding
import com.fastertable.fastertable2022.ui.order.OrderViewModel
import com.fastertable.fastertable2022.ui.payment.PaymentViewModel

class OrderMoreDialog: BaseDialog(R.layout.bottom_sheet_order_more) {
    private lateinit var dialogListener: DialogListener
    private val viewModel: OrderViewModel by activityViewModels()
    private val paymentViewModel: PaymentViewModel by activityViewModels()
    private val binding: BottomSheetOrderMoreBinding by viewBinding()
    private var user: OpsAuth? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
        user = viewModel.user
        setClosePermission()
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

        binding.btnForceClose.setOnClickListener {
            forceOrderClose()
            viewModel.setNavHome(true)
            dismiss()
        }
    }

    private fun setClosePermission(){
        val permission = Permissions.viewOrders.toString()
        user?.claims?.let { claims ->
            val manager = claims.find { it.permission.name == permission && it.permission.value }
            if (claims.contains(manager)){
                binding.btnForceClose.visibility = View.VISIBLE
            }else{
                binding.btnForceClose.visibility = View.GONE
            }
        }
    }

    private fun forceOrderClose(){
        val order = viewModel.activeOrder.value
        val payment = paymentViewModel.activePayment.value
        order?.let {
            it.forceClose()
            viewModel.updateOrder(it)
        }

        payment?.let {
            it.close()
            viewModel.updatePayment(it)
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