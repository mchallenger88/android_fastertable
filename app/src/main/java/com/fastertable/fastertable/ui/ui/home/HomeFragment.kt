package com.fastertable.fastertable.ui.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.fastertable.fastertable.R
import com.fastertable.fastertable.databinding.HomeFragmentBinding
import com.fastertable.fastertable.data.repository.LoginRepository
import com.fastertable.fastertable.data.repository.OrderRepository

class HomeFragment : Fragment() {

    private lateinit var viewModel: HomeViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val binding = HomeFragmentBinding.inflate(inflater)
        val application = requireNotNull(activity).application
        val loginRepository = LoginRepository(application)
        val orderRepository = OrderRepository(application)
        val navController = findNavController()
        val viewModelFactory = HomeViewModelFactory(loginRepository, orderRepository)
        viewModel = ViewModelProvider(
            this, viewModelFactory).get(HomeViewModel::class.java)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        viewModel = ViewModelProvider(
            this, viewModelFactory).get(HomeViewModel::class.java)


        viewModel.company.observe(viewLifecycleOwner, Observer { company ->
            if(company == null){
                navController.navigate(R.id.companyLoginFragment)
            }
        })

        viewModel.settings.observe(viewLifecycleOwner, Observer { settings ->
            if(settings == null){
                navController.navigate(R.id.companyLoginFragment)
            }
        })

        viewModel.terminal.observe(viewLifecycleOwner, Observer { terminal ->
            if(terminal == null){
                navController.navigate(R.id.terminalSelectFragment)
            }
        })

        viewModel.showProgressBar.observe(viewLifecycleOwner, Observer { it ->
            if (it){
                binding.progressBarHome.visibility = View.VISIBLE
            }else{
                binding.progressBarHome.visibility = View.INVISIBLE
            }
        })

        val orderAdapter = OrderListAdapter(OrderListListener {
            orderId ->  viewModel.onOrderClicked(orderId)
        })

        viewModel.orders.observe(viewLifecycleOwner, Observer {
            it?.let{
                orderAdapter.submitList(it)
            }
        })

        val headerAdapter = HeaderTestAdapter()
        val concatAdapter = ConcatAdapter(headerAdapter, orderAdapter)

        binding.orderRecycler.adapter = concatAdapter

        val manager = LinearLayoutManager(activity)
        binding.orderRecycler.layoutManager = manager

                return binding.root
    }
}