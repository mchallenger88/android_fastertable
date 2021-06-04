package com.fastertable.fastertable.ui.approvals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.fastertable.fastertable.adapters.ApprovalAdapter
import com.fastertable.fastertable.adapters.ApprovalsSideBarAdapter
import com.fastertable.fastertable.common.base.BaseFragment
import com.fastertable.fastertable.data.models.TicketItem
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
        val approvalsSideBarAdapter = ApprovalsSideBarAdapter(ApprovalsSideBarAdapter.ApprovalSideBarListener { it ->
            viewModel.onApprovalSidebarClick(it)
        })

        val ticketsAdapter = ApprovalAdapter(ApprovalAdapter.ApprovalListener { it ->

        })

        binding.approvalsSideBarRecycler.adapter = approvalsSideBarAdapter

        binding.approvalItemsRecycler.adapter = ticketsAdapter

        viewModel.showPending.observe(viewLifecycleOwner, { it ->
            viewModel.setApprovalsView(it)
        })

        viewModel.liveApprovalItem.observe(viewLifecycleOwner, {
            if (it.ticket != null){
                ticketsAdapter.submitList(it.ticket?.ticketItems)
            }

            if (it.ticketItem != null){
                val ticketItems = arrayListOf<TicketItem>()
                ticketItems.add(it.ticketItem)
                ticketsAdapter.submitList(ticketItems)
            }

            ticketsAdapter.notifyDataSetChanged()
        })

        viewModel.approvalItems.observe(viewLifecycleOwner, {
            approvalsSideBarAdapter.submitList(it)
            approvalsSideBarAdapter.notifyDataSetChanged()
        })

    }
}