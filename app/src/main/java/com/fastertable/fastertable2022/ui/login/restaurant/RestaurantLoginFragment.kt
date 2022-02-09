package com.fastertable.fastertable2022.ui.login.restaurant


import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.fastertable.fastertable2022.R
import com.fastertable.fastertable2022.common.base.BaseFragment
import com.fastertable.fastertable2022.databinding.RestaurantLoginFragmentBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RestaurantLoginFragment : BaseFragment(R.layout.restaurant_login_fragment) {
    private lateinit var viewModel: RestaurantLoginViewModel
    private val binding: RestaurantLoginFragmentBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(RestaurantLoginViewModel::class.java)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel


        viewModel.navigateToTerminals.observe(viewLifecycleOwner, {
            if (it){
                this.findNavController().navigate(RestaurantLoginFragmentDirections.actionRestaurantLoginFragmentToTerminalSelectFragment())
            }
        })

        viewModel.showProgressBar.observe(viewLifecycleOwner, {
            if (it){
                binding.progressBarRestLogin.visibility = View.VISIBLE
            }else{
                binding.progressBarRestLogin.visibility = View.INVISIBLE
            }
        })

        viewModel.navigateToUserLogin.observe(viewLifecycleOwner, {
            if (it){
                this.findNavController().navigate(RestaurantLoginFragmentDirections.actionRestaurantLoginFragmentToUserLoginFragment())
            }

        })

        viewModel.error.observe(viewLifecycleOwner, {
            if (it){
                binding.txtRestError.visibility = View.VISIBLE
            }else{
                binding.txtRestError.visibility = View.GONE
            }
        })
    }
}