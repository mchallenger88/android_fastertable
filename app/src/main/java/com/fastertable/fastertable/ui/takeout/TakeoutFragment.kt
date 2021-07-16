package com.fastertable.fastertable.ui.takeout

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.fastertable.fastertable.R
import com.fastertable.fastertable.common.base.BaseFragment
import com.fastertable.fastertable.data.models.TakeOutCustomer
import com.fastertable.fastertable.databinding.TakeoutFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow

@AndroidEntryPoint
class TakeoutFragment : BaseFragment(){
    private val viewModel: TakeoutViewModel by activityViewModels()
    private lateinit var binding: TakeoutFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = TakeoutFragmentBinding.inflate(inflater)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        setUpListeners()

        binding.btnStartTakeout.setOnClickListener {
            if (isValidate()){
                val takeoutCustomer = TakeOutCustomer(
                    name = binding.editTakeoutName.text?.trim().toString(),
                    telephone = binding.editTakeoutPhone.text?.trim().toString(),
                    notes = binding.editTakeoutNotes.text?.trim().toString()
                )
                viewModel.startTakeoutOrder(takeoutCustomer)
            }
        }
        return binding.root
    }

    private fun isValidate(): Boolean =
        validateTakeoutName() && validateTakeoutNumber()

    private fun setUpListeners(){
        binding.editTakeoutName.addTextChangedListener(TextFieldValidation(binding.editTakeoutName))
        binding.editTakeoutPhone.addTextChangedListener(TextFieldValidation(binding.editTakeoutPhone))
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

    private fun validateTakeoutNumber(): Boolean {
        if (!binding.editTakeoutPhone.text.toString().trim().isEmpty()){
            if (!isValidPhone(binding.editTakeoutPhone.text.toString().trim())){
                binding.editTakeoutPhoneLayout.error = "Enter a valid phone number"
                binding.editTakeoutPhone.requestFocus()
                return false
            }else{
                binding.editTakeoutPhoneLayout.isErrorEnabled = false
            }
            return true
        }else{
            binding.editTakeoutPhoneLayout.isErrorEnabled = false
            return true
        }
    }

    private fun isValidPhone(phone: String): Boolean{
        return android.util.Patterns.PHONE.matcher(phone).matches()
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
                R.id.edit_takeout_phone -> {
                    validateTakeoutNumber()
                }
            }
        }

    }
}