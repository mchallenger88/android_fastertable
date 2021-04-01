package com.fastertable.fastertable.ui.login.user

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.fastertable.fastertable.MainActivity
import com.fastertable.fastertable.data.repository.LoginRepository
import com.fastertable.fastertable.data.repository.OrderRepository
import com.fastertable.fastertable.databinding.UserLoginFragmentBinding

class UserLoginFragment: Fragment() {
    private lateinit var viewModel: UserLoginViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View {

        val binding = UserLoginFragmentBinding.inflate(inflater)
        val application = requireNotNull(activity).application
        val loginRepository = LoginRepository(application)
        val orderRepository = OrderRepository(application)
        val viewModelFactory = UserLoginViewModelFactory(loginRepository, orderRepository)
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
                 val intent = Intent(this.context, MainActivity::class.java)
                intent.setFlags(FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        })

        viewModel.showProgressBar.observe(viewLifecycleOwner, Observer { it ->
            if (it){
                binding.progressBarUserLogin.visibility = View.VISIBLE
            }else{
                binding.progressBarUserLogin.visibility = View.INVISIBLE
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
        viewModel.userLogin()
    }
}