package com.fastertable.fastertable.ui.approvals

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ConcatAdapter
import com.fastertable.fastertable.adapters.ApprovalAdapter
import com.fastertable.fastertable.adapters.ApprovalHeaderAdapter
import com.fastertable.fastertable.adapters.ApprovalsSideBarAdapter
import com.fastertable.fastertable.common.base.BaseFragment
import com.fastertable.fastertable.data.models.Approval
import com.fastertable.fastertable.databinding.ApprovalsFragmentBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ApprovalsFragment : BaseFragment() {
    private val viewModel: ApprovalsViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = ApprovalsFragmentBinding.inflate(inflater)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        createAdapters(binding)
        return binding.root
    }

    private fun createAdapters(binding: ApprovalsFragmentBinding){
        val approvalsSideBarAdapter = ApprovalsSideBarAdapter(ApprovalsSideBarAdapter.ApprovalSideBarListener {
            viewModel.onApprovalSidebarClick(it)
        })

        val headerAdapter = ApprovalHeaderAdapter(ApprovalHeaderAdapter.ApproveAllListener {
            viewModel.addAllToList(it)
        })

        val ticketsAdapter = ApprovalAdapter(ApprovalAdapter.ApproveListener {
            viewModel.addItemToApprovalList(it)
        }, ApprovalAdapter.RejectListener {
            viewModel.addItemToRejectList(it)
        })

        val concatAdapter = ConcatAdapter(headerAdapter, ticketsAdapter)

        binding.approvalsSideBarRecycler.adapter = approvalsSideBarAdapter

        binding.approvalItemsRecycler.adapter = concatAdapter

        viewModel.approvalTicket.observe(viewLifecycleOwner, {
            if (it != null){
                ticketsAdapter.submitList(it.ticket.ticketItems)
            }else{
                ticketsAdapter.submitList(null)
            }
            ticketsAdapter.notifyDataSetChanged()
        })

        viewModel.activeApproval.observe(viewLifecycleOwner, {
            if (it != null){
                val list = mutableListOf<Approval>()
                list.add(it.approval)
                headerAdapter.submitList(list)
                headerAdapter.notifyDataSetChanged()
            }

        })

        viewModel.approvalsShown.observe(viewLifecycleOwner, {
            approvalsSideBarAdapter.submitList(it)
            approvalsSideBarAdapter.notifyDataSetChanged()
        })

    }
}