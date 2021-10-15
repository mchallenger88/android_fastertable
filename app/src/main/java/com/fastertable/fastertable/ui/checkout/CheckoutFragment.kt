package com.fastertable.fastertable.ui.checkout

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ConcatAdapter
import com.fastertable.fastertable.adapters.*
import com.fastertable.fastertable.common.base.BaseFragment
import com.fastertable.fastertable.databinding.CheckoutFragmentBinding

import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CheckoutFragment : BaseFragment() {
    private val viewModel: CheckoutViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = CheckoutFragmentBinding.inflate(inflater)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        viewModel.getEmployeeCheckout()
        bindingObservables(binding)
        viewModel.activated()
        return binding.root
    }

    private fun bindingObservables(binding: CheckoutFragmentBinding){
        val orderAdapter = CheckoutOrdersAdapter(CheckoutOrderListListener {
                orderId ->  viewModel.setPaymentThenNav(orderId)
        })

        val headerAdapter = CheckoutOrderHeaderAdapter()
        val concatAdapter = ConcatAdapter(headerAdapter, orderAdapter)
        val paymentAdapter = CheckoutPaymentsAdapter(CheckoutPaymentListListener {

        })

        binding.checkoutOrderRecycler.adapter = concatAdapter
        binding.checkoutPaymentRecycler.adapter = paymentAdapter


        viewModel.checkout.observe(viewLifecycleOwner, {
            if (it != null){
                viewModel.separateTickets(it)
                orderAdapter.submitList(it.payTickets)
                paymentAdapter.submitList(it.payments)
            }
        })
    }
}