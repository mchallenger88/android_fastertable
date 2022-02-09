package com.fastertable.fastertable2022.api

import com.fastertable.fastertable2022.data.models.Menu
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetMenusUseCase @Inject constructor(private val fastertableApi: FastertableApi){
    sealed class Result {
        data class Success(val menus: List<Menu>) : Result()
        object Failure: Result()
    }

    suspend fun getMenus(lid: String): Result {
        return withContext(Dispatchers.IO){
            try{
                val response = fastertableApi.getMenus(lid)
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