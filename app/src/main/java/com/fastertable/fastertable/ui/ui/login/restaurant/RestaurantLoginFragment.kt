package com.fastertable.fastertable.ui.ui.login.restaurant


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.fastertable.fastertable.data.Settings
import com.fastertable.fastertable.data.repository.LoginRepository
import com.fastertable.fastertable.databinding.RestaurantLoginFragmentBinding


class RestaurantLoginFragment : Fragment() {
    private lateinit var viewModel: RestaurantLoginViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View {
        val binding = RestaurantLoginFragmentBinding.inflate(inflater)
        val application = requireNotNull(activity).application
        val restaurant = RestaurantLoginFragmentArgs.fromBundle(requireArguments()).location
        val loginRepository = LoginRepository(application)
        val viewModelFactory = RestaurantLoginViewModelFactory(application, restaurant, loginRepository)
        viewModel = ViewModelProvider(
                this, viewModelFactory).get(RestaurantLoginViewModel::class.java)

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

        viewModel.navigateToTerminals.observe(viewLifecycleOwner, Observer { it ->
            if (it){
                if (viewModel.settings.value != null){
                    val settings: Settings = viewModel.settings.value!!
                    this.findNavController().navigate(RestaurantLoginFragmentDirections.actionRestaurantLoginFragmentToTerminalSelectFragment(settings))
                }
            }
        })

        viewModel.showProgressBar.observe(viewLifecycleOwner, Observer { it ->
            if (it){
                binding.progressBarRestLogin.visibility = View.VISIBLE
            }else{
                binding.progressBarRestLogin.visibility = View.INVISIBLE
            }
        })

        viewModel.navigateToUserLogin.observe(viewLifecycleOwner, Observer { it ->
            if (it){
                this.findNavController().navigate(RestaurantLoginFragmentDirections.actionRestaurantLoginFragmentToUserLoginFragment())
            }

        })

        viewModel.error.observe(viewLifecycleOwner, Observer { it ->
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


}