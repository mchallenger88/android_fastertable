package com.fastertable.fastertable2022.ui.dialogs

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.fastertable.fastertable2022.common.base.BaseContinueDialog
import com.fastertable.fastertable2022.databinding.DialogContinueCancelBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ContinueCancelFragment : BaseContinueDialog(){
    private val viewModel: ContinueCancelViewModel by activityViewModels()
    private lateinit var callBack: ContinueListener

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = DialogContinueCancelBinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        binding.btnContinue.setOnClickListener {
            viewModel.title.value?.let {
                callBack.returnContinue(it)
            }
            dismiss()
        }

        binding.btnClose.setOnClickListener {
            dismiss()
        }

        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is ContinueListener) {
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
        fun newInstance(): ContinueCancelFragment {
            return ContinueCancelFragment()
        }

        const val TAG = "ContinueCancelFragment"
    }
}