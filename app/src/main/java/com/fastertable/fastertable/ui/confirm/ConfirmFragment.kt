package com.fastertable.fastertable.ui.confirm

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ConcatAdapter
import by.kirich1409.viewbindingdelegate.viewBinding
import com.fastertable.fastertable.R
import com.fastertable.fastertable.adapters.ConfirmHeaderAdapter
import com.fastertable.fastertable.adapters.ConfirmListAdapter
import com.fastertable.fastertable.common.base.BaseFragment
import com.fastertable.fastertable.databinding.ConfirmFragmentBinding
import com.fastertable.fastertable.ui.dialogs.DatePickerViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ConfirmFragment  : BaseFragment(R.layout.confirm_fragment) {
    private val viewModel: ConfirmViewModel by activityViewModels()
    private val dateViewModel: DatePickerViewModel by activityViewModels()
    private val binding: ConfirmFragmentBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        viewModel.getConfirmList()
        bindingObservables(binding)

        binding.btnConfirmDate.setOnClickListener {
            dateViewModel.setSource("Confirm")
        }

        viewModel.progressVisibility.observe( viewLifecycleOwner, {
            if (it){
                binding.confirmProgressBar.visibility = View.VISIBLE
            }else{
                binding.confirmProgressBar.visibility = View.INVISIBLE
            }
        })
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

        viewModel.confirmList.observe(viewLifecycleOwner, {
            if (it != null){
                confirmAdapter.submitList(it)
                confirmAdapter.notifyDataSetChanged()
            }
        })
    }


}