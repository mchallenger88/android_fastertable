package com.fastertable.fastertable.data.repository

import android.app.Application
import com.fastertable.fastertable.api.GetCheckoutUseCase
import com.fastertable.fastertable.data.models.CheckoutRequest
import com.fastertable.fastertable.data.models.ConfirmEmployee
import java.lang.RuntimeException
import javax.inject.Inject

class GetCheckout @Inject constructor(private val checkoutUseCase: GetCheckoutUseCase,
                                      private val checkoutRepository: CheckoutRepository){
    suspend fun getCheckout(checkoutRequest: CheckoutRequest): ConfirmEmployee {
        val ce: ConfirmEmployee
        val result = checkoutUseCase.getCheckout(checkoutRequest)
        if (result is GetCheckoutUseCase.Result.Success){
            ce = result.confirmEmployee
//            paymentRepository.savePayment(p)
        }else{
            throw RuntimeException("fetch failure")
        }
        return ce
    }
}

class CheckoutRepository @Inject constructor(private val app: Application) {
}