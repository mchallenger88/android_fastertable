package com.fastertable.fastertable2022.data.repository

import android.app.Application
import com.fastertable.fastertable2022.api.*
import com.fastertable.fastertable2022.data.models.*
import java.lang.RuntimeException
import javax.inject.Inject

class GetCheckout @Inject constructor(private val getCheckoutUseCase: GetCheckoutUseCase,
                                      private val checkoutRepository: CheckoutRepository){
    suspend fun getCheckout(checkoutRequest: CheckoutRequest): ConfirmEmployee {
        val ce: ConfirmEmployee
        val result = getCheckoutUseCase.getCheckout(checkoutRequest)
        if (result is GetCheckoutUseCase.Result.Success){
            ce = result.confirmEmployee
//            paymentRepository.savePayment(p)
        }else{
            throw RuntimeException("fetch failure")
        }
        return ce
    }
}

class GetConfirmList @Inject constructor(private val getConfirmListUseCase: GetConfirmListUseCase,
                                      private val checkoutRepository: CheckoutRepository){
    suspend fun getList(companyTimeBasedRequest: CompanyTimeBasedRequest): List<ConfirmEmployee> {
        val ce: List<ConfirmEmployee>
        val result = getConfirmListUseCase.getList(companyTimeBasedRequest)
        if (result is GetConfirmListUseCase.Result.Success){
            ce = result.list
        }else{
            throw RuntimeException("fetch failure")
        }
        return ce
    }
}

class CheckoutUser @Inject constructor(private val checkoutUseCase: CheckoutUseCase){
    suspend fun checkout(checkoutCredentials: CheckoutCredentials): UserClock{
        val result = checkoutUseCase.checkout(checkoutCredentials)

        if (result is CheckoutUseCase.Result.Success){
            return result.userClock
        }else{
            throw RuntimeException("fetch failed")
        }
    }
}

class ConfirmCheckout @Inject constructor(private val confirmCheckoutUseCase: ConfirmCheckoutUseCase){
    suspend fun confirm(checkoutCredentials: CheckoutCredentials): UserClock{
        val result = confirmCheckoutUseCase.confirm(checkoutCredentials)

        if (result is ConfirmCheckoutUseCase.Result.Success){
            return result.userClock
        }else{
            throw RuntimeException("fetch failed")
        }
    }
}

class ReopenCheckout @Inject constructor(private val reopenCheckoutUseCase: ReopenCheckoutUseCase){
    suspend fun open(reopenCheckoutRequest: ReopenCheckoutRequest): UserClock{
        val result = reopenCheckoutUseCase.reopenCheckout(reopenCheckoutRequest)

        if (result is ReopenCheckoutUseCase.Result.Success){
            return result.userClock
        }else{
            throw RuntimeException("fetch failed")
        }
    }
}

class CheckoutRepository @Inject constructor(private val app: Application) {
}