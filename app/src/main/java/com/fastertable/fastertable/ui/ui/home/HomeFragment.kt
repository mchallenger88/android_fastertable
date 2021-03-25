package com.fastertable.fastertable.ui.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.fastertable.fastertable.R
import com.fastertable.fastertable.data.repository.LoginRepository
import com.fastertable.fastertable.ui.ui.login.restaurant.RestaurantLoginFragmentDirections

class HomeFragment : Fragment() {

    private lateinit var viewModel: HomeViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val application = requireNotNull(activity).application
        val loginRepository = LoginRepository(application)
        val navController = findNavController()

        val viewModelFactory = HomeViewModelFactory(application, loginRepository)
        viewModel = ViewModelProvider(
            this, viewModelFactory).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        val textView: TextView = root.findViewById(R.id.text_home)
        viewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
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
                val action = HomeFragmentDirections.actionNavHomeToTerminalSelectFragment(viewModel.settings.value!!)
                navController.navigate(action)
            }
        })
        return root
    }
}