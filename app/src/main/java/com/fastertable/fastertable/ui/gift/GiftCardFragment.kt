package com.fastertable.fastertable.ui.gift

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.fastertable.fastertable.adapters.TicketItemAdapter
import com.fastertable.fastertable.common.base.BaseFragment
import com.fastertable.fastertable.databinding.GiftCardFragmentBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GiftCardFragment : BaseFragment(){
    private val viewModel: GiftCardViewModel by activityViewModels()
    private lateinit var binding: GiftCardFragmentBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = GiftCardFragmentBinding.inflate(inflater)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        val ticketsAdapter = TicketItemAdapter(TicketItemAdapter.TicketItemListener {
        })
        binding.ticketItemsRecycler.adapter = ticketsAdapter

        viewModel.activePayment.observe(viewLifecycleOwner, { item ->
            item?.tickets?.forEach { ticket ->
                if (ticket.uiActive){
                    ticketsAdapter.submitList(ticket.ticketItems)
                    ticketsAdapter.notifyDataSetChanged()

                }
            }
        })

        binding.btnAddAny.setOnClickListener {
            setAnyAmount(binding)
        }

        createObservers(binding)

        return binding.root
    }

    private fun createObservers(binding: GiftCardFragmentBinding){
        viewModel.giftScreen.observe(viewLifecycleOwner, {
            when (it){
                ShowGift.ADD_CASH -> {
                    binding.giftAddCashLayout.visibility = View.VISIBLE
                    binding.giftBalanceLayout .visibility = View.GONE
                    binding.giftCashLayout.visibility = View.GONE
                    binding.giftSwipeLayout.visibility = View.GONE
                }
                ShowGift.BALANCE_INQUIRY -> {
                    binding.giftAddCashLayout.visibility = View.GONE
                    binding.giftBalanceLayout .visibility = View.VISIBLE
                    binding.giftCashLayout.visibility = View.GONE
                    binding.giftSwipeLayout.visibility = View.GONE
                }
                ShowGift.PAY_CASH -> {
                    binding.giftAddCashLayout.visibility = View.GONE
                    binding.giftBalanceLayout .visibility = View.GONE
                    binding.giftCashLayout.visibility = View.VISIBLE
                    binding.giftSwipeLayout.visibility = View.GONE
                }
                ShowGift.SWIPE_CARD -> {
                    binding.giftAddCashLayout.visibility = View.GONE
                    binding.giftBalanceLayout .visibility = View.GONE
                    binding.giftCashLayout.visibility = View.GONE
                    binding.giftSwipeLayout.visibility = View.VISIBLE
                }
                ShowGift.PAY_CREDIT -> {
                    binding.giftAddCashLayout.visibility = View.GONE
                    binding.giftBalanceLayout .visibility = View.GONE
                    binding.giftCashLayout.visibility = View.GONE
                    binding.giftSwipeLayout.visibility = View.VISIBLE
                }
                else -> {
                    binding.giftAddCashLayout.visibility = View.VISIBLE
                    binding.giftBalanceLayout .visibility = View.GONE
                    binding.giftCashLayout.visibility = View.GONE
                    binding.giftSwipeLayout.visibility = View.GONE
                }
            }
        })
    }

    private fun setAnyAmount(binding: GiftCardFragmentBinding){
        val amount = binding.editAnyAmount.editText?.text.toString()
        if (amount != ""){
            viewModel.addGiftAmount(amount.toDouble())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.unbind()
    }
}