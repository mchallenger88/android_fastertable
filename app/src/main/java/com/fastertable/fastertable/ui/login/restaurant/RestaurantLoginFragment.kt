package com.fastertable.fastertable.ui.login.restaurant


import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.fastertable.fastertable.R
import com.fastertable.fastertable.common.base.BaseFragment
import com.fastertable.fastertable.databinding.RestaurantLoginFragmentBinding
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

        binding.btnOne.setOnClickListener{numClick(1)}
        binding.btnTwo.setOnClickListener{numClick(2)}
        binding.btnThree.setOnClickListener{numClick(3)}
        binding.btnFour.setOnClickListener{numClick(4)}
        binding.btnFive.setOnClickListener{numClick(5)}
        binding.btnSix.setOnClickListener{numClick(6)}
        binding.btnSeven.setOnClickListener{numClick(7)}
        binding.btnEight.setOnClickListener{numClick(8)}
        binding.btnNine.setOnClickListener{numClick(9)}
        binding.btnZero.setOnClickListener{numClick(0)}

        binding.btnClear.setOnClickListener { pinClear() }
        binding.btnEnter.setOnClickListener { loginEnter() }

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

    private fun numClick(num: Int){
        viewModel.concatPin(num)
    }

    private fun pinClear(){
        viewModel.pinClear()
    }

    private fun loginEnter(){
        viewModel.restLogin()
    }
}