package com.fastertable.fastertable2022.ui.dialogs

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.fastertable.fastertable2022.R
import com.fastertable.fastertable2022.adapters.DrinkListAdapter
import com.fastertable.fastertable2022.data.models.ReorderDrink
import com.fastertable.fastertable2022.databinding.DialogReorderDrinksBinding
import com.fastertable.fastertable2022.ui.order.OrderViewModel

class ReorderDrinksDialogFragment  : BaseDialog(R.layout.dialog_reorder_drinks) {
    private val viewModel: OrderViewModel by activityViewModels()
    private val binding: DialogReorderDrinksBinding by viewBinding()
    private var drinksList = mutableListOf<ReorderDrink>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        createAdapter(binding)

        binding.btnDrinksOk.setOnClickListener {
            if (viewModel.activePayment.value != null){
                if (viewModel.activePayment.value!!.anyTicketsPaid()){
                    dismiss()
                    viewModel.setError("Tickets Paid", "Payments have been made on this order. You will have to void the payments or begin a new order.")
                }else{
                    viewModel.addDrinksToOrder(drinksList)
                    dismiss()
                }
            }else{
                viewModel.addDrinksToOrder(drinksList)
                dismiss()
            }
        }

        binding.btnDrinksClose.setOnClickListener {
            dismiss()
        }
    }

    private fun createAdapter(binding: DialogReorderDrinksBinding){
        val adapter = DrinkListAdapter(DrinkListAdapter.AddDrinkListener {
            drinksList.add(it)
        }, DrinkListAdapter.RemoveDrinkListener {
            drinksList.remove(it)
        })
        binding.drinksRecycler.adapter = adapter

        viewModel.drinkList.observe(viewLifecycleOwner, {
            adapter.submitList(it)
            adapter.notifyDataSetChanged()
        })
    }

    override fun onStart() {
        super.onStart()
        val width = 1000
        dialog?.let {
            it.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }


    companion object {
        fun newInstance(): ReorderDrinksDialogFragment {
            return ReorderDrinksDialogFragment()
        }

        const val TAG = "ReorderDrinksDialogFragment"
    }
}