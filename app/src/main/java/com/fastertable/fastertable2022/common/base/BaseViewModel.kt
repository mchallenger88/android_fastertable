package com.fastertable.fastertable2022.common.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

abstract class BaseViewModel: ViewModel() {
    private val observableEvents = MutableLiveData<ViewModelEvent>()

    fun observeViewModelEvents(): LiveData<ViewModelEvent> = observableEvents

    protected fun postViewModelEvent(event: ViewModelEvent) {
        observableEvents.postValue(event)
    }
}

abstract class ViewModelEvent {
    var handled: Boolean = false
        private set

    open fun handle(activity: BaseActivity) {
        handled = true
    }
}

class VmEvent: ViewModelEvent()