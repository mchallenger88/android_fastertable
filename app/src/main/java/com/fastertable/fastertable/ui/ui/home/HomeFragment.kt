package com.fastertable.fastertable.ui.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.fastertable.fastertable.R
import com.fastertable.fastertable.databinding.FragmentHomeBinding
import com.fastertable.fastertable.data.repository.LoginRepository
import com.fastertable.fastertable.ui.ui.login.restaurant.RestaurantLoginFragmentDirections

class HomeFragment : Fragment() {

    private lateinit var viewModel: HomeViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentHomeBinding.inflate(inflater)
        val application = requireNotNull(activity).application
        val loginRepository = LoginRepository(application)
        val navController = findNavController()

        val viewModelFactory = HomeViewModelFactory(loginRepository)
        viewModel = ViewModelProvider(
            this, viewModelFactory).get(HomeViewModel::class.java)

        viewModel.text.observe(viewLifecycleOwner, Observer {
            binding.textHome.text = it
        })

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
        return binding.root
    }
}