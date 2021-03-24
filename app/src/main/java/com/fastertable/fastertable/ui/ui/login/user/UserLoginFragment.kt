package com.fastertable.fastertable.ui.ui.login.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.fastertable.fastertable.databinding.UserLoginFragmentBinding

class UserLoginFragment: Fragment() {
    private lateinit var viewModel: UserLoginViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View? {

        val binding = UserLoginFragmentBinding.inflate(inflater)
        val application = requireNotNull(activity).application
        val viewModelFactory = UserLoginViewModelFactory(application)
        viewModel = ViewModelProvider(
                this, viewModelFactory).get(UserLoginViewModel::class.java)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        binding.btnUserOne.setOnClickListener{numClick(1)}
        binding.btnUserTwo.setOnClickListener{numClick(2)}
        binding.btnUserThree.setOnClickListener{numClick(3)}
        binding.btnUserFour.setOnClickListener{numClick(4)}
        binding.btnUserFive.setOnClickListener{numClick(5)}
        binding.btnUserSix.setOnClickListener{numClick(6)}
        binding.btnUserSeven.setOnClickListener{numClick(7)}
        binding.btnUserEight.setOnClickListener{numClick(8)}
        binding.btnUserNine.setOnClickListener{numClick(9)}
        binding.btnUserZero.setOnClickListener{numClick(0)}

        binding.btnUserClear.setOnClickListener { pinClear() }
        binding.btnUserEnter.setOnClickListener { loginEnter() }

        viewModel.navigate.observe(viewLifecycleOwner, Observer { it ->
            if (it){
                this.findNavController().navigate(UserLoginFragmentDirections.actionUserLoginFragmentToOrderListFragment())
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
        UserLoginFragmentDirections.actionUserLoginFragmentToOrderListFragment()
//        viewModel.userLogin()
    }
}