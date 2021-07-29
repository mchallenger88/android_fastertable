package com.fastertable.fastertable.ui.dialogs

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.fastertable.fastertable.data.models.DateDialog
import com.fastertable.fastertable.databinding.DatePickerDialogBinding
import com.fastertable.fastertable.utils.getDate
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DatePickerDialogFragment: BaseDialog() {
    private val viewModel: DatePickerViewModel by activityViewModels()
    private lateinit var returnDate: DateListener

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = DatePickerDialogBinding.inflate(inflater)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        binding.btnDateOk.setOnClickListener {
            val rd = DateDialog (
                source = viewModel.source.value!!,
                date = binding.datePickerFt.getDate()
                    )
            returnDate.returnDate(rd)
            dismiss()
        }

        binding.btnDateCancel.setOnClickListener {
            dismiss()
        }

        return binding.root
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