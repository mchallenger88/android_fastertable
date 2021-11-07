package com.fastertable.fastertable.ui.payment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.fastertable.fastertable.adapters.VoidPaymentAdapter
import com.fastertable.fastertable.common.base.BaseFragment
import com.fastertable.fastertable.data.models.TicketPayment
import com.fastertable.fastertable.databinding.VoidPaymentFragmentBinding
import com.fastertable.fastertable.ui.order.OrderViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VoidFragment: BaseFragment() {
    private val viewModel: PaymentViewModel by activityViewModels()
    private val orderViewModel: OrderViewModel by activityViewModels()
    private lateinit var binding: VoidPaymentFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = VoidPaymentFragmentBinding.inflate(inflater)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        binding.orderViewModel = orderViewModel
        bindAdapters()
        return binding.root
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

    override fun onDestroyView() {
        super.onDestroyView()
        binding.unbind()
    }
}