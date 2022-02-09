package com.fastertable.fastertable2022.ui.order

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ConcatAdapter
import by.kirich1409.viewbindingdelegate.viewBinding
import com.fastertable.fastertable2022.R
import com.fastertable.fastertable2022.adapters.*
import com.fastertable.fastertable2022.common.base.BaseFragment
import com.fastertable.fastertable2022.databinding.TransferOrderFragmentBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TransferOrderFragment : BaseFragment(R.layout.transfer_order_fragment) {
    private val viewModel: TransferOrderViewModel by activityViewModels()
    private val binding: TransferOrderFragmentBinding by viewBinding()
    private val args: TransferOrderFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        viewModel.refreshOrders()
        viewModel.setInitialOrderId(args.orderId)
        transferObservables()
    }

    private fun transferObservables(){
        val orderAdapter = TransferOrderAdapter(TransferOrderListListener {
            viewModel.orderClicked(it)
        })

        val headerAdapter = TransferOrderListHeaderAdapter()
        val concatAdapter = ConcatAdapter(headerAdapter, orderAdapter)

        val employeeAdapter = TransferEmployeeAdapter(TransferEmployeeListListener {
            viewModel.initiateTransfer(it)
        })

        binding.transferOrderRecycler.adapter = concatAdapter
        binding.transferEmployeeRecycler.adapter = employeeAdapter

        viewModel.activeOrders.observe(viewLifecycleOwner, {
            orderAdapter.submitList(it)
            orderAdapter.notifyDataSetChanged()
        })

        viewModel.activeEmployees.observe(viewLifecycleOwner, {
            employeeAdapter.submitList(it)
            employeeAdapter.notifyDataSetChanged()
        })
    }

}