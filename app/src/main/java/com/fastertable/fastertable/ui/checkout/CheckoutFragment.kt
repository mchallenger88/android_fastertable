package com.fastertable.fastertable.ui.checkout

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ConcatAdapter
import by.kirich1409.viewbindingdelegate.viewBinding
import com.fastertable.fastertable.R
import com.fastertable.fastertable.adapters.*
import com.fastertable.fastertable.common.base.BaseFragment
import com.fastertable.fastertable.databinding.CheckoutFragmentBinding

import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CheckoutFragment : BaseFragment(R.layout.checkout_fragment) {
    private val viewModel: CheckoutViewModel by activityViewModels()
    private val binding: CheckoutFragmentBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        viewModel.getEmployeeCheckout()
        bindingObservables(binding)
        viewModel.activated()
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

        viewModel.checkout.observe(viewLifecycleOwner, {
            if (it != null){
                viewModel.separateTickets(it)
                orderAdapter.submitList(it.payTickets)
                paymentAdapter.submitList(it.payments)
            }
        })
    }

}