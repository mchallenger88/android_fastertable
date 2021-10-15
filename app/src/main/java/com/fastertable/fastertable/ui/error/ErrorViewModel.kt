package com.fastertable.fastertable.ui.error

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fastertable.fastertable.common.base.BaseViewModel


class ErrorViewModel : BaseViewModel()  {

    private val _errorTitle = MutableLiveData<String>()
    val errorTitle: LiveData<String>
        get() = _errorTitle

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String>
        get() = _errorMessage

    fun setTitle(title: String){
        _errorTitle.value = title
    }

    fun setMessage(message: String){
        _errorMessage.value = message
    }
}