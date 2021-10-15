package com.fastertable.fastertable.ui.dialogs

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.activityViewModels
import com.fastertable.fastertable.databinding.OrderNoteDialogBinding
import com.fastertable.fastertable.ui.order.OrderViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OrderNotesDialogFragment : BaseDialog() {
    private val viewModel: OrderViewModel by activityViewModels()
    private lateinit var itemNoteListener: ItemNoteListener
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = OrderNoteDialogBinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        binding.btnSaveOrderNote.setOnClickListener {
            itemNoteListener.returnNote(binding.editOrderNote.editText?.text.toString())
            hideKeyboardFrom(requireContext(), requireView())
            dismiss()
        }

        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is ItemNoteListener) {
            itemNoteListener = context
        } else {
            throw RuntimeException(requireContext().toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onStart() {
        super.onStart()
        val width = 800
        dialog!!.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
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
