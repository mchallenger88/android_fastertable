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
import com.fastertable.fastertable.ui.dialogs.DatePickerViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ConfirmFragment  : BaseFragment(){
    private val viewModel: ConfirmViewModel by activityViewModels()
    private val dateViewModel: DatePickerViewModel by activityViewModels()
    private lateinit var binding: ConfirmFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ConfirmFragmentBinding.inflate(inflater)
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

        viewModel.confirmList.observe(viewLifecycleOwner, {
            if (it != null){
                confirmAdapter.submitList(it)
                confirmAdapter.notifyDataSetChanged()
            }
        })
    }
    override fun onDestroyView() {
        super.onDestroyView()
        binding.unbind()
    }

}