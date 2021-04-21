package com.fastertable.fastertable.ui.order

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.TEXT_ALIGNMENT_CENTER
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
import com.fastertable.fastertable.adapters.IngredientHeaderAdapter
import com.fastertable.fastertable.adapters.IngredientsAdapter
import com.fastertable.fastertable.adapters.ModifierAdapter
import com.fastertable.fastertable.adapters.OrderItemAdapter
import com.fastertable.fastertable.common.base.BaseFragment
import com.fastertable.fastertable.data.models.Menu
import com.fastertable.fastertable.data.models.MenuCategory
import com.fastertable.fastertable.data.models.MenuItem
import com.fastertable.fastertable.databinding.OrderFragmentBinding
import com.fastertable.fastertable.ui.menus.MenusViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabItem
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import java.util.jar.Attributes

@AndroidEntryPoint
class OrderFragment : BaseFragment() {
    private lateinit var viewModel: OrderViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = OrderFragmentBinding.inflate(inflater)

        viewModel = ViewModelProvider(this
        ).get(OrderViewModel::class.java)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        viewModel.guestAdd.observe(viewLifecycleOwner, Observer { it ->
            if (it) {
                addGuestButtons(viewModel.order.value?.guests?.last()!!.id, binding)
                viewModel.setGuestAdd(false)
            }
        })


        viewModel.activeGuest.observe(viewLifecycleOwner, Observer { guestNumber ->
            binding.toolbarGuestSideBar.children.forEachIndexed { index, element ->
                if (element is Button) {
                    if (index == guestNumber) {
                        val color = ContextCompat.getColor(requireContext(), R.color.white)
                        element.setTextColor(ColorStateList.valueOf(color))
                        element.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_user_white, 0, 0)
                    } else {
                        val color = ContextCompat.getColor(requireContext(), R.color.offWhite)
                        element.setTextColor(ColorStateList.valueOf(color))
                        element.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_user_offwhite, 0, 0)
                    }
                }
            }
        })

        val modAdapter = ModifierAdapter(ModifierAdapter.ModifierListener { item ->
            viewModel.onModItemClicked(item)
        })

        val ingHeaderAdapter = IngredientHeaderAdapter()
        val ingAdapter = IngredientsAdapter(IngredientsAdapter.IngredientListener { item ->
            viewModel.onIngredientClicked(item)
        })
        val ingConcatAdapter = ConcatAdapter(ingHeaderAdapter, ingAdapter)


        viewModel.activeItem.observe(viewLifecycleOwner, Observer { item ->
            modAdapter.submitList(item?.modifiers)
            modAdapter.notifyDataSetChanged()
        })

        viewModel.changedIngredients.observe(viewLifecycleOwner, Observer {
            ingAdapter.submitList(viewModel.changedIngredients.value)
            ingAdapter.notifyDataSetChanged()
        })

        val concatAdapter = ConcatAdapter(modAdapter, ingConcatAdapter)


        binding.orderItems.adapter = OrderItemAdapter()
        binding.modRecyclerView.adapter = concatAdapter

        binding.menusTabBar.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val menu = viewModel.menus.value?.find{it -> it.name == tab?.text!!}
                if (menu != null){
                    menuSelect(menu, binding)
                    createCategoryButtons(menu, binding)
                    viewModel.setMenusNavigation(MenusNavigation.CATEGORIES)
                }

            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        viewModel.menusNavigation.observe(viewLifecycleOwner, Observer { it ->
            when (it) {
                MenusNavigation.CATEGORIES -> setCategoryVisibility(binding)
                MenusNavigation.MENU_ITEMS -> setMenuItemsVisibility(binding)
                MenusNavigation.MENU_ITEM -> setItemVisibility(binding)
                else -> setCategoryVisibility(binding)
            }

        })

            return binding.root
    }

    private fun setActiveGuest(int: Int){
        viewModel.setActiveGuest(int)
    }

    @SuppressLint("SetTextI18n")
    private fun createGuestButton(int: Int): Button{
        val btn = Button(requireContext(), null, android.R.style.TextAppearance_Material_Widget_Button_Borderless_Colored)
        btn.id = ViewCompat.generateViewId()
        val color = ContextCompat.getColor(requireContext(), android.R.color.transparent)
        val white = ContextCompat.getColor(requireContext(), R.color.white)
        btn.backgroundTintList = ColorStateList.valueOf(color)
        btn.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_user_white, 0, 0)
        btn.text = "Guest ${int.toString()}"
        btn.textSize = 16f
        btn.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
        btn.textAlignment = TEXT_ALIGNMENT_CENTER
        val layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL
        layoutParams.setMargins(0, 40, 0, 0)
        btn.setTextColor(ColorStateList.valueOf(white))
        btn.layoutParams = layoutParams
        btn.setOnClickListener{ setActiveGuest(int) }
        return btn
    }

    private fun addGuestButtons(int: Int, binding: OrderFragmentBinding){
        val btn = createGuestButton(int)
        binding.toolbarGuestSideBar.addView(btn)
        viewModel.pageLoaded.observe(viewLifecycleOwner, Observer { it ->
            if (it){
                createMenuButtons(viewModel.menus.value!!, binding)
                viewModel.setPageLoaded(false)
            }

        })

    }

    private fun createMenuButtons(menus: List<Menu>, binding: OrderFragmentBinding){
        menus.forEachIndexed{ int, menu ->
            val tab = binding.menusTabBar.newTab()

            tab.id = ViewCompat.generateViewId()
            tab.text = menu.name
            binding.menusTabBar.addTab(tab)

        }

        menus.forEachIndexed{ int, menu ->
            binding.menusTabBar.getTabAt(int)?.setText(menus[int].name)
        }
    }

    private fun setCategoryVisibility(binding: OrderFragmentBinding){
        binding.layoutMenuCategories.visibility = View.VISIBLE
        binding.scrollMenuItems.visibility = View.GONE
        binding.btnMenuBack.visibility = View.GONE
        binding.layoutMenuItem.visibility = View.GONE
        binding.txtMenuCategory.visibility = View.GONE
        binding.txtMenuItemItemName.visibility = View.GONE
        val offWhite = ContextCompat.getColor(requireContext(), R.color.offWhite_background)
        binding.layoutMenus.backgroundTintList = ColorStateList.valueOf(offWhite)
        binding.btnMenuBack.backgroundTintList = ColorStateList.valueOf(offWhite)
    }

    private fun setMenuItemsVisibility(binding: OrderFragmentBinding){
        binding.layoutMenuCategories.visibility = View.GONE
        binding.scrollMenuItems.visibility = View.VISIBLE
        binding.btnMenuBack.visibility = View.VISIBLE
        binding.layoutMenuItem.visibility = View.GONE
        binding.txtMenuCategory.visibility = View.VISIBLE
        binding.txtMenuItemItemName.visibility = View.GONE
        val offWhite = ContextCompat.getColor(requireContext(), R.color.offWhite_background)
        binding.layoutMenus.backgroundTintList = ColorStateList.valueOf(offWhite)
        binding.btnMenuBack.backgroundTintList = ColorStateList.valueOf(offWhite)
    }

    private fun setItemVisibility(binding: OrderFragmentBinding){
        binding.layoutMenuCategories.visibility = View.GONE
        binding.scrollMenuItems.visibility = View.GONE
        binding.btnMenuBack.visibility = View.VISIBLE
        binding.txtMenuCategory.visibility = View.GONE
        binding.layoutMenuItem.visibility = View.VISIBLE
        binding.txtMenuItemItemName.visibility = View.VISIBLE
        val white = ContextCompat.getColor(requireContext(), R.color.white)
        binding.layoutMenus.backgroundTintList = ColorStateList.valueOf(white)
        binding.btnMenuBack.backgroundTintList = ColorStateList.valueOf(white)
    }

    private fun menuSelect(menu: Menu, binding: OrderFragmentBinding){
        viewModel.setActiveMenu(menu)

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
            val color = ContextCompat.getColor(requireContext(), R.color.primaryTextColor)
            val white = ContextCompat.getColor(requireContext(), R.color.white)
            val accent = ContextCompat.getColor(requireContext(), R.color.secondaryColor)
            btnView.backgroundTintList = ColorStateList.valueOf(white)
            btnView.setTextColor(ColorStateList.valueOf(color))

            btnView.text = category.category
            btnView.textSize = 20F

            val params = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            params.setMargins(5, 2, 5, 2)
            btnView.setPadding(25, 20, 25, 20)
            btnView.layoutParams = params
            btnView.width = 250
            btnView.height = 200
            btnView.setOnClickListener { setCategory(category, binding) }

            binding.layoutMenuCategories.addView(btnView)

            flow.addView(btnView)

        }

        binding.layoutMenuCategories.addView(flow)
    }

    private fun setCategory(cat: MenuCategory, binding: OrderFragmentBinding){
        binding.layoutMenuItems.removeAllViews()
        viewModel.setActiveCategory(cat)
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
            val color = ContextCompat.getColor(requireContext(), R.color.primaryTextColor)
            val white = ContextCompat.getColor(requireContext(), R.color.white)
            val accent = ContextCompat.getColor(requireContext(), R.color.secondaryColor)
            btnView.backgroundTintList = ColorStateList.valueOf(white)
            btnView.setTextColor(ColorStateList.valueOf(color))
            btnView.text = menuItem.itemName
            btnView.textSize = 20F

            val params = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            params.setMargins(5, 2, 5, 2)
            btnView.setPadding(25, 20, 25, 20)
            btnView.layoutParams = params
            btnView.width = 450
            btnView.height = 180
            btnView.setOnClickListener { setMenuItem(menuItem, binding) }

            binding.layoutMenuItems.addView(btnView)

            flow.addView(btnView)
        }
        viewModel.setMenusNavigation(MenusNavigation.MENU_ITEMS)
        binding.layoutMenuItems.addView(flow)

    }

    private fun setMenuItem(item: MenuItem, binding: OrderFragmentBinding){
        viewModel.setMenusNavigation(MenusNavigation.MENU_ITEM)
        viewModel.setActiveItem(item)

    }


}