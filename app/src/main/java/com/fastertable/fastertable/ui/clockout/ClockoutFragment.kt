package com.fastertable.fastertable.ui.clockout

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.fastertable.fastertable.common.base.BaseFragment
import com.fastertable.fastertable.databinding.ClockoutFragmentBinding
import com.fastertable.fastertable.ui.dialogs.AssignTableDialog
import com.fastertable.fastertable.ui.dialogs.BaseDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ClockoutFragment : BaseFragment(){
    private val viewModel: ClockoutViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = ClockoutFragmentBinding.inflate(inflater)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        viewModel.errorMessage.observe(viewLifecycleOwner, {it ->
            if (it != ""){
                binding.txtClockoutError.text = it
            }
        })
        return binding.root
    }


}