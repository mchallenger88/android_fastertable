package com.fastertable.fastertable.ui.menus

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.fastertable.fastertable.common.base.BaseFragment
import com.fastertable.fastertable.data.repository.MenusRepository
import com.fastertable.fastertable.data.repository.OrderRepository
import com.fastertable.fastertable.databinding.MenusFragmentBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MenusFragment : BaseFragment(){
    private lateinit var viewModel: MenusViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = MenusFragmentBinding.inflate(inflater)

        viewModel = ViewModelProvider(this).get(MenusViewModel::class.java)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel




            return binding.root
    }

}