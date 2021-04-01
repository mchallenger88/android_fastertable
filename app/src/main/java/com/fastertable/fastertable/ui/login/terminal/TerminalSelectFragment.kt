package com.fastertable.fastertable.ui.login.terminal

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.fastertable.fastertable.R
import com.fastertable.fastertable.data.Terminal
import com.fastertable.fastertable.data.repository.LoginRepository
import com.fastertable.fastertable.databinding.TerminalSelectFragmentBinding
import com.google.android.material.chip.Chip

class TerminalSelectFragment : Fragment()  {
    private lateinit var viewModel: TerminalSelectViewModel

    @SuppressLint("ResourceAsColor")
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View? {
        val binding = TerminalSelectFragmentBinding.inflate(inflater)
        val application = requireNotNull(activity).application
        val loginRepository = LoginRepository(application)
        val viewModelFactory = TerminalSelectViewModelFactory(loginRepository)
        viewModel = ViewModelProvider(
                this, viewModelFactory).get(TerminalSelectViewModel::class.java)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        viewModel.settings.observe(viewLifecycleOwner, Observer { it ->
            it.terminals.forEach { terminal ->
                val restChip = Chip(activity)
                restChip.id = ViewCompat.generateViewId();
                restChip.text = terminal.terminalName
                restChip.setChipBackgroundColorResource(R.color.primaryColor);
                restChip.setTextAppearance(R.style.ChipTextAppearance);
                restChip.setOnClickListener{ setTerminal( terminal )}

                if (viewModel.terminal.value != null){
                    if (viewModel.terminal.value!!.terminalId == terminal.terminalId){
                        restChip.setChipBackgroundColorResource(R.color.secondaryColor);
                    }

                }
                binding.chipsRestaurants.addView(restChip)

            }
        })
        viewModel.terminal.observe(viewLifecycleOwner, Observer { terminal ->
            if (terminal != null){
                this.findNavController().navigate(TerminalSelectFragmentDirections.actionTerminalSelectFragmentToUserLoginFragment())
            }
        })
        return binding.root
    }



    private fun setTerminal(t: Terminal){
        viewModel.setTerminal((t))
    }
}