package com.fastertable.fastertable2022.ui.checkout

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.fastertable.fastertable2022.R
import com.fastertable.fastertable2022.adapters.TicketItemAdapter
import com.fastertable.fastertable2022.adapters.TicketPaymentAdapter
import com.fastertable.fastertable2022.adapters.TicketSideBarAdapter
import com.fastertable.fastertable2022.common.base.BaseFragment
import com.fastertable.fastertable2022.data.models.TicketPayment
import com.fastertable.fastertable2022.databinding.CheckoutAddTipFragmentBinding
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
            tipAlert(it)
            viewModel.addTipNew(it)
        })

        binding.recyclerTicketPayment.adapter = paymentAdapter

        binding.tipTicketRecycler.adapter = ticketNumberAdapter
        binding.ticketItemsRecycler.adapter = ticketsAdapter

        viewModel.activePayment.observe(viewLifecycleOwner) { payment ->
            if (payment != null) {
                val list = mutableListOf<TicketPayment>()
                payment.tickets?.forEach { ticket ->
                    ticket.paymentList?.forEach {
                        if (!it.canceled) {
                            list.add(it)
                        }
                    }
                }

                paymentAdapter.submitList(list)
                paymentAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun hideKeyboardFrom(context: Context, view: View) {
        val imm: InputMethodManager =
            context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun tipAlert(tp: TicketPayment){
        val builder: AlertDialog.Builder = this.let {
            AlertDialog.Builder(activity)
        }

        var tipMessage = ""

        this.context?.let {
            tipMessage = it.getString(R.string.add_gratuity_message, "%.${2}f".format(tp.gratuity))
        }

        builder.setMessage(tipMessage)
            ?.setTitle(R.string.add_gratuity_title)

//        builder.apply {
//            setPositiveButton(
//                R.string.ok,
//                DialogInterface.OnClickListener { dialog, id ->
//
//                })
//        }

        val dialog: AlertDialog = builder.create()
        dialog.show()
        val textView = dialog.findViewById<View>(android.R.id.message) as TextView
        textView.textSize = 24f
    }

}