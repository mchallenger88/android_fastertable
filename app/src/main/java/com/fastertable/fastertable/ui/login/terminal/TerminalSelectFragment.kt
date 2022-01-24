package com.fastertable.fastertable.ui.login.terminal

import EpsonDiscovery
import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
import technology.master.kotlinprint.printer.DiscoveredPrinter
import technology.master.kotlinprint.printer.DiscoverySettings
import technology.master.kotlinprint.printer.PrinterDriver

@AndroidEntryPoint
class TerminalSelectFragment : BaseFragment(R.layout.terminal_select_fragment) {
    private val viewModel: TerminalSelectViewModel by activityViewModels()
    private val binding: TerminalSelectFragmentBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        viewModel.getSettings()
        binding.txtBluetoothStatus.text = ""

        binding.btnFindBluetooth.setOnClickListener{
            viewModel.setDiscover(true)
        }

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