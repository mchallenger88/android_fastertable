package com.fastertable.fastertable.ui.dialogs

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.fastertable.fastertable.databinding.BottomSheetErrorDialogBinding
import com.fastertable.fastertable.ui.error.ErrorViewModel

class ClockinDialog: BaseDialog() {
    private val viewModel: ErrorViewModel by activityViewModels()
    private lateinit var callBack: DialogListener
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = BottomSheetErrorDialogBinding.inflate(inflater)
        binding.viewModel = viewModel

        binding.btnErrorClose.setOnClickListener{
            callBack.returnValue("Done")
            dismiss()
        }
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        val width = 800
        dialog!!.window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is DialogListener) {
            callBack = context
        } else {
            throw RuntimeException(requireContext().toString() + " must implement OnFragmentInteractionListener")
        }
    }

    companion object {
        fun newInstance(): ClockinDialog {
            return ClockinDialog()
        }

        const val TAG = "ClockinDialog"
    }
}