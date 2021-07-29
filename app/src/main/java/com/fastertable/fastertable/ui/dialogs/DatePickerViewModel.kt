package com.fastertable.fastertable.ui.dialogs

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fastertable.fastertable.common.base.BaseViewModel
import com.fastertable.fastertable.data.repository.LoginRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DatePickerViewModel @Inject constructor (
    private val loginRepository: LoginRepository,) : BaseViewModel(){

    private val _source = MutableLiveData<String>()
    val source: LiveData<String>
        get() = _source


    fun setSource(source: String){
        _source.value = source
    }
}