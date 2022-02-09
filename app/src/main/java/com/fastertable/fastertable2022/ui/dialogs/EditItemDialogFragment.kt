package com.fastertable.fastertable2022.ui.dialogs

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.RadioButton
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ConcatAdapter
import by.kirich1409.viewbindingdelegate.viewBinding
import com.fastertable.fastertable2022.R
import com.fastertable.fastertable2022.adapters.IngredientsAdapter
import com.fastertable.fastertable2022.adapters.ModifierAdapter
import com.fastertable.fastertable2022.data.models.*
import com.fastertable.fastertable2022.databinding.DialogEditItemBinding
import com.fastertable.fastertable2022.ui.order.OrderViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditItemDialogFragment: BaseDialog(R.layout.dialog_edit_item) {
    private val orderViewModel: OrderViewModel by activityViewModels()
    private val viewModel: EditItemDialogViewModel by activityViewModels()
    private val binding: DialogEditItemBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        var mi: MenuItem
        orderViewModel.editItem.value?.let {
            mi = it.clone()
            viewModel.setMenuItem(mi)
        }

        orderViewModel.editOrderItem.value?.let {
            viewModel.setOrderItem(it)
        }

        viewModel.saveChanges.observe(viewLifecycleOwner, {
            it?.let {
                it.note = binding.txtEditOrderNote.text.toString()
                orderViewModel.saveEditedItem(it)
                viewModel.setSaveChanges(null)
            }
        })

        createAdapters()

        binding.btnEditOrderNote.setOnClickListener {
            if (viewModel.showNotes.value == true){
                viewModel.setShowNotes(false)
            }else{
                viewModel.setShowNotes(true)
            }
        }

        binding.btnEditSaveItem.setOnClickListener {
            viewModel.saveChanges()
            dismiss()
        }

        binding.btnEditCancelItem.setOnClickListener {
            dismiss()
        }

        binding.btnEditMinusQuantity.setOnClickListener {
            viewModel.decreaseQuantity()
        }

        binding.btnEditAddQuantity.setOnClickListener {
            viewModel.increaseQuantity()
        }
    }

    private fun createAdapters() {
        val modAdapter = ModifierAdapter(ModifierAdapter.ModifierListener { item ->
            viewModel.onModItemClicked(item)
        })

        val ingAdapter = IngredientsAdapter(IngredientsAdapter.IngredientListener { item ->
            viewModel.onIngredientClicked(item)
        })

        val concatAdapter = ConcatAdapter(modAdapter, ingAdapter)
        binding.editModRecyclerView.adapter = concatAdapter

        viewModel.menuItem.observe(viewLifecycleOwner, { item ->
            if (item != null){
                modAdapter.submitList(item.modifiers)
                modAdapter.notifyDataSetChanged()

                ingAdapter.submitList(item.ingredients)
                ingAdapter.notifyDataSetChanged()

                populatePriceGroup(item)
            }
        })

    }



    override fun onStart() {
        super.onStart()
        val width = 1200
        val height = 1200
        dialog!!.window?.setLayout(width, height)
    }


    companion object {
        fun newInstance(): EditItemDialogFragment {
            return EditItemDialogFragment()
        }

        const val TAG = "EditItemDialogFragment"
    }

    @SuppressLint("SetTextI18n")
    fun populatePriceGroup(menuItem: MenuItem) {
        binding.itemPriceRadioGroupEdit.removeAllViews()
        for (price in menuItem.prices){
            val button = RadioButton(activity)
            val typeface = ResourcesCompat.getFont(button.context, R.font.open_sans_semibold)
            button.typeface = typeface
            button.textSize = 24f
            val workingPrice = price.price.plus(price.modifiedPrice).times(price.quantity)
            val x = button.context.getString(R.string.item_price, "%.${2}f".format(workingPrice))
            button.text = "${price.size}: $x"
            button.isChecked = price.isSelected
            button.setOnClickListener { viewModel.changeSelectedPrice(price) }
            binding.itemPriceRadioGroupEdit.addView(button)
        }
    }

    private fun hideKeyboardFrom(context: Context, view: View) {
        val imm: InputMethodManager =
            context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}
