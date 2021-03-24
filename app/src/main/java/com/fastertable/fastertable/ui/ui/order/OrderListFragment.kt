package com.fastertable.fastertable.ui.ui.order

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.fastertable.fastertable.databinding.OrderListFragmentBinding


class OrderListFragment: Fragment() {
    private lateinit var viewModel: OrderListViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View? {

        val binding = OrderListFragmentBinding.inflate(inflater)
        val application = requireNotNull(activity).application
        val viewModelFactory = OrderListViewModelFactory(application)
        viewModel = ViewModelProvider(
            this, viewModelFactory).get(OrderListViewModel::class.java)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
            return binding.root
    }
}