package com.fastertable.fastertable2022.ui.dialogs

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.activityViewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.fastertable.fastertable2022.R
import com.fastertable.fastertable2022.databinding.OrderNoteDialogBinding
import com.fastertable.fastertable2022.ui.order.OrderViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OrderNotesDialogFragment : BaseDialog(R.layout.order_note_dialog) {
    private val viewModel: OrderViewModel by activityViewModels()
    private val binding: OrderNoteDialogBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        binding.btnSaveOrderNote.setOnClickListener {
            viewModel.addItemNote(binding.txtOrderNote.text.toString())
            hideKeyboardFrom(requireContext(), requireView())
            dismiss()
        }
        binding.btnCancelOrderNote.setOnClickListener {
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        val width = 1200
        dialog?.let {
            it.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }


    companion object {
        fun newInstance(): OrderNotesDialogFragment {
            return OrderNotesDialogFragment()
        }

        const val TAG = "OrderNotesDialogFragment"
    }

    private fun hideKeyboardFrom(context: Context, view: View) {
        val imm: InputMethodManager =
            context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}
