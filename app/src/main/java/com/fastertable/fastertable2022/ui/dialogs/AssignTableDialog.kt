package com.fastertable.fastertable2022.ui.dialogs

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.fastertable.fastertable2022.R
import com.fastertable.fastertable2022.common.base.DismissListener
import com.fastertable.fastertable2022.databinding.DialogAssignTableBinding
import com.fastertable.fastertable2022.ui.order.OrderViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AssignTableDialog : BaseDialog(R.layout.dialog_assign_table)  {
    private val viewModel: OrderViewModel by activityViewModels()
    private val binding: DialogAssignTableBinding by viewBinding()
    private lateinit var callBack: DismissListener

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        binding.btnSaveTableNumber.setOnClickListener {
            callBack.getReturnValue(binding.editTableNumber.editText?.text.toString())
            dismiss()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is DismissListener) {
            callBack = context
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
        fun newInstance(): AssignTableDialog {
            return AssignTableDialog()
        }

        const val TAG = "AssignTableDialog"
    }
}