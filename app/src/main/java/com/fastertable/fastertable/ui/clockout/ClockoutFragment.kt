package com.fastertable.fastertable.ui.clockout

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.fastertable.fastertable.R
import com.fastertable.fastertable.common.base.BaseFragment
import com.fastertable.fastertable.databinding.ClockoutFragmentBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ClockoutFragment : BaseFragment(R.layout.clockout_fragment) {
    private val viewModel: ClockoutViewModel by activityViewModels()
    private val binding: ClockoutFragmentBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        viewModel.errorMessage.observe(viewLifecycleOwner, {
            if (it != ""){
                binding.txtClockoutError.text = it
            }
        })
    }

}