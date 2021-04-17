package com.fastertable.fastertable.api

import com.fastertable.fastertable.data.models.Company
import com.fastertable.fastertable.data.models.Order
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetOrderUseCase @Inject constructor(private val fastertableApi: FastertableApi){

    sealed class Result {
        data class Success(val order: Order) : Result()
        object Failure: Result()
    }

    suspend fun getOrder(id: String, lid: String): Result {
        return withContext(Dispatchers.IO){
            try{
                val response = fastertableApi.getOrder(id, lid)
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

class GetOrdersUseCase @Inject constructor(private val fastertableApi: FastertableApi){

    sealed class Result {
        data class Success(val orders: List<Order>) : Result()
        object Failure: Result()
    }

    suspend fun getOrders(midnight: Long, rid: String): Result {
        return withContext(Dispatchers.IO){
            try{
                val response = fastertableApi.getOrders(midnight, rid)
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