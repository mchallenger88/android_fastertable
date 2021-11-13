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

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        setChipDefaults(binding)

        viewModel.showProgressBar.observe(viewLifecycleOwner, {
            if (it){
                binding.progressBarHome.visibility = View.VISIBLE
            }else{
                binding.progressBarHome.visibility = View.INVISIBLE
            }
        })

        viewModel.viewLoaded.observe(viewLifecycleOwner, {
            if (it){
                viewModel.filterOrders("Open")
            }
        })


        viewModel.orderFilter.observe(viewLifecycleOwner, {

            if (it == "All"){
                binding.chipAllOrders.setChipBackgroundColorResource(R.color.secondaryColor)
                binding.chipClosedOrders.setChipBackgroundColorResource(R.color.primaryColor)
                binding.chipOpenOrders.setChipBackgroundColorResource(R.color.primaryColor)
            }

            if (it == "Open"){
                binding.chipOpenOrders.setChipBackgroundColorResource(R.color.secondaryColor)
                binding.chipAllOrders.setChipBackgroundColorResource(R.color.primaryColor)
                binding.chipClosedOrders.setChipBackgroundColorResource(R.color.primaryColor)
            }

            if (it == "Closed"){
                binding.chipClosedOrders.setChipBackgroundColorResource(R.color.secondaryColor)
                binding.chipAllOrders.setChipBackgroundColorResource(R.color.primaryColor)
                binding.chipOpenOrders.setChipBackgroundColorResource(R.color.primaryColor)
            }

        })

        val orderAdapter = OrderListAdapter(OrderListListener {
                orderId ->  viewModel.onOrderClicked(orderId)
        })

        viewModel.filteredOrders.observe(viewLifecycleOwner, {
            it?.let{
                orderAdapter.submitList(it)
            }
        })

        val headerAdapter = OrderListHeaderAdapter()
        val concatAdapter = ConcatAdapter(headerAdapter, orderAdapter)

        binding.orderRecycler.adapter = concatAdapter

        val manager = LinearLayoutManager(activity)
        binding.orderRecycler.layoutManager = manager
    }


    private fun setChipDefaults(binding: HomeFragmentBinding){
        binding.chipAllOrders.setChipBackgroundColorResource(R.color.primaryColor)
        binding.chipAllOrders.setTextAppearance(R.style.ChipTextAppearance)
        binding.chipAllOrders.setOnClickListener{ viewModel.onAllClicked()}

        binding.chipClosedOrders.setChipBackgroundColorResource(R.color.primaryColor)
        binding.chipClosedOrders.setTextAppearance(R.style.ChipTextAppearance)
        binding.chipClosedOrders.setOnClickListener{ viewModel.onClosedClicked()}

        binding.chipOpenOrders.setChipBackgroundColorResource(R.color.secondaryColor)
        binding.chipOpenOrders.setTextAppearance(R.style.ChipTextAppearance)
        binding.chipOpenOrders.setOnClickListener{ viewModel.onOpenClicked()}
    }

}