package com.fastertable.fastertable.api

import com.fastertable.fastertable.data.models.*
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

class CheckoutUseCase @Inject constructor(private val fastertableApi: FastertableApi){
    sealed class Result {
        data class Success(val userClock: UserClock): Result()
        object Failure: Result()
    }

    suspend fun checkout(checkoutCredentials: CheckoutCredentials): Result {
        return withContext(Dispatchers.IO){
            try{
                val response = fastertableApi.checkout(checkoutCredentials)
                if (response.isSuccessful){
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

class ClockoutUseCase @Inject constructor(private val fastertableApi: FastertableApi){
    sealed class Result {
        data class Success(val userClock: UserClock): Result()
        object Failure: Result()
    }

    suspend fun clockOut(clockoutRequest: ClockOutCredentials): Result {
        return withContext(Dispatchers.IO){
            try{
                val response = fastertableApi.clockout(clockoutRequest)
                if (response.isSuccessful){
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

class GetConfirmListUseCase @Inject constructor(private val fastertableApi: FastertableApi){
    sealed class Result {
        data class Success(val list: List<ConfirmEmployee>) : Result()
        object Failure: Result()
    }

    suspend fun getList(companyTimeBasedRequest: CompanyTimeBasedRequest): Result {
        return withContext(Dispatchers.IO){
            try{
                val response = fastertableApi.getConfirmList(companyTimeBasedRequest)
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

class ConfirmCheckoutUseCase @Inject constructor(private val fastertableApi: FastertableApi){
    sealed class Result {
        data class Success(val userClock: UserClock): Result()
        object Failure: Result()
    }

    suspend fun confirm(checkoutCredentials: CheckoutCredentials): ConfirmCheckoutUseCase.Result {
        return withContext(Dispatchers.IO){
            try{
                val response = fastertableApi.confirmCheckout(checkoutCredentials)
                if (response.isSuccessful){
                    return@withContext ConfirmCheckoutUseCase.Result.Success(response.body()!!)
                }else{
                    return@withContext ConfirmCheckoutUseCase.Result.Failure
                }
            }catch (t: Throwable){
                if (t !is CancellationException){
                    return@withContext  ConfirmCheckoutUseCase.Result.Failure
                }else{
                    throw t
                }
            }
        }
    }
}

class ReopenCheckoutUseCase @Inject constructor(private val fastertableApi: FastertableApi){
    sealed class Result {
        data class Success(val userClock: UserClock): Result()
        object Failure: Result()
    }

    suspend fun reopenCheckout(reopenCheckoutRequest: ReopenCheckoutRequest): ReopenCheckoutUseCase.Result {
        return withContext(Dispatchers.IO){
            try{
                val response = fastertableApi.reopenCheckout(reopenCheckoutRequest)
                if (response.isSuccessful){
                    return@withContext ReopenCheckoutUseCase.Result.Success(response.body()!!)
                }else{
                    return@withContext ReopenCheckoutUseCase.Result.Failure
                }
            }catch (t: Throwable){
                if (t !is CancellationException){
                    return@withContext  ReopenCheckoutUseCase.Result.Failure
                }else{
                    throw t
                }
            }
        }
    }
}