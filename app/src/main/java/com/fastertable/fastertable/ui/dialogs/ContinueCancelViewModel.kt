package com.fastertable.fastertable.ui.dialogs

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fastertable.fastertable.common.base.BaseViewModel
import com.fastertable.fastertable.data.repository.LoginRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ContinueCancelViewModel @Inject constructor (
    private val loginRepository: LoginRepository,) : BaseViewModel() {

    private val _title = MutableLiveData<String>()
    val title: LiveData<String>
        get() = _title

    private val _message = MutableLiveData<String>()
    val message: LiveData<String>
        get() = _message

    fun setTitle(title: String){
        _title.value = title
    }

    fun setMessage(message: String){
        _message.value = message
    }
}