package com.fastertable.fastertable.ui.payment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.fastertable.fastertable.R
import com.fastertable.fastertable.adapters.TicketItemMoveAdapter
import com.fastertable.fastertable.adapters.TicketSideBarAdapter
import com.fastertable.fastertable.common.base.BaseFragment
import com.fastertable.fastertable.databinding.PaymentAddHocSplitBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AdhocPaymentFragment: BaseFragment(R.layout.payment_add_hoc_split) {
    private val viewModel: SplitPaymentViewModel by activityViewModels()
    private val paymentViewModel: PaymentViewModel by activityViewModels()
    private val binding: PaymentAddHocSplitBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        binding.paymentViewModel = paymentViewModel

        createObservers()
    }

    private fun createObservers(){
        val ticketSideBarAdapter = TicketSideBarAdapter(TicketSideBarAdapter.TicketSideBarListener {
            viewModel.moveItemsToTicket(it)
        })

        val ticketsAdapter = TicketItemMoveAdapter(TicketItemMoveAdapter.TicketItemMoveListener {
            viewModel.selectedItem(it)
        })

        binding.adhocPaymentRecycler.adapter = ticketSideBarAdapter

        binding.adhocTicketRecycler.adapter = ticketsAdapter

        viewModel.activePayment.observe(viewLifecycleOwner, { item ->
            item?.let {
                ticketSideBarAdapter.submitList(it.tickets)
                ticketSideBarAdapter.notifyDataSetChanged()
            }
            item?.tickets?.forEach { ticket ->
                if (ticket.uiActive){
                    ticketsAdapter.submitList(ticket.ticketItems)
                    ticketsAdapter.notifyDataSetChanged()

                }
            }
        })

        viewModel.saveMove.observe(viewLifecycleOwner, {
            if (it){
                viewModel.activePayment.value?.let { payment ->
                    paymentViewModel.setLivePayment(payment)
                    viewModel.setSaveMove(false)
                    viewModel.setNavigatePayment(true)
                }

            }
        })

    }
}