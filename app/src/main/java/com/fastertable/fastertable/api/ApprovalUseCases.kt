package com.fastertable.fastertable.api

import com.fastertable.fastertable.data.models.Approval
import com.fastertable.fastertable.data.models.TimeBasedRequest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SaveApprovalUseCase @Inject constructor(private val fastertableApi: FastertableApi){
    sealed class Result {
        data class Success(val approval: Approval) : Result()
        object Failure: Result()
    }

    suspend fun saveApproval(approval: Approval): Result {
        return withContext(Dispatchers.IO){
            try{
                val response = fastertableApi.saveApproval(approval)
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

class UpdateApprovalUseCase @Inject constructor(private val fastertableApi: FastertableApi){
    sealed class Result {
        data class Success(val approval: Approval) : Result()
        object Failure: Result()
    }

    suspend fun saveApproval(approval: Approval): Result {
        return withContext(Dispatchers.IO){
            try{
                val response = fastertableApi.updateApproval(approval.id, approval)
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

class GetApprovalUseCase @Inject constructor(private val fastertableApi: FastertableApi){

    sealed class Result {
        data class Success(val approval: Approval) : Result()
        object Failure: Result()
    }

    suspend fun getApproval(id: String, lid: String): Result {
        return withContext(Dispatchers.IO){
            try{
                val response = fastertableApi.getApproval(id, lid)
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

class GetApprovalsUseCase @Inject constructor(private val fastertableApi: FastertableApi){

    sealed class Result {
        data class Success(val approvals: List<Approval>) : Result()
        object Failure: Result()
    }

    suspend fun getApprovals(timeBasedRequest: TimeBasedRequest): Result {
        return withContext(Dispatchers.IO){
            try{
                val response = fastertableApi.getApprovals(timeBasedRequest)
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