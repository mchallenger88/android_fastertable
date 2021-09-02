package com.fastertable.fastertable.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.fastertable.fastertable.MainActivity
import com.fastertable.fastertable.OrderActivity
import com.fastertable.fastertable.R
import com.fastertable.fastertable.api.CompanyLoginUseCase
import com.fastertable.fastertable.common.base.BaseFragment
import com.fastertable.fastertable.databinding.HomeFragmentBinding
import com.fastertable.fastertable.data.repository.LoginRepository
import com.fastertable.fastertable.data.repository.OrderRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : BaseFragment() {

    private val viewModel: HomeViewModel by activityViewModels()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val binding = HomeFragmentBinding.inflate(inflater)

        val navController = findNavController()

        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        setChipDefaults(binding)

        viewModel.showProgressBar.observe(viewLifecycleOwner, Observer { it ->
            if (it){
                binding.progressBarHome.visibility = View.VISIBLE
            }else{
                binding.progressBarHome.visibility = View.INVISIBLE
            }
        })

        viewModel.viewLoaded.observe(viewLifecycleOwner, Observer { it ->
            if (it){
                viewModel.filterOrders("Open")
            }
        })


        viewModel.orderFilter.observe(viewLifecycleOwner, Observer {  it ->

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

        viewModel.filteredOrders.observe(viewLifecycleOwner, Observer {
            it?.let{
                orderAdapter.submitList(it)
            }
        })

        val headerAdapter = OrderListHeaderAdapter()
        val concatAdapter = ConcatAdapter(headerAdapter, orderAdapter)

        binding.orderRecycler.adapter = concatAdapter

        val manager = LinearLayoutManager(activity)
        binding.orderRecycler.layoutManager = manager

                return binding.root
    }

    private fun setChipDefaults(binding: HomeFragmentBinding){
        binding.chipAllOrders.setChipBackgroundColorResource(R.color.primaryColor)
        binding.chipAllOrders.setTextAppearance(R.style.ChipTextAppearance)
        binding.chipAllOrders.setOnClickListener{ viewModel.onAllClicked()}

        binding.chipClosedOrders.setChipBackgroundColorResource(R.color.primaryColor)
        binding.chipClosedOrders.setTextAppearance(R.style.ChipTextAppearance)
        binding.chipClosedOrders.setOnClickListener{ viewModel.onClosedClicked()}

        binding.chipOpenOrders.setChipBackgroundColorResource(R.color.primaryColor)
        binding.chipOpenOrders.setTextAppearance(R.style.ChipTextAppearance)
        binding.chipOpenOrders.setOnClickListener{ viewModel.onOpenClicked()}
    }

}