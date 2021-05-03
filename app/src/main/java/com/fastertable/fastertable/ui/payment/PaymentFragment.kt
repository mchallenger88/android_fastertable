package com.fastertable.fastertable.ui.payment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.fastertable.fastertable.adapters.TicketSideBarAdapter
import com.fastertable.fastertable.common.base.BaseFragment
import com.fastertable.fastertable.databinding.PaymentFragmentBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PaymentFragment: BaseFragment() {
    private val viewModel: PaymentViewModel by activityViewModels()
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        val binding = PaymentFragmentBinding.inflate(inflater)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        createAdapters(binding)
        return binding.root
    }

    private fun createAdapters(binding: PaymentFragmentBinding){
        val ticketAdapter = TicketSideBarAdapter(TicketSideBarAdapter.TicketSideBarListener { it ->
            viewModel.setActiveTicket(it)
        })

        binding.ticketRecycler.adapter = ticketAdapter
    }
}