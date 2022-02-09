package com.fastertable.fastertable2022.ui.approvals

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ConcatAdapter
import by.kirich1409.viewbindingdelegate.viewBinding
import com.fastertable.fastertable2022.R
import com.fastertable.fastertable2022.adapters.ApprovalAdapter
import com.fastertable.fastertable2022.adapters.ApprovalHeaderAdapter
import com.fastertable.fastertable2022.adapters.ApprovalsSideBarAdapter
import com.fastertable.fastertable2022.common.base.BaseFragment
import com.fastertable.fastertable2022.data.models.Approval
import com.fastertable.fastertable2022.databinding.ApprovalsFragmentBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ApprovalsFragment : BaseFragment(R.layout.approvals_fragment) {
    private val viewModel: ApprovalsViewModel by activityViewModels()
    private val binding: ApprovalsFragmentBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        createAdapters(binding)

        binding.btnSaveApproval.setOnClickListener {
            binding.btnSaveApproval.isEnabled = false
        }
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

        viewModel.approvals.observe(viewLifecycleOwner, {
            if (it != null){
                if (viewModel.showPending.value == true){
                    approvalsSideBarAdapter.submitList(it)
                    approvalsSideBarAdapter.notifyDataSetChanged()
                    viewModel.setActiveApproval()
                }else{
                    approvalsSideBarAdapter.submitList(null)
                    approvalsSideBarAdapter.notifyDataSetChanged()
                }
            }

        })

    }

}