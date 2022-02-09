package com.fastertable.fastertable2022.api

import com.fastertable.fastertable2022.data.models.RestaurantFloorplan
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

    sealed class DeleteResult {
        data class Success(val b: Boolean): DeleteResult()
        object Failure: DeleteResult()
    }

        suspend fun getFloorplans(lid: String, cid: String): Result {
            return withContext(Dispatchers.IO){
                try{
                    val response = fastertableApi.getFloorplans(lid, cid)
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

    suspend fun saveFloorplan(restaurantFloorplan: RestaurantFloorplan): SaveResult {
        return withContext(Dispatchers.IO){
            try{
                val response = fastertableApi.saveFloorplan(restaurantFloorplan)
                if (response.isSuccessful && response.body() != null){
                    return@withContext SaveResult.Success(response.body()!!)
                }else{
                    return@withContext SaveResult.Failure
                }
            }catch (t: Throwable){
                if (t !is CancellationException){
                    return@withContext  SaveResult.Failure
                }else{
                    throw t
                }
            }
        }
    }

    suspend fun updateFloorplan(restaurantFloorplan: RestaurantFloorplan): SaveResult {
        return withContext(Dispatchers.IO){
            try{
                val response = fastertableApi.updateFloorplan(restaurantFloorplan)
                if (response.isSuccessful && response.body() != null){
                    return@withContext SaveResult.Success(response.body()!!)
                }else{
                    return@withContext SaveResult.Failure
                }
            }catch (t: Throwable){
                if (t !is CancellationException){
                    return@withContext  SaveResult.Failure
                }else{
                    throw t
                }
            }
        }
    }

    suspend fun deleteFloorplan(id: String, cid: String): Any {
        return withContext(Dispatchers.IO){
            try{
                val response = fastertableApi.deleteFloorplan(id, cid)
                if (response.isSuccessful){
                    return@withContext DeleteResult.Success(true)
                }else{
                    return@withContext DeleteResult.Failure
                }
            }catch (t: Throwable){
                if (t !is CancellationException){
                    return@withContext  DeleteResult.Failure
                }else{
                    throw t
                }
            }
        }
    }
}