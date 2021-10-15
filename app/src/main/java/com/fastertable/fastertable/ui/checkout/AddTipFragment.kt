package com.fastertable.fastertable.ui.checkout

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.fastertable.fastertable.adapters.TicketItemAdapter
import com.fastertable.fastertable.adapters.TicketSideBarAdapter
import com.fastertable.fastertable.common.base.BaseFragment
import com.fastertable.fastertable.databinding.CheckoutAddTipFragmentBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddTipFragment: BaseFragment() {
    private val viewModel: CheckoutViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = CheckoutAddTipFragmentBinding.inflate(inflater)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        binding.btnAddTip.setOnClickListener {
            viewModel.addTip(binding.editAddTip.editText?.text.toString())
        }
        bindingObservables(binding)
        return binding.root
    }

    private fun bindingObservables(binding: CheckoutAddTipFragmentBinding){
        val ticketNumberAdapter = TicketSideBarAdapter(TicketSideBarAdapter.TicketSideBarListener {
            viewModel.setActiveTicket(it)
        })

        val ticketsAdapter = TicketItemAdapter(TicketItemAdapter.TicketItemListener {

        })

        binding.tipTicketRecycler.adapter = ticketNumberAdapter
        binding.ticketItemsRecycler.adapter = ticketsAdapter
    }
}