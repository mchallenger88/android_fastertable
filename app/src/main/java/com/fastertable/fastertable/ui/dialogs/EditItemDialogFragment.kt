package com.fastertable.fastertable.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ConcatAdapter
import com.fastertable.fastertable.adapters.IngredientsAdapter
import com.fastertable.fastertable.adapters.ModifierAdapter
import com.fastertable.fastertable.databinding.DialogEditItemBinding
import com.fastertable.fastertable.ui.order.OrderViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditItemDialogFragment: BaseDialog() {
    private val viewModel: OrderViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = DialogEditItemBinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        createAdapters(binding)

        binding.btnEditSaveItem.setOnClickListener {
            viewModel.saveModifiedItem()
            dismiss()
        }

//        binding.btnEditAddQuantity.setOnClickListener {
//            viewModel.increaseItemQuantity()
//        }
//
//        binding.btnEditMinusQuantity.setOnClickListener {
//            viewModel.decreaseItemQuantity()
//        }

        return binding.root
    }

    private fun createAdapters(binding: DialogEditItemBinding){
        val modAdapter = ModifierAdapter(ModifierAdapter.ModifierListener { item ->
            viewModel.onModItemClicked(item)
        })

        val ingAdapter = IngredientsAdapter(IngredientsAdapter.IngredientListener { item ->
            viewModel.onIngredientClicked(item)
        })

        val concatAdapter = ConcatAdapter(modAdapter, ingAdapter)
        binding.editModRecyclerView.adapter = concatAdapter

        viewModel.activeItem.observe(viewLifecycleOwner, { item ->
            modAdapter.submitList(item?.modifiers)
            modAdapter.notifyDataSetChanged()
        })

        viewModel.changedIngredients.observe(viewLifecycleOwner, {
            ingAdapter.submitList(viewModel.changedIngredients.value)
            ingAdapter.notifyDataSetChanged()
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
}