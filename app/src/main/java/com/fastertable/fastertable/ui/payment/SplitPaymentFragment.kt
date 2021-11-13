package com.fastertable.fastertable.ui.payment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.fastertable.fastertable.R
import com.fastertable.fastertable.adapters.TicketItemAdapter
import com.fastertable.fastertable.adapters.TicketSideBarAdapter
import com.fastertable.fastertable.common.base.BaseFragment
import com.fastertable.fastertable.databinding.PaymentSplitFragmentBinding
import com.fastertable.fastertable.ui.order.OrderViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplitPaymentFragment: BaseFragment(R.layout.payment_split_fragment) {
    private val viewModel: PaymentViewModel by activityViewModels()
    private val orderViewModel: OrderViewModel by activityViewModels()
    private val binding: PaymentSplitFragmentBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        binding.orderViewModel = orderViewModel
        createAdapters(binding)
    }

    private fun createAdapters(binding: PaymentSplitFragmentBinding){
        val ticketNumberAdapter = TicketSideBarAdapter(TicketSideBarAdapter.TicketSideBarListener {
            viewModel.setActiveTicket(it)
        })

        val ticketsAdapter = TicketItemAdapter(TicketItemAdapter.TicketItemListener {

        })

        binding.managePaymentRecycler.adapter = ticketNumberAdapter

        binding.manageTicketItemsRecycler.adapter = ticketsAdapter

        viewModel.activePayment.observe(viewLifecycleOwner, { item ->
            item?.tickets?.forEach { ticket ->
                if (ticket.uiActive){
//                    ticketsAdapter.submitList(ticket.ticketItems)
                    ticketsAdapter.notifyDataSetChanged()

                }
            }

        })
    }

}