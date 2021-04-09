package com.fastertable.fastertable.ui.order

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.constraintlayout.helper.widget.Flow
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ConcatAdapter
import com.fastertable.fastertable.R
import com.fastertable.fastertable.adapters.IngredientsAdapter
import com.fastertable.fastertable.adapters.ModifierAdapter
import com.fastertable.fastertable.adapters.OrderItemAdapter
import com.fastertable.fastertable.data.Menu
import com.fastertable.fastertable.data.MenuCategory
import com.fastertable.fastertable.data.MenuItem
import com.fastertable.fastertable.data.repository.LoginRepository
import com.fastertable.fastertable.data.repository.MenusRepository
import com.fastertable.fastertable.data.repository.OrderRepository
import com.fastertable.fastertable.databinding.OrderFragmentBinding
import com.fastertable.fastertable.ui.menus.MenusViewModel
import com.fastertable.fastertable.ui.menus.MenusViewModelFactory
import com.google.android.material.floatingactionbutton.FloatingActionButton


class OrderFragment : Fragment() {
    private lateinit var viewModel: OrderViewModel
    private lateinit var menusViewModel: MenusViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = OrderFragmentBinding.inflate(inflater)
        val application = requireNotNull(activity).application
        val loginRepository = LoginRepository(application)
        val orderRepository = OrderRepository(application)
        val menusRepository = MenusRepository(application)
        val viewModelFactory = OrderViewModelFactory(loginRepository, orderRepository)
        viewModel = ViewModelProvider(
            this, viewModelFactory
        ).get(OrderViewModel::class.java)

        val viewModelFactoryMenus = MenusViewModelFactory(menusRepository, orderRepository)
        menusViewModel = ViewModelProvider(
            this, viewModelFactoryMenus
        ).get(MenusViewModel::class.java)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        binding.menusViewModel = menusViewModel

        viewModel.guestAdd.observe(viewLifecycleOwner, Observer { it ->
            if (it) {
                addGuestButtons(viewModel.order.value?.guests?.last()!!.id, binding)
                viewModel.setGuestAdd(false)
            }
        })


        viewModel.activeGuest.observe(viewLifecycleOwner, Observer { guestNumber ->
            binding.toolbarGuestSideBar.children.forEachIndexed { index, element ->
                if (element is FloatingActionButton) {
                    if (index == guestNumber) {
                        val color = ContextCompat.getColor(requireContext(), R.color.secondaryColor)
                        element.backgroundTintList = ColorStateList.valueOf(color)
                    } else {
                        val color = ContextCompat.getColor(requireContext(), R.color.primaryColor)
                        element.backgroundTintList = ColorStateList.valueOf(color)
                    }
                }
            }
        })

        val modAdapter = ModifierAdapter(ModifierAdapter.ModifierListener { item ->
            viewModel.onModItemClicked(item)
        })

        val ingAdapter = IngredientsAdapter(IngredientsAdapter.IngredientListener { item ->
            viewModel.onIngredientClicked(item)
        })


        menusViewModel.activeItem.observe(viewLifecycleOwner, Observer { item ->
            modAdapter.submitList(item.modifiers)
            ingAdapter.submitList(menusViewModel.createIngredientList(item))
        })

        val concatAdapter = ConcatAdapter(modAdapter, ingAdapter)


        binding.orderItems.adapter = OrderItemAdapter()
        binding.modRecyclerView.adapter = concatAdapter
            return binding.root
    }

    private fun setActiveGuest(int: Int){
        viewModel.setActiveGuest(int)
    }

    private fun addGuestButtons(int: Int, binding: OrderFragmentBinding){
        val fab = FloatingActionButton(requireActivity())
        fab.id = ViewCompat.generateViewId()
        fab.size = FloatingActionButton.SIZE_MINI
        val color = ContextCompat.getColor(requireContext(), R.color.primaryColor)
        fab.backgroundTintList = ColorStateList.valueOf(color)
        fab.elevation = 4f

        when (int){
            1 -> fab.setImageResource(R.drawable.guest_1_foreground)
            2 -> fab.setImageResource(R.drawable.guest_2_foreground)
            3 -> fab.setImageResource(R.drawable.guest_3_foreground)
            4 -> fab.setImageResource(R.drawable.guest_4_foreground)
            5 -> fab.setImageResource(R.drawable.guest_5_foreground)
            6 -> fab.setImageResource(R.drawable.guest_6_foreground)
            7 -> fab.setImageResource(R.drawable.guest_7_foreground)
            8 -> fab.setImageResource(R.drawable.guest_8_foreground)
            9 -> fab.setImageResource(R.drawable.guest_9_foreground)
            10 -> fab.setImageResource(R.drawable.guest_10_foreground)
            11 -> fab.setImageResource(R.drawable.guest_11_foreground)
            12 -> fab.setImageResource(R.drawable.guest_12_foreground)
            13 -> fab.setImageResource(R.drawable.guest_13_foreground)
            14 -> fab.setImageResource(R.drawable.guest_14_foreground)
            15 -> fab.setImageResource(R.drawable.guest_15_foreground)
            16 -> fab.setImageResource(R.drawable.guest_16_foreground)
            17 -> fab.setImageResource(R.drawable.guest_17_foreground)
            18 -> fab.setImageResource(R.drawable.guest_18_foreground)
            19 -> fab.setImageResource(R.drawable.guest_19_foreground)
            20 -> fab.setImageResource(R.drawable.guest_20_foreground)
            else -> fab.setImageResource(R.drawable.guest_1_foreground)
        }

        fab.isFocusable = true
        val layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL
        fab.layoutParams = layoutParams

        fab.setOnClickListener{ setActiveGuest(int) }

        menusViewModel.pageLoaded.observe(viewLifecycleOwner, Observer { it ->
            if (it){
                createMenuButtons(menusViewModel.menus.value!!, binding)
                menusViewModel.setPageLoaded(false)
            }

//            menus.forEach { menu ->
//                createCategoryButtons(menu, binding)
//            }

        })

        menusViewModel.activeMenu.observe(viewLifecycleOwner, Observer { menu ->
            binding.menusToolbar.children.forEach { child ->
                val color = ContextCompat.getColor(requireContext(), R.color.primaryColor)
                val accent = ContextCompat.getColor(requireContext(), R.color.secondaryColor)
                if (child is Button) {
                    if (child.text == menu.name) {
                        child.backgroundTintList = ColorStateList.valueOf(accent)
                        createCategoryButtons(menu, binding)
                    } else {
                        child.backgroundTintList = ColorStateList.valueOf(color)
                    }
                }
            }
        })

        binding.toolbarGuestSideBar.addView(fab)
    }

    private fun createMenuButtons(menus: List<Menu>, binding: OrderFragmentBinding){
        menus.forEachIndexed{ int, menu ->
            val btnView = Button(activity)
            btnView.id = ViewCompat.generateViewId()
            val color = ContextCompat.getColor(requireContext(), R.color.primaryColor)
            val white = ContextCompat.getColor(requireContext(), R.color.white)
            val accent = ContextCompat.getColor(requireContext(), R.color.secondaryColor)
            btnView.backgroundTintList = ColorStateList.valueOf(color)
            btnView.setTextColor(ColorStateList.valueOf(white))
            btnView.text = menu.name
            btnView.textSize = 18F

            val params = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            params.setMargins(5, 2, 5, 2)
            btnView.setPadding(25, 20, 25, 20)
            btnView.layoutParams = params
            btnView.setOnClickListener { menuSelect(menu, binding) }

//            if (int == 0){
//                btnView.backgroundTintList = ColorStateList.valueOf(accent)
//                createCategoryButtons(menu, binding)
//            }

            binding.menusToolbar.addView(btnView)
        }
    }

    private fun menuSelect(menu: Menu, binding: OrderFragmentBinding){
        menusViewModel.setActiveMenu(menu)
        binding.layoutMenuCategories.visibility = View.VISIBLE
        binding.layoutMenuItems.visibility = View.GONE
        binding.btnMenuBack.visibility = View.VISIBLE
        binding.layoutMenuItem.visibility = View.GONE
        binding.btnMinusQuantity.visibility = View.GONE
        binding.btnAddQuantity.visibility = View.GONE
        binding.txtItemQuantity.visibility = View.GONE
    }

    private fun createCategoryButtons(menu: Menu, binding: OrderFragmentBinding){
        binding.btnMenuBack.visibility = View.VISIBLE

        binding.layoutMenuCategories.removeAllViews()
        //Create the flow layout
        val flow = Flow(context)
        flow.id = ViewCompat.generateViewId()
        val flowParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )
        flow.layoutParams = flowParams
        flow.setOrientation(Flow.HORIZONTAL)
        flow.setWrapMode(Flow.WRAP_ALIGNED)
        flow.setVerticalGap(2)
        flow.setVerticalStyle(Flow.CHAIN_SPREAD_INSIDE)
        flow.setHorizontalStyle(Flow.CHAIN_PACKED)
//        binding.layoutMenuCategories.removeView(flow)

        //Add categories as buttons
        menu.categories.forEachIndexed{int, category ->
            val btnView = Button(activity)
            btnView.id = ViewCompat.generateViewId()
            val color = ContextCompat.getColor(requireContext(), R.color.primaryColor)
            val white = ContextCompat.getColor(requireContext(), R.color.white)
            val accent = ContextCompat.getColor(requireContext(), R.color.secondaryColor)
            btnView.backgroundTintList = ColorStateList.valueOf(color)
            btnView.setTextColor(ColorStateList.valueOf(white))
            btnView.text = category.category
            btnView.textSize = 18F

            val params = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            params.setMargins(5, 2, 5, 2)
            btnView.setPadding(25, 20, 25, 20)
            btnView.layoutParams = params
            btnView.width = 225
            btnView.height = 150
            btnView.setOnClickListener { setCategory(category, binding) }

            binding.layoutMenuCategories.addView(btnView)

            flow.addView(btnView)

        }

        binding.layoutMenuCategories.addView(flow)
    }

    private fun setCategory(cat: MenuCategory, binding: OrderFragmentBinding){
        binding.btnMenuBack.visibility = View.VISIBLE
        binding.layoutMenuCategories.visibility = View.GONE
        binding.layoutMenuItems.visibility = View.VISIBLE
        binding.layoutMenuItems.removeAllViews()

        val flow = Flow(context)
        flow.id = ViewCompat.generateViewId()
        val flowParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )
        flow.layoutParams = flowParams
        flow.setOrientation(Flow.HORIZONTAL)
        flow.setWrapMode(Flow.WRAP_ALIGNED)
        flow.setVerticalGap(2)
        flow.setVerticalStyle(Flow.CHAIN_SPREAD_INSIDE)
        flow.setHorizontalStyle(Flow.CHAIN_PACKED)

        cat.menuItems.sortBy { it.itemName }

        cat.menuItems.forEachIndexed { index, menuItem ->
            val btnView = Button(activity)
            btnView.id = ViewCompat.generateViewId()
            val color = ContextCompat.getColor(requireContext(), R.color.primaryColor)
            val white = ContextCompat.getColor(requireContext(), R.color.white)
            val accent = ContextCompat.getColor(requireContext(), R.color.secondaryColor)
            btnView.backgroundTintList = ColorStateList.valueOf(color)
            btnView.setTextColor(ColorStateList.valueOf(white))
            btnView.text = menuItem.itemName
            btnView.textSize = 18F

            val params = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            params.setMargins(5, 2, 5, 2)
            btnView.setPadding(25, 20, 25, 20)
            btnView.layoutParams = params
            btnView.width = 225
            btnView.height = 150
            btnView.setOnClickListener { setMenuItem(menuItem, binding) }

            binding.layoutMenuItems.addView(btnView)

            flow.addView(btnView)
        }

        binding.layoutMenuItems.addView(flow)
    }

    private fun setMenuItem(item: MenuItem, binding: OrderFragmentBinding){
        binding.btnMenuBack.visibility = View.GONE
        binding.layoutMenuItems.visibility = View.GONE
        binding.layoutMenuItem.visibility = View.VISIBLE
        binding.btnMinusQuantity.visibility = View.VISIBLE
        binding.btnAddQuantity.visibility = View.VISIBLE
        binding.txtItemQuantity.visibility = View.VISIBLE

        menusViewModel.setActiveItem(item)
    }



//    private fun chipMenus(menus: List<Menu>, binding: OrderFragmentBinding){
//        menus.forEachIndexed{ int, menu ->
//            val chipView = Chip(activity)
//            chipView.setChipBackgroundColorResource(R.color.primaryColor)
//            chipView.setTextAppearance(R.style.ChipTextAppearance)
//            chipView.text = menu.name
//
//            if (int == 0){
//                chipView.setChipBackgroundColorResource(R.color.secondaryColor)
//            }
//
//            binding.menusChipGroup.addView(chipView)
//        }
//    }



}