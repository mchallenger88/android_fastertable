package com.fastertable.fastertable.ui.payment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.fastertable.fastertable.adapters.TicketItemAdapter
import com.fastertable.fastertable.adapters.TicketSideBarAdapter
import com.fastertable.fastertable.common.base.BaseFragment
import com.fastertable.fastertable.databinding.VoidFragmentStartBinding
import com.fastertable.fastertable.ui.order.OrderViewModel

class VoidStartFragment: BaseFragment()  {
    private val viewModel: PaymentViewModel by activityViewModels()
    private val orderViewModel: OrderViewModel by activityViewModels()
    private lateinit var binding: VoidFragmentStartBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = VoidFragmentStartBinding.inflate(inflater)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        binding.orderViewModel = orderViewModel

        val ticketNumberAdapter = TicketSideBarAdapter(TicketSideBarAdapter.TicketSideBarListener {
            viewModel.setActiveTicket(it)
        })

        val ticketsAdapter = TicketItemAdapter(TicketItemAdapter.TicketItemListener {
            viewModel.toggleTicketItemMore(it)
        })

        binding.voidTicketRecycler.adapter = ticketNumberAdapter

        binding.ticketItemsRecycler1.adapter = ticketsAdapter

        viewModel.activePayment.observe(viewLifecycleOwner, { item ->
            item?.tickets?.forEach { ticket ->
                if (ticket.uiActive){
                    ticketsAdapter.submitList(ticket.ticketItems)
                    ticketsAdapter.notifyDataSetChanged()

                }
            }
        })

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.unbind()
    }
}