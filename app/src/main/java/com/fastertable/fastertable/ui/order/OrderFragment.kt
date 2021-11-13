package com.fastertable.fastertable.ui.order

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.LinearLayout
import androidx.constraintlayout.helper.widget.Flow
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ConcatAdapter
import by.kirich1409.viewbindingdelegate.viewBinding
import com.fastertable.fastertable.R
import com.fastertable.fastertable.adapters.GuestSideBarAdapter
import com.fastertable.fastertable.adapters.IngredientsAdapter
import com.fastertable.fastertable.adapters.ModifierAdapter
import com.fastertable.fastertable.adapters.OrderItemAdapter
import com.fastertable.fastertable.common.base.BaseFragment
import com.fastertable.fastertable.data.models.Menu
import com.fastertable.fastertable.data.models.MenuCategory
import com.fastertable.fastertable.data.models.MenuItem
import com.fastertable.fastertable.data.models.newGuest
import com.fastertable.fastertable.databinding.OrderFragmentBinding
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OrderFragment : BaseFragment(R.layout.order_fragment) {
    private val viewModel: OrderViewModel by activityViewModels()
    private val binding: OrderFragmentBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        viewModel.initOrder()
        createAndSetAdapters(binding)
        createObservers(binding)

        binding.menusTabBar.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val menu = viewModel.menus.value?.find{it -> it.name == tab?.text!!}
                if (menu != null){
                    createCategoryButtons(menu, binding)
                    viewModel.setMenusNavigation(MenusNavigation.CATEGORIES)
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun createObservers(binding: OrderFragmentBinding){
        viewModel.pageLoaded.observe(viewLifecycleOwner, {
            if (it){
                createMenuButtons(viewModel.menus.value!!, binding)
                viewModel.setPageLoaded(false)
            }
        })

        viewModel.ticketsPrinted.observe(viewLifecycleOwner, {
            if (it != null){
                viewModel.updateOrderStatus(viewModel.activeOrder.value!!)
            }
        })
    }

    private fun createMenuButtons(menus: List<Menu>, binding: OrderFragmentBinding){
        for (menu in menus){
            val tab = binding.menusTabBar.newTab()

            tab.id = ViewCompat.generateViewId()
            tab.text = menu.name
            binding.menusTabBar.addTab(tab)
        }

        menus.forEachIndexed{ int, menu ->
            binding.menusTabBar.getTabAt(int)?.text = menus[int].name
        }
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
            btnView.setOnClickListener { setMenuItem(menuItem) }

            binding.layoutMenuItems.addView(btnView)

            flow.addView(btnView)
        }
        viewModel.setMenusNavigation(MenusNavigation.MENU_ITEMS)
        binding.layoutMenuItems.addView(flow)

    }

    private fun setMenuItem(item: MenuItem){
        viewModel.setMenusNavigation(MenusNavigation.MENU_ITEM)
        viewModel.setActiveItem(item)

    }

    private fun createAndSetAdapters(binding: OrderFragmentBinding){
        val modAdapter = ModifierAdapter(ModifierAdapter.ModifierListener { item ->
            viewModel.onModItemClicked(item)
        })

        val ingAdapter = IngredientsAdapter(IngredientsAdapter.IngredientListener { item ->
            viewModel.onIngredientClicked(item)
        })

        val guestAdapter = GuestSideBarAdapter(GuestSideBarAdapter.GuestSideBarListener {item ->
            viewModel.setActiveGuest(item.guest)
        })

        val concatAdapter = ConcatAdapter(modAdapter, ingAdapter)
        val orderItemsAdapter = OrderItemAdapter(OrderItemAdapter.OrderItemListener { item ->
            viewModel.orderItemClicked(item)
        })

        binding.orderItems.adapter = orderItemsAdapter
        binding.modRecyclerView.adapter = concatAdapter

        binding.guestRecycler.adapter = guestAdapter

        viewModel.activeItem.observe(viewLifecycleOwner, { item ->
            modAdapter.submitList(item?.modifiers)
            modAdapter.notifyDataSetChanged()
        })

        viewModel.changedIngredients.observe(viewLifecycleOwner, {
            ingAdapter.submitList(viewModel.changedIngredients.value)
            ingAdapter.notifyDataSetChanged()
        })

        viewModel.activeOrder.observe(viewLifecycleOwner, { item ->
            val list = mutableListOf<newGuest>()
            if (item != null){
                for (i in 1..item.guestCount){
                    val newGuest = newGuest(
                        guest = i,
                        activeGuest = item.activeGuest
                    )
                    list.add(newGuest)
                }
                guestAdapter.submitList(list)
                guestAdapter.notifyDataSetChanged()

                val list1 = item.orderItems?.filter { it.guestId == item.activeGuest }
                if (list1 != null){
                    orderItemsAdapter.submitList(list1)
                    orderItemsAdapter.notifyDataSetChanged()
                }
            }

        })
    }
}