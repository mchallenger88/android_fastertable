package com.fastertable.fastertable.ui.menus

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import by.kirich1409.viewbindingdelegate.viewBinding
import com.fastertable.fastertable.R
import com.fastertable.fastertable.common.base.BaseFragment
import com.fastertable.fastertable.databinding.MenusFragmentBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MenusFragment : BaseFragment(R.layout.menus_fragment) {
    private lateinit var viewModel: MenusViewModel
    private val binding: MenusFragmentBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(MenusViewModel::class.java)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
    }


}