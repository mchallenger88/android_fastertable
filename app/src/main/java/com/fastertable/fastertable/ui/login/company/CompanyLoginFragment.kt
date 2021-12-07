package com.fastertable.fastertable.ui.login.company

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.fastertable.fastertable.R
import com.fastertable.fastertable.common.base.BaseFragment
import com.fastertable.fastertable.data.models.Location
import com.fastertable.fastertable.databinding.CompanyLoginFragmentBinding
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CompanyLoginFragment : BaseFragment(R.layout.company_login_fragment)  {
    private lateinit var viewModel: CompanyLoginViewModel
    private val binding: CompanyLoginFragmentBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(CompanyLoginViewModel::class.java)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        
        binding.txtCompanyLogin.addTextChangedListener( object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.setLoginText(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.txtCompanyPassword.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.setPasswordText(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        viewModel.locations.observe(viewLifecycleOwner, {
            it?.forEach { loc ->
                val btnView = MaterialButton(requireContext(), null, R.attr.materialButtonOutlinedStyle)
                btnView.text = loc.locationName
                btnView.textSize = 21F
                btnView.setOnClickListener { setRestaurant(loc) }

                binding.locationsLayout.addView(btnView)
            }

        })

        viewModel.showProgressBar.observe(viewLifecycleOwner, {
            if (it){
                binding.progressBarCompany.visibility = View.VISIBLE
            }else{
                binding.progressBarCompany.visibility = View.INVISIBLE
            }
        })

        viewModel.error.observe(viewLifecycleOwner, {
            if (it){
                binding.txtCompanyError.visibility = View.VISIBLE
            }else{
                binding.txtCompanyError.visibility = View.GONE
            }
        })
    }

    private fun setRestaurant(loc: Location){
        viewModel.setRestaurant(loc)
        this.findNavController().navigate(CompanyLoginFragmentDirections.actionCompanyLoginFragmentToRestaurantLoginFragment())
    }

}