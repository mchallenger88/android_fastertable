package com.fastertable.fastertable2022.ui.payment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.fastertable.fastertable2022.R
import com.fastertable.fastertable2022.adapters.TicketItemAdapter
import com.fastertable.fastertable2022.adapters.TicketSideBarAdapter
import com.fastertable.fastertable2022.common.base.BaseFragment
import com.fastertable.fastertable2022.databinding.VoidFragmentStartBinding
import com.fastertable.fastertable2022.ui.order.OrderViewModel

class VoidStartFragment: BaseFragment(R.layout.void_fragment_start) {
    private val viewModel: PaymentViewModel by activityViewModels()
    private val orderViewModel: OrderViewModel by activityViewModels()
    private val binding: VoidFragmentStartBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        binding.orderViewModel = orderViewModel
        viewModel.setVoidSpinner(true)

        val ticketNumberAdapter = TicketSideBarAdapter(TicketSideBarAdapter.TicketSideBarListener {
            viewModel.setActiveTicket(it)
        })

        val ticketsAdapter = TicketItemAdapter(TicketItemAdapter.TicketItemListener {
            viewModel.toggleTicketItemMore(it)
        })

        binding.voidTicketRecycler.adapter = ticketNumberAdapter

        binding.ticketItemsRecycler1.adapter = ticketsAdapter

        viewModel.activePayment.observe(viewLifecycleOwner, { item ->
            if (item != null){
                viewModel.setVoidSpinner(false)
            }
            item?.tickets?.forEach { ticket ->
                if (ticket.uiActive){
                    ticketsAdapter.submitList(ticket.ticketItems)
                    ticketsAdapter.notifyDataSetChanged()

                }
            }
        })
    }

}