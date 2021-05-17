package com.fastertable.fastertable.ui.payment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.fastertable.fastertable.adapters.TicketItemAdapter
import com.fastertable.fastertable.adapters.TicketSideBarAdapter
import com.fastertable.fastertable.common.base.BaseFragment
import com.fastertable.fastertable.databinding.PaymentFragmentBinding
import com.fastertable.fastertable.ui.order.OrderViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PaymentFragment: BaseFragment() {
    private val viewModel: PaymentViewModel by activityViewModels()
    private val orderViewModel: OrderViewModel by activityViewModels()
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        val binding = PaymentFragmentBinding.inflate(inflater)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        binding.orderViewModel = orderViewModel
        createAdapters(binding)
        createObservers(binding)
        return binding.root
    }

    private fun createObservers(binding: PaymentFragmentBinding){
        viewModel.paymentScreen.observe(viewLifecycleOwner, {it ->
            when (it){
                ShowPayment.NONE -> {
                    binding.cashLayout.visibility = View.GONE
                }
                ShowPayment.CASH -> {
                    binding.cashLayout.visibility = View.VISIBLE
                }
            }
        })


    }

    private fun createAdapters(binding: PaymentFragmentBinding){
        val ticketNummberAdapter = TicketSideBarAdapter(TicketSideBarAdapter.TicketSideBarListener { it ->
            viewModel.setActiveTicket(it)
        })

        val ticketsAdapter = TicketItemAdapter(TicketItemAdapter.TicketItemListener { it ->

        })

        binding.ticketRecycler.adapter = ticketNummberAdapter

        binding.ticketItemsRecycler.adapter = ticketsAdapter

        viewModel.livePayment.observe(viewLifecycleOwner, { item ->
            item?.tickets?.forEach { ticket ->
                if (ticket.uiActive){
                    ticketsAdapter.submitList(ticket.ticketItems)
                    ticketsAdapter.notifyDataSetChanged()

                }
            }

        })
    }
}