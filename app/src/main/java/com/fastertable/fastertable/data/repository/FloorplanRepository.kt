package com.fastertable.fastertable.data.repository

import android.app.Application
import com.fastertable.fastertable.api.FloorplanUseCase
import com.fastertable.fastertable.data.models.RestaurantFloorplan
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.File
import java.lang.RuntimeException
import javax.inject.Inject

class FloorplanQueries @Inject constructor(private val floorplanUseCase: FloorplanUseCase,
                                    private val floorplanRepository: FloorplanRepository){

    suspend fun getFloorplans(lid: String, cid: String): List<RestaurantFloorplan>{
        val floorplans: List<RestaurantFloorplan>
        val result = floorplanUseCase.getFloorplans(lid, cid)
        if (result is FloorplanUseCase.Result.Success){
            floorplans = result.floorplans
            floorplanRepository.saveFloorplans(floorplans)
            return floorplans
        }else{
            throw RuntimeException("fetch failed")
        }
    }

    suspend fun saveFloorplan(restaurantFloorplan: RestaurantFloorplan): RestaurantFloorplan{
        val floorplan: RestaurantFloorplan
        val result = floorplanUseCase.saveFloorplan(restaurantFloorplan)
        if (result is FloorplanUseCase.SaveResult.Success){
            floorplan = result.floorplan
            return floorplan
        }else{
            throw RuntimeException("fetch failed")
        }
    }

    suspend fun updateFloorplan(restaurantFloorplan: RestaurantFloorplan): RestaurantFloorplan{
        val floorplan: RestaurantFloorplan
        val result = floorplanUseCase.updateFloorplan(restaurantFloorplan)
        if (result is FloorplanUseCase.SaveResult.Success){
            floorplan = result.floorplan
            return floorplan
        }else{
            throw RuntimeException("fetch failed")
        }
    }
}

class FloorplanRepository @Inject constructor(private val app: Application) {

    fun saveFloorplans(floorplans: List<RestaurantFloorplan>){
        //Save order json to file
        val gson = Gson()
        val jsonString = gson.toJson(floorplans)
        val file= File(app.filesDir, "floorplans.json")
        file.writeText(jsonString)
    }

    fun getFloorplansFromFile(): List<RestaurantFloorplan>?{
        val gson = Gson()
        if (File(app.filesDir, "floorplans.json").exists()){
            val bufferedReader: BufferedReader = File(app.filesDir, "floorplans.json").bufferedReader()
            val inputString = bufferedReader.use { it.readText() }
            val arrayList: ArrayList<RestaurantFloorplan> = gson.fromJson(inputString, object : TypeToken<List<RestaurantFloorplan?>?>() {}.type)
            val list: List<RestaurantFloorplan> = arrayList
            return list
        }
        return null
    }

}