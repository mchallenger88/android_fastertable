package com.fastertable.fastertable.api

import com.fastertable.fastertable.data.models.CheckoutRequest
import com.fastertable.fastertable.data.models.ConfirmEmployee
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetCheckoutUseCase @Inject constructor(private val fastertableApi: FastertableApi){
    sealed class Result {
        data class Success(val confirmEmployee: ConfirmEmployee) : Result()
        object Failure: Result()
    }

    suspend fun getCheckout(checkoutRequest: CheckoutRequest): Result {
        return withContext(Dispatchers.IO){
            try{
                val response = fastertableApi.getCheckout(checkoutRequest)
                if (response.isSuccessful && response.body() != null){
                    return@withContext Result.Success(response.body()!!)
                }else{
                    return@withContext Result.Failure
                }
            }catch (t: Throwable){
                if (t !is CancellationException){
                    return@withContext  Result.Failure
                }else{
                    throw t
                }
            }
        }
    }
}