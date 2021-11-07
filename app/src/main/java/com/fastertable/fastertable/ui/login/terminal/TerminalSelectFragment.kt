package com.fastertable.fastertable.ui.login.terminal

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ConcatAdapter
import com.fastertable.fastertable.adapters.TerminalAdapter
import com.fastertable.fastertable.adapters.TerminalHeaderAdapter
import com.fastertable.fastertable.common.base.BaseFragment
import com.fastertable.fastertable.data.models.Terminal
import com.fastertable.fastertable.databinding.TerminalSelectFragmentBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TerminalSelectFragment : BaseFragment()  {
    private val viewModel: TerminalSelectViewModel by activityViewModels()
    private lateinit var binding: TerminalSelectFragmentBinding

    @SuppressLint("ResourceAsColor")
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View {
        binding = TerminalSelectFragmentBinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        val adapter = TerminalAdapter(TerminalAdapter.TerminalItemListener { item ->
            setTerminal(item)
        })

        val termHeadAdapter = TerminalHeaderAdapter()
        val concatAdapter = ConcatAdapter(termHeadAdapter, adapter)
        binding.terminalRecycler.adapter = concatAdapter

        viewModel.settings.observe(viewLifecycleOwner, {
//            it?.terminals?.forEach { terminal ->
//                val restChip = Chip(activity)
//                restChip.id = ViewCompat.generateViewId();
//                restChip.text = terminal.terminalName
//                restChip.setChipBackgroundColorResource(R.color.primaryColor);
//                restChip.setTextAppearance(R.style.ChipTextAppearance);
//                restChip.setOnClickListener{ setTerminal( terminal )}
//
//                if (viewModel.terminal.value != null){
//                    if (viewModel.terminal.value!!.terminalId == terminal.terminalId){
//                        restChip.setChipBackgroundColorResource(R.color.secondaryColor);
//                    }
//
//                }
//                binding.chipsRestaurants.addView(restChip)
//
//            }
            if (it != null) {
               adapter.submitList(it.terminals)
                adapter.notifyDataSetChanged()
            }
        })



        return binding.root
    }

    private fun setTerminal(t: Terminal){
        viewModel.setTerminal((t))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.unbind()
    }
}