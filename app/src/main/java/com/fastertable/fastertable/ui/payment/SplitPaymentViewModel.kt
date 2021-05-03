package com.fastertable.fastertable.ui.payment

import com.fastertable.fastertable.common.base.BaseViewModel
import com.fastertable.fastertable.data.repository.LoginRepository
import com.fastertable.fastertable.data.repository.OrderRepository
import com.fastertable.fastertable.data.repository.PaymentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SplitPaymentViewModel @Inject constructor (private val loginRepository: LoginRepository,
                                                 private val orderRepository: OrderRepository,
                                                 private val paymentRepository: PaymentRepository): BaseViewModel() {
}