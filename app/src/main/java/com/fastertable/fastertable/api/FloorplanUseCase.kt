package com.fastertable.fastertable.api

import com.fastertable.fastertable.data.models.Order
import com.fastertable.fastertable.data.models.RestaurantFloorplan
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FloorplanUseCase @Inject constructor(private val fastertableApi: FastertableApi) {
    sealed class Result {
        data class Success(val floorplans: List<RestaurantFloorplan>) : Result()
        object Failure : Result()
    }

    sealed class SaveResult {
        data class Success(val floorplan: RestaurantFloorplan) : SaveResult()
        object Failure : SaveResult()
    }

        suspend fun getFloorplans(lid: String, cid: String): FloorplanUseCase.Result {
            return withContext(Dispatchers.IO){
                try{
                    val response = fastertableApi.getFloorplans(lid, cid)
                    if (response.isSuccessful && response.body() != null){
                        return@withContext FloorplanUseCase.Result.Success(response.body()!!)
                    }else{
                        return@withContext FloorplanUseCase.Result.Failure
                    }
                }catch (t: Throwable){
                    if (t !is CancellationException){
                        return@withContext FloorplanUseCase.Result.Failure
                    }else{
                        throw t
                    }
                }
            }
        }

    suspend fun saveFloorplan(restaurantFloorplan: RestaurantFloorplan): FloorplanUseCase.SaveResult {
        return withContext(Dispatchers.IO){
            try{
                val response = fastertableApi.saveFloorplan(restaurantFloorplan)
                if (response.isSuccessful && response.body() != null){
                    return@withContext FloorplanUseCase.SaveResult.Success(response.body()!!)
                }else{
                    return@withContext FloorplanUseCase.SaveResult.Failure
                }
            }catch (t: Throwable){
                if (t !is CancellationException){
                    return@withContext  FloorplanUseCase.SaveResult.Failure
                }else{
                    throw t
                }
            }
        }
    }

    suspend fun updateFloorplan(restaurantFloorplan: RestaurantFloorplan): FloorplanUseCase.SaveResult {
        return withContext(Dispatchers.IO){
            try{
                val response = fastertableApi.updateFloorplan(restaurantFloorplan)
                if (response.isSuccessful && response.body() != null){
                    return@withContext FloorplanUseCase.SaveResult.Success(response.body()!!)
                }else{
                    return@withContext FloorplanUseCase.SaveResult.Failure
                }
            }catch (t: Throwable){
                if (t !is CancellationException){
                    return@withContext  FloorplanUseCase.SaveResult.Failure
                }else{
                    throw t
                }
            }
        }
    }
}