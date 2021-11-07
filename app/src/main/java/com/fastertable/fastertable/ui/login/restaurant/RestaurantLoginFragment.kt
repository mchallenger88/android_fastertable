package com.fastertable.fastertable.ui.login.restaurant


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.fastertable.fastertable.common.base.BaseFragment
import com.fastertable.fastertable.databinding.RestaurantLoginFragmentBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RestaurantLoginFragment : BaseFragment() {
    private lateinit var viewModel: RestaurantLoginViewModel
    private lateinit var binding: RestaurantLoginFragmentBinding

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View {
        binding = RestaurantLoginFragmentBinding.inflate(inflater)
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
        return binding.root
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

    override fun onDestroyView() {
        super.onDestroyView()
        binding.unbind()
    }
}