package com.fastertable.fastertable.ui.clockout

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.fastertable.fastertable.common.base.BaseFragment
import com.fastertable.fastertable.databinding.ClockoutFragmentBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ClockoutFragment : BaseFragment(){
    private val viewModel: ClockoutViewModel by activityViewModels()
    private lateinit var binding: ClockoutFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ClockoutFragmentBinding.inflate(inflater)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        viewModel.errorMessage.observe(viewLifecycleOwner, {
            if (it != ""){
                binding.txtClockoutError.text = it
            }
        })
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.unbind()
    }
}