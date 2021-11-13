package com.fastertable.fastertable.ui.login.user

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.fastertable.fastertable.MainActivity
import com.fastertable.fastertable.R
import com.fastertable.fastertable.common.base.BaseFragment
import com.fastertable.fastertable.databinding.UserLoginFragmentBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UserLoginFragment: BaseFragment(R.layout.user_login_fragment) {
    private val viewModel: UserLoginViewModel by activityViewModels()
    private val binding: UserLoginFragmentBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel


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
    }


}