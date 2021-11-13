package com.fastertable.fastertable.ui.payment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.fastertable.fastertable.R
import com.fastertable.fastertable.adapters.VoidPaymentAdapter
import com.fastertable.fastertable.common.base.BaseFragment
import com.fastertable.fastertable.data.models.TicketPayment
import com.fastertable.fastertable.databinding.VoidPaymentFragmentBinding
import com.fastertable.fastertable.ui.order.OrderViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VoidFragment: BaseFragment(R.layout.void_payment_fragment) {
    private val viewModel: PaymentViewModel by activityViewModels()
    private val orderViewModel: OrderViewModel by activityViewModels()
    private val binding: VoidPaymentFragmentBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        binding.orderViewModel = orderViewModel
        bindAdapters()
    }

    private fun bindAdapters(){
        val adapter = VoidPaymentAdapter(VoidPaymentAdapter.VoidPaymentListener {
            viewModel.voidPayment(it)
        })

        binding.recyclerVoidPayment.adapter = adapter

        viewModel.activePayment.observe(viewLifecycleOwner, { payment ->
            if (payment != null){
                val list = mutableListOf<TicketPayment>()
                for (ticket in payment.tickets!!){
                    for (ticketPayment in ticket.paymentList!!){
                        list.add(ticketPayment)
                    }
                }
                adapter.submitList(list)
                adapter.notifyDataSetChanged()
            }
        })
    }

}