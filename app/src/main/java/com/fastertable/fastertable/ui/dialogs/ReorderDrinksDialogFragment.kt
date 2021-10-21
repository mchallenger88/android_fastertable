package com.fastertable.fastertable.ui.dialogs

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.fastertable.fastertable.adapters.DrinkListAdapter
import com.fastertable.fastertable.data.models.ReorderDrink
import com.fastertable.fastertable.databinding.DialogReorderDrinksBinding
import com.fastertable.fastertable.ui.order.OrderViewModel

class ReorderDrinksDialogFragment  : BaseDialog() {
    private val viewModel: OrderViewModel by activityViewModels()
    private var drinksList = mutableListOf<ReorderDrink>()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = DialogReorderDrinksBinding.inflate(inflater)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        createAdapter(binding)

        binding.btnDrinksOk.setOnClickListener {
            viewModel.addDrinksToOrder(drinksList)
            dismiss()
        }

        binding.btnDrinksClose.setOnClickListener {
            dismiss()
        }


        return binding.root
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
        dialog!!.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }


    companion object {
        fun newInstance(): ReorderDrinksDialogFragment {
            return ReorderDrinksDialogFragment()
        }

        const val TAG = "ReorderDrinksDialogFragment"
    }
}