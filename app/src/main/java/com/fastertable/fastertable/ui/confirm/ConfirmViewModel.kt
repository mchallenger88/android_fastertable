package com.fastertable.fastertable.ui.confirm

import com.fastertable.fastertable.common.base.BaseViewModel
import com.fastertable.fastertable.data.repository.LoginRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ConfirmViewModel @Inject constructor (
    private val loginRepository: LoginRepository,
        ): BaseViewModel() {
}