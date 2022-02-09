package com.fastertable.fastertable2022.ui.dialogs

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.fastertable.fastertable2022.R
import com.fastertable.fastertable2022.data.models.DateDialog
import com.fastertable.fastertable2022.databinding.DatePickerDialogBinding
import com.fastertable.fastertable2022.utils.getDate
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DatePickerDialogFragment: BaseDialog(R.layout.date_picker_dialog) {
    private val viewModel: DatePickerViewModel by activityViewModels()
    private val binding: DatePickerDialogBinding by viewBinding()
    private lateinit var returnDate: DateListener

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        binding.btnDateOk.setOnClickListener {
            viewModel.source.value?.let {
                val rd = DateDialog (
                    source = it,
                    date = binding.datePickerFt.getDate()
                )
                returnDate.returnDate(rd)
            }
            dismiss()
        }

        binding.btnDateCancel.setOnClickListener {
            dismiss()
        }

    }

    override fun onStart() {
        super.onStart()
        dialog!!.window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is DateListener) {
            returnDate = context
        } else {
            throw RuntimeException(requireContext().toString() + " must implement OnFragmentInteractionListener")
        }
    }

    companion object {
        fun newInstance(): DatePickerDialogFragment {
            return DatePickerDialogFragment()
        }

        const val TAG = "DatePickerDialogFragment"
    }


}