package com.fastertable.fastertable.ui.payment

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
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
import com.fastertable.fastertable.data.models.Discount
import com.fastertable.fastertable.databinding.PaymentFragmentBinding
import com.fastertable.fastertable.ui.order.OrderViewModel
import dagger.hilt.android.AndroidEntryPoint
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.InputMethodManager
import by.kirich1409.viewbindingdelegate.viewBinding
import com.fastertable.fastertable.common.base.BaseFragment
import com.fastertable.fastertable.data.models.ManualCredit


@AndroidEntryPoint
class PaymentFragment: BaseFragment(R.layout.payment_fragment) {
    private val viewModel: PaymentViewModel by activityViewModels()
    private val orderViewModel: OrderViewModel by activityViewModels()
    private val binding: PaymentFragmentBinding by viewBinding()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        binding.orderViewModel = orderViewModel

        binding.txtModifyPrice.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.setModifyPrice(s.toString())
            }
        })

        binding.btnModifyPrice.setOnClickListener {
            modifyPrice()
        }
        manualCreditCardInputs()

        viewModel.showNone()
        createAdapters(binding)
        createObservers(binding)
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
        if (viewModel.liveTicketItem.value != null){
            viewModel.initialDiscountItem(discount)
        }else{
            viewModel.initialDiscountTicket(discount)
        }

    }

    private fun modifyPrice(){
        hideKeyboardFrom(requireContext(), requireView())
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


    private fun hideKeyboardFrom(context: Context, view: View) {
        val imm: InputMethodManager =
            context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun manualCreditCardInputs(){
        binding.manualExpirationDate.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                var txt = s.toString()
                if (s?.length == 4 && !txt.contains("/")){
                    txt = txt.substring(0,2) + "/" + txt.substring(2, 4)
                    viewModel.setExpirationDate(txt)
                }
                if (s?.length == 6){
                    viewModel.setExpirationDate("")
                }
            }
        })
        binding.manualCardholder.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.setCardHolder(s.toString())
            }
        })
        binding.manualCreditCardNumber.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.setCardNumber(s.toString())
            }
        })
        binding.etCvv.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.setCVV(s.toString())
            }
        })
        binding.etZipcode.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.setZipcode(s.toString())
            }
        })
    }
}
