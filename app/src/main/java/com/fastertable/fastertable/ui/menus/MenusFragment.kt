package com.fastertable.fastertable.ui.menus

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.fastertable.fastertable.data.Menu
import com.fastertable.fastertable.data.repository.MenusRepository
import com.fastertable.fastertable.data.repository.OrderRepository
import com.fastertable.fastertable.databinding.MenusFragmentBinding

class MenusFragment : Fragment(){
    private lateinit var viewModel: MenusViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = MenusFragmentBinding.inflate(inflater)
        val application = requireNotNull(activity).application
        val menusRepository = MenusRepository(application)
        val orderRepository = OrderRepository(application)
        val viewModelFactory = MenusViewModelFactory(menusRepository, orderRepository)
        viewModel = ViewModelProvider(
            this, viewModelFactory).get(MenusViewModel::class.java)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

//        viewModel.pageLoaded.observe(viewLifecycleOwner, Observer { it ->
//            if (it){
//                println("in the observer")
//                println(it)
//                createMenuButtons(viewModel.menus.value, binding)
//            }
//
//        })



            return binding.root
    }

//    private fun createMenuButtons(menus: List<Menu>?, binding: MenusFragmentBinding){
//        menus?.forEach{ menu ->
//            val btnView = Button(activity)
//            btnView.text = menu.name
//            btnView.textSize = 18F
//            btnView.setPadding(25, 20, 25, 20)
//            println("In the function")
//            binding.toolbarMenus.addView(btnView)
//        }
//    }
}