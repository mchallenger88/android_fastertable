package com.fastertable.fastertable2022.api

import com.fastertable.fastertable2022.data.models.Employee
import com.fastertable.fastertable2022.data.models.GetEmployee
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class EmployeeUseCase@Inject constructor(private val fastertableApi: FastertableApi){

    sealed class Result {
        data class Success(val employee: Employee) : Result()
        object Failure : Result()
    }

    suspend fun getEmployee(getEmployee: GetEmployee): Result {
        return withContext(Dispatchers.IO) {
            try {
                val response = fastertableApi.getEmployee(getEmployee)
                if (response.isSuccessful && response.body() != null) {
                    return@withContext Result.Success(response.body()!!)
                } else {
                    return@withContext Result.Failure
                }
            } catch (t: Throwable) {
                if (t !is CancellationException) {
                    return@withContext Result.Failure
                } else {
                    throw t
                }
            }
        }
    }
}