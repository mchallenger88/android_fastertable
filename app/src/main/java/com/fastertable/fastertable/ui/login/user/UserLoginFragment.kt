package com.fastertable.fastertable.ui.login.user

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.fastertable.fastertable.MainActivity
import com.fastertable.fastertable.common.base.BaseFragment
import com.fastertable.fastertable.databinding.UserLoginFragmentBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UserLoginFragment: BaseFragment() {
    private val viewModel: UserLoginViewModel by activityViewModels()
    private lateinit var binding: UserLoginFragmentBinding

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View {

        binding = UserLoginFragmentBinding.inflate(inflater)

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

        viewModel.navigate.observe(viewLifecycleOwner, {
            if (it){
                val intent = Intent(this.context, MainActivity::class.java)
                intent.flags = FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
        })

        viewModel.showProgressBar.observe(viewLifecycleOwner, {
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

    override fun onDestroyView() {
        super.onDestroyView()
        binding.unbind()
    }
}