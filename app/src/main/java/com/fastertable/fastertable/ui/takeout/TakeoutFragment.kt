package com.fastertable.fastertable.ui.takeout

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.fastertable.fastertable.R
import com.fastertable.fastertable.common.base.BaseFragment
import com.fastertable.fastertable.data.models.TakeOutCustomer
import com.fastertable.fastertable.databinding.TakeoutFragmentBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TakeoutFragment : BaseFragment(R.layout.takeout_fragment) {
    private val viewModel: TakeoutViewModel by activityViewModels()
    private val binding: TakeoutFragmentBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        setUpListeners()

        binding.editTakeoutName.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE){
                hideKeyboardFrom(requireContext(), requireView())
                true
            }else{
                false
            }
        }

        binding.editTakeoutPhone.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                binding.editTakeoutPhone.setSelection(s.toString().length)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                var txt = s.toString()

                txt.let {
                    if (it.length in 1..3) {
                        txt = it.substring(0, it.length)
                        viewModel.setPhoneNumber(txt)
                    }

                   if (it.length == 4) {
                       txt = "(${it.substring(0, 3)}) ${it.substring(3, 4)}"
                       viewModel.setPhoneNumber(txt)
                   }

                    if (it.length in 7..9){
                        viewModel.setPhoneNumber(txt)
                    }

                    if (it.length == 10){
                        txt = "${txt.substring(0,9)}-${txt.substring(9, 10)}"
                        viewModel.setPhoneNumber(txt)
                    }

                    if (it.length in 11..15){
                        viewModel.setPhoneNumber(txt)
                    }
                }

                validatePhone()
            }
        })

        binding.btnStartTakeout.setOnClickListener {
            if (isValidate()){
                var phone = viewModel.phoneNumber.value
                phone?.let {
                    phone = phone!!.replace("(", "")
                    phone = phone!!.replace(")", "")
                    phone = phone!!.replace(" ", "")
                    phone = phone!!.replace("-", "")
                }

                val takeoutCustomer = TakeOutCustomer(
                    name = binding.editTakeoutName.text?.trim().toString(),
                    telephone = phone ?: "",
                    notes = binding.editTakeoutNotes.text?.trim().toString()
                )
                viewModel.startTakeoutOrder(takeoutCustomer)
            }
        }
    }

    private fun isValidate(): Boolean =
        validateTakeoutName() && validatePhone()

    private fun setUpListeners(){
        binding.editTakeoutName.addTextChangedListener(TextFieldValidation(binding.editTakeoutName))
    }

    private fun validatePhone(): Boolean{
        viewModel.phoneNumber.value?.let {
            if (it.isNotEmpty()){
                if (it.length < 14){
                    binding.editTakeoutPhoneLayout.error = "Not a valid phone number"
                    binding.editTakeoutPhone.requestFocus()
                    return false
                }else{
                    binding.editTakeoutPhoneLayout.isErrorEnabled = false
                    return true
                }
            }else{
                binding.editTakeoutPhoneLayout.isErrorEnabled = false
            }
        }
        return true
    }

    private fun validateTakeoutName(): Boolean {
        if (binding.editTakeoutName.text.toString().trim().isEmpty()) {
            binding.editTakeoutNameLayout.error = "Required Field!"
            binding.editTakeoutName.requestFocus()
            return false
        } else {
            binding.editTakeoutNameLayout.isErrorEnabled = false
        }
        return true
    }

    inner class TextFieldValidation(private val view: View) : TextWatcher {
        override fun afterTextChanged(s: Editable?) {}
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            // checking ids of each text field and applying functions accordingly.
            when (view.id){
                R.id.edit_takeout_name -> {
                    validateTakeoutName()
                }
            }
        }

    }

    private fun hideKeyboardFrom(context: Context, view: View) {
        val imm: InputMethodManager =
            context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}