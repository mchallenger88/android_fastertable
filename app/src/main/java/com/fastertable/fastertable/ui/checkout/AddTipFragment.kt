package com.fastertable.fastertable.ui.checkout

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.activityViewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.fastertable.fastertable.R
import com.fastertable.fastertable.adapters.TicketItemAdapter
import com.fastertable.fastertable.adapters.TicketPaymentAdapter
import com.fastertable.fastertable.adapters.TicketSideBarAdapter
import com.fastertable.fastertable.common.base.BaseFragment
import com.fastertable.fastertable.data.models.TicketPayment
import com.fastertable.fastertable.databinding.CheckoutAddTipFragmentBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddTipFragment: BaseFragment(R.layout.checkout_add_tip_fragment) {
    private val viewModel: CheckoutViewModel by activityViewModels()
    private val binding: CheckoutAddTipFragmentBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        bindingObservables(binding)
    }


    private fun bindingObservables(binding: CheckoutAddTipFragmentBinding){
        val ticketNumberAdapter = TicketSideBarAdapter(TicketSideBarAdapter.TicketSideBarListener {
            viewModel.setActiveTicket(it)
        })

        val ticketsAdapter = TicketItemAdapter(TicketItemAdapter.TicketItemListener {

        })

        val paymentAdapter = TicketPaymentAdapter(TicketPaymentAdapter.AddTipListener {
            hideKeyboardFrom(requireContext(), requireView())
            viewModel.addTipNew(it)
        })

        binding.recyclerTicketPayment.adapter = paymentAdapter

        binding.tipTicketRecycler.adapter = ticketNumberAdapter
        binding.ticketItemsRecycler.adapter = ticketsAdapter

        viewModel.activePayment.observe(viewLifecycleOwner, { payment ->
            if (payment != null){
                val list = mutableListOf<TicketPayment>()
                payment.tickets?.forEach { ticket ->
                    ticket.paymentList?.forEach {
                        if (!it.canceled){
                            list.add(it)
                        }
                    }
                }

                paymentAdapter.submitList(list)
                paymentAdapter.notifyDataSetChanged()
            }
        })
    }

    private fun hideKeyboardFrom(context: Context, view: View) {
        val imm: InputMethodManager =
            context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

}