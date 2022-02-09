package com.fastertable.fastertable2022.api

import com.fastertable.fastertable2022.data.models.Payment
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SavePaymentUseCase @Inject constructor(private val fastertableApi: FastertableApi){
    sealed class Result {
        data class Success(val payment: Payment) : Result()
        object Failure: Result()
    }

    suspend fun savePayment(payment: Payment): Result {
        return withContext(Dispatchers.IO){
            try{
                val response = fastertableApi.savePayment(payment)
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

class UpdatePaymentUseCase @Inject constructor(private val fastertableApi: FastertableApi){
    sealed class Result {
        data class Success(val payment: Payment) : Result()
        object Failure: Result()
    }

    suspend fun savePayment(payment: Payment): Result {
        return withContext(Dispatchers.IO){
            try{
                val response = fastertableApi.updatePayment(payment.id, payment)
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

class GetPaymentUseCase @Inject constructor(private val fastertableApi: FastertableApi){

    sealed class Result {
        data class Success(val payment: Payment?) : Result()
        object Failure: Result()
    }

    suspend fun getPayment(id: String, lid: String): Result {
        return withContext(Dispatchers.IO){
            try{
                val response = fastertableApi.getPayment(id, lid)
                when (response.isSuccessful){
                    response.body() != null -> {
                        return@withContext Result.Success(response.body()!!)
                    }
                    response.body() == null -> {
                        return@withContext Result.Success(null)
                    }
                    else ->  return@withContext Result.Failure
                }
            }catch (t: Throwable){
                if (t !is CancellationException){
                    return@withContext Result.Failure
                }else{
                    throw t
                }
            }
        }
    }
}

class GetPaymentsUseCase @Inject constructor(private val fastertableApi: FastertableApi){

    sealed class Result {
        data class Success(val payments: List<Payment>) : Result()
        object Failure: Result()
    }

    suspend fun getPayments(midnight: Long, rid: String): Result {
        return withContext(Dispatchers.IO){
            try{
                val response = fastertableApi.getPayments(midnight, rid)
                if (response.isSuccessful && response.body() != null){
                    return@withContext Result.Success(response.body()!!)
                }else{
                    return@withContext Result.Failure
                }
            }catch (t: Throwable){
                if (t !is CancellationException){
                    return@withContext Result.Failure
                }else{
                    throw t
                }
            }
        }
    }
}
