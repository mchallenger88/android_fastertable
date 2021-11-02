package com.fastertable.fastertable.ui.checkout

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.activityViewModels
import com.fastertable.fastertable.adapters.TicketItemAdapter
import com.fastertable.fastertable.adapters.TicketPaymentAdapter
import com.fastertable.fastertable.adapters.TicketSideBarAdapter
import com.fastertable.fastertable.common.base.BaseFragment
import com.fastertable.fastertable.data.models.TicketPayment
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
//        binding.btnAddTip.setOnClickListener {
//            viewModel.addTip(binding.editAddTip.editText?.text.toString())
//        }
        bindingObservables(binding)
        return binding.root
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
                for (ticket in payment.tickets!!){
                    for (ticketPayment in ticket.paymentList!!){
                        list.add(ticketPayment)
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