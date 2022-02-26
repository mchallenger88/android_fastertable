package com.fastertable.fastertable2022.api

import android.util.Log
import com.fastertable.fastertable2022.data.models.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class StartCreditUseCase @Inject constructor(private val fastertableApi: FastertableApi){
    sealed class Result {
        data class Success(val response: TerminalResponse) : Result()
        object Failure: Result()
    }

    suspend fun startCredit(url: String): Result {
        return withContext(Dispatchers.IO){
            try{
                val response = fastertableApi.startCredit(url)
                if (response.isSuccessful && response.body() != null){
                    return@withContext Result.Success(response.body()!!)
                }else{
                    Log.d("Testing", Result.Failure.toString())
                    return@withContext Result.Failure
                }
            }catch (t: Throwable){
                if (t !is CancellationException){
                    Log.d("Testing1", Result.Failure.toString())
                    return@withContext  Result.Failure
                }else{
                    throw t
                }
            }
        }
    }



    suspend fun cancelCredit(url: String): Result {
        return withContext(Dispatchers.IO){
            try{
                val response = fastertableApi.cancelCredit(url)
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

class StageResponseUseCase @Inject constructor(private val fastertableApi: FastertableApi) {
    sealed class Result {
        data class Success(val response: Any) : Result()
        object Failure: Result()
    }

    suspend fun stageTransaction(transaction: CayanCardTransaction): StageResponseUseCase.Result {
        return withContext(Dispatchers.IO){
            try{
                val response = fastertableApi.stageTransaction(transaction)
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

class InitiateCreditTransactionUseCase @Inject constructor(private val fastertableApi: FastertableApi) {
    sealed class Result {
        data class Success(val response: CayanTransaction) : Result()
        object Failure: Result()
    }

    suspend fun initiateTransaction(url: String): Result {
        return withContext(Dispatchers.IO){
            try{
                val response = fastertableApi.initiateTransaction(url)
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

class AdjustTipUseCase @Inject constructor(private val fastertableApi: FastertableApi) {
    sealed class Result {
        data class Success(val response: Any) : Result()
        object Failure: Result()
    }

    suspend fun tipAdjust(transaction: AdjustTip): Result {
        return withContext(Dispatchers.IO){
            try{
                val response = fastertableApi.tipAdjustment(transaction)
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

class CaptureRequestUseCase @Inject constructor(private val fastertableApi: FastertableApi) {
    sealed class Result {
        data class Success(val response: TransactionResponse45) : Result()
        object Failure: Result()
    }

    suspend fun capture(transaction: CaptureRequest): Result {
        return withContext(Dispatchers.IO){
            try{
                val response = fastertableApi.captureTicket(transaction)
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

class ManualCreditAuthorizationUseCase @Inject constructor(private val fastertableApi: FastertableApi) {
    sealed class Result {
        data class Success(val response: TransactionResponse45) : Result()
        object Failure: Result()
    }

    suspend fun authorize(authorizationRequest: AuthorizationRequest): Result {
        return withContext(Dispatchers.IO){
            try{
                val response = fastertableApi.manualAuthorization(authorizationRequest)
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

class VoidCreditUseCase @Inject constructor(private val fastertableApi: FastertableApi){
    sealed class Result {
        data class Success(val response: TransactionResponse45) : Result()
        object Failure: Result()
    }

    suspend fun void(refundRequest: RefundRequest): Result {
        return withContext(Dispatchers.IO){
            try {
                val response = fastertableApi.voidCreditPayment(refundRequest)
                if (response.isSuccessful && response.body() != null ){
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
