package com.fastertable.fastertable.ui.payment

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.constraintlayout.helper.widget.Flow
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.activityViewModels
import com.fastertable.fastertable.R
import com.fastertable.fastertable.adapters.TicketItemAdapter
import com.fastertable.fastertable.adapters.TicketSideBarAdapter
import com.fastertable.fastertable.common.base.BaseFragment
import com.fastertable.fastertable.data.models.Discount
import com.fastertable.fastertable.databinding.PaymentFragmentBinding
import com.fastertable.fastertable.ui.order.OrderViewModel
import dagger.hilt.android.AndroidEntryPoint
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.navigation.fragment.findNavController
import com.fastertable.fastertable.data.models.ManualCredit


@AndroidEntryPoint
class PaymentFragment: BaseFragment() {
    private val viewModel: PaymentViewModel by activityViewModels()
    private val orderViewModel: OrderViewModel by activityViewModels()
    private lateinit var binding: PaymentFragmentBinding
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        binding = PaymentFragmentBinding.inflate(inflater)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        binding.orderViewModel = orderViewModel
        binding.btnModifyPrice.setOnClickListener {
            modifyPrice(binding)
        }
        binding.manualExpirationDate.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                var txt = s.toString()
                if (s?.length == 4 && !txt.contains("/")){
                    txt = txt.substring(0,2) + "/" + txt.substring(2, 4)
                    binding.manualExpirationDate.setText(txt)
                }
                if (s?.length == 6){
                    binding.manualExpirationDate.setText("")
                }
            }
        })
        binding.btnPayManualCredit.setOnClickListener {
            getManualCreditData()
        }
        createAdapters(binding)
        createObservers(binding)
        return binding.root
    }

    private fun getManualCreditData(){
        val manualCredit = ManualCredit (
            cardHolder = binding.manualCardholder.text.toString(),
            cardNumber = binding.manualCreditCardNumber.text.toString(),
            expirationDate = binding.manualExpirationDate.text.toString(),
            cvv = binding.etCvv.text.toString(),
            postalCode = binding.etZipcode.text.toString()
                )



        viewModel.startManualCredit(manualCredit, viewModel.activeOrder.value!!)
    }

    private fun createObservers(binding: PaymentFragmentBinding){
        viewModel.paymentScreen.observe(viewLifecycleOwner, {
            when (it){
                ShowPayment.NONE -> {
                    binding.cashLayout.visibility = View.GONE
                    binding.discountLayout.visibility = View.GONE
                    binding.priceLayout.visibility = View.GONE
                    binding.creditLayout.visibility = View.GONE
                }
                ShowPayment.CASH -> {
                    binding.cashLayout.visibility = View.VISIBLE
                    binding.discountLayout.visibility = View.GONE
                    binding.priceLayout.visibility = View.GONE
                    binding.creditLayout.visibility = View.GONE
                }
                ShowPayment.CREDIT -> {
                    binding.creditLayout.visibility = View.VISIBLE
                    binding.cashLayout.visibility = View.GONE
                    binding.discountLayout.visibility = View.GONE
                    binding.priceLayout.visibility = View.GONE
                }
                ShowPayment.DISCOUNT -> {
                    binding.discountLayout.visibility = View.VISIBLE
                    binding.cashLayout.visibility = View.GONE
                    binding.priceLayout.visibility = View.GONE
                    binding.creditLayout.visibility = View.GONE
                    discountButtons(binding)
                }
                ShowPayment.MODIFY_PRICE -> {
                    binding.priceLayout.visibility = View.VISIBLE
                    binding.cashLayout.visibility = View.GONE
                    binding.discountLayout.visibility = View.GONE
                    binding.creditLayout.visibility = View.GONE
                }
                else -> {
                    binding.cashLayout.visibility = View.GONE
                    binding.discountLayout.visibility = View.GONE
                    binding.priceLayout.visibility = View.GONE
                    binding.creditLayout.visibility = View.GONE
                }
            }
        })
    }

    private fun createAdapters(binding: PaymentFragmentBinding){
        val ticketNumberAdapter = TicketSideBarAdapter(TicketSideBarAdapter.TicketSideBarListener {
            viewModel.setActiveTicket(it)
        })

        val ticketsAdapter = TicketItemAdapter(TicketItemAdapter.TicketItemListener {
            viewModel.toggleTicketItemMore(it)
        })

        binding.ticketRecycler.adapter = ticketNumberAdapter

        binding.ticketItemsRecycler.adapter = ticketsAdapter

        viewModel.activePayment.observe(viewLifecycleOwner, { item ->
            item?.tickets?.forEach { ticket ->
                if (ticket.uiActive){
                    ticketsAdapter.submitList(ticket.ticketItems)
                    ticketsAdapter.notifyDataSetChanged()

                }
            }
        })
    }

    private fun discountButtons(binding: PaymentFragmentBinding){
        binding.layoutDiscountButtons.removeAllViews()
        val flow = Flow(context)
        flow.id = ViewCompat.generateViewId()
        val flowParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )
        flow.layoutParams = flowParams
        flow.setOrientation(Flow.HORIZONTAL)
        flow.setWrapMode(Flow.WRAP_ALIGNED)
        flow.setVerticalGap(2)
        flow.setVerticalStyle(Flow.CHAIN_SPREAD_INSIDE)
        flow.setHorizontalStyle(Flow.CHAIN_PACKED)
        for (discount in viewModel.settings.discounts) {
            val btn = createDiscountButton(discount)
            binding.layoutDiscountButtons.addView(btn)
            flow.addView(btn)
        }
        binding.layoutDiscountButtons.addView(flow)
    }

    private fun createDiscountButton(discount: Discount): Button {
        val btn = Button(context)
        btn.id = ViewCompat.generateViewId()
        val color = ContextCompat.getColor(requireContext(), R.color.primaryTextColor)
        val white = ContextCompat.getColor(requireContext(), R.color.white)
        btn.backgroundTintList = ColorStateList.valueOf(white)
        btn.setTextColor(ColorStateList.valueOf(color))
        btn.text = discount.discountName
        btn.textSize = 26F

        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        params.setMargins(5, 2, 5, 2)
        btn.setPadding(25, 20, 25, 20)
        btn.layoutParams = params
        btn.width = 450
        btn.height = 180
        btn.setOnClickListener { applyDiscount(discount) }
        return btn

    }

    private fun applyDiscount(discount: Discount){
        //Is it discount ticket or discount item
        viewModel.discountTicket(orderViewModel.activeOrder.value!!, discount)
    }

    private fun modifyPrice(binding: PaymentFragmentBinding){
        viewModel.modifyPrice(orderViewModel.activeOrder.value!!, binding.editModifyPrice.editText?.text.toString())
    }

    private val expirationWatcher = object : TextWatcher {

        override fun afterTextChanged(s: Editable?) {

        }
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {


            var txt = s.toString()

            println(txt)
            println(s!!.length)
            if (s.length == 4){
                txt = txt.substring(0,2) + "/" + txt.substring(2, 4)

                println(txt)
            }



        }
    }


}
