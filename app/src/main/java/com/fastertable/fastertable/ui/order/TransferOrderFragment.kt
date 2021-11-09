package com.fastertable.fastertable.ui.order

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ConcatAdapter
import com.fastertable.fastertable.adapters.*
import com.fastertable.fastertable.common.base.BaseFragment
import com.fastertable.fastertable.databinding.TransferOrderFragmentBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TransferOrderFragment : BaseFragment() {
    private val viewModel: TransferOrderViewModel by activityViewModels()
    private lateinit var binding: TransferOrderFragmentBinding
    private val args: TransferOrderFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = TransferOrderFragmentBinding.inflate(inflater)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        viewModel.refreshOrders()
        viewModel.setInitialOrderId(args.orderId)
        transferObservables()
        return binding.root
    }

    private fun transferObservables(){
        val orderAdapter = TransferOrderAdapter(TransferOrderListListener {
            viewModel.orderClicked(it)
        })

        val headerAdapter = TransferOrderListHeaderAdapter()
        val concatAdapter = ConcatAdapter(headerAdapter, orderAdapter)

        val employeeAdapter = TransferEmployeeAdapter(TransferEmployeeListListener {
            viewModel.employeeClicked(it)
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

    override fun onDestroyView() {
        super.onDestroyView()
        binding.unbind()
    }
}