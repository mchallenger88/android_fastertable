package com.fastertable.fastertable.ui.order

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.fastertable.fastertable.R
import com.fastertable.fastertable.databinding.OrderFragmentBinding
import com.fastertable.fastertable.utils.GlobalUtils

class OrderFragment : Fragment() {
    private lateinit var viewModel: OrderViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = OrderFragmentBinding.inflate(inflater)
        return binding.root
    }
}