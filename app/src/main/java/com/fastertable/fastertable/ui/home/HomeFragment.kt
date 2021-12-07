package com.fastertable.fastertable.ui.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import com.fastertable.fastertable.R
import com.fastertable.fastertable.common.base.BaseFragment
import com.fastertable.fastertable.databinding.HomeFragmentBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : BaseFragment(R.layout.home_fragment) {
    private val viewModel: HomeViewModel by activityViewModels()
    private val binding: HomeFragmentBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel.showProgressBar.observe(viewLifecycleOwner, {
            if (it){
                binding.progressBarHome.visibility = View.VISIBLE
            }else{
                binding.progressBarHome.visibility = View.INVISIBLE
            }
        })

        binding.chipAllOrders.setOnClickListener{
            viewModel.setOrderFilter("All")
        }

        binding.chipOpenOrders.setOnClickListener{
            viewModel.setOrderFilter("Open")
        }

        binding.chipClosedOrders.setOnClickListener{
            viewModel.setOrderFilter("Closed")
        }

        viewModel.orders.observe(viewLifecycleOwner, {
            viewModel.setOrderFilter("Open")
        })


        val orderAdapter = OrderListAdapter(OrderListListener {
                orderId ->  viewModel.onOrderClicked(orderId)
        })

        viewModel.filteredOrders.observe(viewLifecycleOwner, {
            it?.let{
                orderAdapter.submitList(it)
                orderAdapter.notifyDataSetChanged()
            }
        })

        val headerAdapter = OrderListHeaderAdapter()
        val concatAdapter = ConcatAdapter(headerAdapter, orderAdapter)

        binding.orderRecycler.adapter = concatAdapter

        val manager = LinearLayoutManager(activity)
        binding.orderRecycler.layoutManager = manager
    }

}