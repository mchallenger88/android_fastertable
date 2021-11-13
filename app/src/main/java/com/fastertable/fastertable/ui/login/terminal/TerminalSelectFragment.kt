package com.fastertable.fastertable.ui.login.terminal

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ConcatAdapter
import by.kirich1409.viewbindingdelegate.viewBinding
import com.fastertable.fastertable.R
import com.fastertable.fastertable.adapters.TerminalAdapter
import com.fastertable.fastertable.adapters.TerminalHeaderAdapter
import com.fastertable.fastertable.common.base.BaseFragment
import com.fastertable.fastertable.data.models.Terminal
import com.fastertable.fastertable.databinding.TerminalSelectFragmentBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TerminalSelectFragment : BaseFragment(R.layout.terminal_select_fragment) {
    private val viewModel: TerminalSelectViewModel by activityViewModels()
    private val binding: TerminalSelectFragmentBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        val adapter = TerminalAdapter(TerminalAdapter.TerminalItemListener { item ->
            setTerminal(item)
        })

        val termHeadAdapter = TerminalHeaderAdapter()
        val concatAdapter = ConcatAdapter(termHeadAdapter, adapter)
        binding.terminalRecycler.adapter = concatAdapter

        viewModel.settings.observe(viewLifecycleOwner, {
            if (it != null) {
                adapter.submitList(it.terminals)
                adapter.notifyDataSetChanged()
            }
        })
    }

    private fun setTerminal(t: Terminal){
        viewModel.setTerminal((t))
    }

}