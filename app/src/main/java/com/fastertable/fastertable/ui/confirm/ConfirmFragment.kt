package com.fastertable.fastertable.ui.confirm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ConcatAdapter
import com.fastertable.fastertable.adapters.ConfirmHeaderAdapter
import com.fastertable.fastertable.adapters.ConfirmListAdapter
import com.fastertable.fastertable.common.base.BaseFragment
import com.fastertable.fastertable.databinding.ConfirmFragmentBinding
import com.google.android.material.datepicker.MaterialDatePicker
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ConfirmFragment  : BaseFragment(){
    private val viewModel: ConfirmViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = ConfirmFragmentBinding.inflate(inflater)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        viewModel.getConfirmList()
        bindingObservables(binding)

        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select date")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        binding.btnConfirmDate.setOnClickListener {

        }
        return binding.root
    }

    private fun bindingObservables(binding: ConfirmFragmentBinding){
        val confirmAdapter = ConfirmListAdapter(ConfirmListAdapter.ConfirmListListener {
            //click to show details of checkout
        }, ConfirmListAdapter.ConfirmButtonListener {
            //click to Confirm Checkout
            viewModel.confirm(it)
        })

        val headerAdapter = ConfirmHeaderAdapter()
        val concatAdapter = ConcatAdapter(headerAdapter, confirmAdapter)

        binding.confirmListRecycler.adapter = concatAdapter

        viewModel.confirmList.observe(viewLifecycleOwner, { it ->
            if (it != null){
                confirmAdapter.submitList(it)
                confirmAdapter.notifyDataSetChanged()
            }
        })
    }
}