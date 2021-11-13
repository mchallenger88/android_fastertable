package com.fastertable.fastertable.ui.approvals

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.fastertable.fastertable.R
import com.fastertable.fastertable.adapters.ClosedApprovalAdapter
import com.fastertable.fastertable.common.base.BaseFragment
import com.fastertable.fastertable.databinding.ApprovalsCompletedFragmentBinding

import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CompletedApprovalsFragment : BaseFragment(R.layout.approvals_completed_fragment) {
    private val viewModel: CompletedApprovalsViewModel by activityViewModels()
    private val binding: ApprovalsCompletedFragmentBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        createAdapters(binding)
    }


    fun createAdapters(binding: ApprovalsCompletedFragmentBinding){
        val closedAdapter = ClosedApprovalAdapter()
        binding.closedItemsRecycler.adapter = closedAdapter

        viewModel.approvals.observe(viewLifecycleOwner, {
            closedAdapter.submitList(it)
            closedAdapter.notifyDataSetChanged()
        })
    }


}