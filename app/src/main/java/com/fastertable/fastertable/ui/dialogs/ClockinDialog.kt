package com.fastertable.fastertable.ui.dialogs

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.fastertable.fastertable.R
import com.fastertable.fastertable.databinding.BottomSheetErrorDialogBinding
import com.fastertable.fastertable.ui.error.ErrorViewModel

class ClockinDialog: BaseDialog(R.layout.bottom_sheet_error_dialog) {
    private val viewModel: ErrorViewModel by activityViewModels()
    private val binding: BottomSheetErrorDialogBinding by viewBinding()
    private lateinit var callBack: DialogListener

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel

        binding.btnErrorClose.setOnClickListener{
            callBack.returnValue("Done")
            dismiss()
        }
    }
//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
//        val binding = BottomSheetErrorDialogBinding.inflate(inflater)
//
//        return binding.root
//    }

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