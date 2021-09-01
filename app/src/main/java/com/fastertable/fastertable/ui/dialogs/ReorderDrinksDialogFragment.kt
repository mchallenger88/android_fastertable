package com.fastertable.fastertable.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.fastertable.fastertable.adapters.DrinkListAdapter
import com.fastertable.fastertable.databinding.DialogReorderDrinksBinding
import com.fastertable.fastertable.ui.order.OrderViewModel

class ReorderDrinksDialogFragment  : BaseDialog() {
    private val viewModel: OrderViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = DialogReorderDrinksBinding.inflate(inflater)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        createAdapter(binding)

        binding.btnDrinksOk.setOnClickListener {
            viewModel.addDrinksToOrder()
            dismiss()
        }

        binding.btnDrinksClose.setOnClickListener {
            dismiss()
        }


        return binding.root
    }

    private fun createAdapter(binding: DialogReorderDrinksBinding){
        val adapter = DrinkListAdapter()
        binding.drinksRecycler.adapter = adapter

        viewModel.drinkList.observe(viewLifecycleOwner, {
            adapter.submitList(it)
            adapter.notifyDataSetChanged()
        })
    }

    override fun onStart() {
        super.onStart()
        val width = 800
        dialog!!.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }


    companion object {
        fun newInstance(): ReorderDrinksDialogFragment {
            return ReorderDrinksDialogFragment()
        }

        const val TAG = "ReorderDrinksDialogFragment"
    }
}