package com.fastertable.fastertable.ui.login.company

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.fastertable.fastertable.R
import com.fastertable.fastertable.api.CompanyLoginUseCase
import com.fastertable.fastertable.data.models.Location
import com.fastertable.fastertable.data.repository.LoginRepository
import com.fastertable.fastertable.databinding.CompanyLoginFragmentBinding
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CompanyLoginFragment : Fragment()  {

    private lateinit var viewModel: CompanyLoginViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View? {

        val binding = CompanyLoginFragmentBinding.inflate(inflater)

        viewModel = ViewModelProvider(this).get(CompanyLoginViewModel::class.java)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        viewModel.locations.observe(viewLifecycleOwner, Observer { l ->
            l?.forEach { loc ->
                val btnView = MaterialButton(requireContext(), null, R.attr.materialButtonOutlinedStyle)
                btnView.text = loc.locationName
                btnView.textSize = 21F
                btnView.setOnClickListener { setRestaurant(loc) }

                binding.locationsLayout.addView(btnView)
            }

        })

        viewModel.showProgressBar.observe(viewLifecycleOwner, Observer { it ->
            if (it){
                binding.progressBarCompany.visibility = View.VISIBLE
            }else{
                binding.progressBarCompany.visibility = View.INVISIBLE
            }
        })

        viewModel.error.observe(viewLifecycleOwner, Observer { it ->
            if (it){
                binding.txtCompanyError.visibility = View.VISIBLE
            }else{
                binding.txtCompanyError.visibility = View.GONE
            }
        })


    return binding.root
}

    private fun setRestaurant(loc: Location){
        viewModel.setRestaurant(loc)
        this.findNavController().navigate(CompanyLoginFragmentDirections.actionCompanyLoginFragmentToRestaurantLoginFragment())
    }

}