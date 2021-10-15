package com.fastertable.fastertable.ui.login.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.fastertable.fastertable.common.base.BaseFragment
import com.fastertable.fastertable.databinding.KitchenClockoutFragmentBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class KitchenClockoutFragment : BaseFragment(){
    private val viewModel: KitchenClockoutViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = KitchenClockoutFragmentBinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        viewModel.errorMessage.observe(viewLifecycleOwner, {
            if (it != ""){
                binding.txtClockoutError.text = it
            }
        })
        return binding.root
    }

}