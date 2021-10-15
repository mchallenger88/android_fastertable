package com.fastertable.fastertable.ui.floorplan_manage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fastertable.fastertable.R
import com.fastertable.fastertable.common.base.BaseViewModel
import com.fastertable.fastertable.data.models.*
import com.fastertable.fastertable.data.repository.FloorplanQueries
import com.fastertable.fastertable.data.repository.FloorplanRepository
import com.fastertable.fastertable.data.repository.LoginRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FloorplanManageViewModel @Inject constructor(
    private val floorplanQueries: FloorplanQueries,
    private val floorplanRepository: FloorplanRepository,
    private val loginRepository: LoginRepository
): BaseViewModel(){
    val user: OpsAuth = loginRepository.getOpsUser()!!
    val settings: Settings = loginRepository.getSettings()!!
    private var tableList = arrayListOf<RestaurantTable>()
    var wallList = arrayListOf<FloorplanWall>()

    private val _floorplans = MutableLiveData<MutableList<RestaurantFloorplan>>()
    val floorplans: LiveData<MutableList<RestaurantFloorplan>>
        get() = _floorplans

    private val _selectedFloorplanIndex = MutableLiveData(-1)
    val selectedFloorplanIndex: LiveData<Int>
        get() = _selectedFloorplanIndex

    private val _tables = MutableLiveData<List<RestaurantTable>>()
    val tables: LiveData<List<RestaurantTable>>
        get() = _tables

    private val _activeFloorplan = MutableLiveData<RestaurantFloorplan?>()
    val activeFloorplan: LiveData<RestaurantFloorplan?>
        get() = _activeFloorplan

    private val _saveReturn = MutableLiveData(0)
    val saveReturn: LiveData<Int>
        get() = _saveReturn

    private val _requestDelete = MutableLiveData(false)
    val requestDelete: LiveData<Boolean>
        get() = _requestDelete

    private val _deleteReturn = MutableLiveData(0)
    val deleteReturn: LiveData<Int>
        get() = _deleteReturn

    private val _reloadTables = MutableLiveData(false)
    val reloadTables: LiveData<Boolean>
        get() = _reloadTables


    fun getFloorplans() {
        viewModelScope.launch {
            val floorplans = floorplanQueries.getFloorplans(settings.locationId, settings.companyId)

            if (floorplans.isNotEmpty()){
                _floorplans.postValue(floorplans.toMutableList())
                setActiveFloorplan(floorplans[0])
            }else{
                createFloorplan()
            }
        }
    }

    private fun setActiveFloorplan(floorplan: RestaurantFloorplan){
        _activeFloorplan.value = floorplan
        tableList = floorplan.tables
        wallList = floorplan.walls
    }

    fun setFloorplanName(name: String){
        _activeFloorplan.value!!.name = name
    }

    fun getCurrentIndex(): Int{
        return _selectedFloorplanIndex.value!!

    }

    fun selectFloorplan(floorplan: RestaurantFloorplan) {
        setActiveFloorplan(floorplan)
    }

    fun saveFloorplanToCloud(){
        viewModelScope.launch {
            if (_activeFloorplan.value != null){
                if (_activeFloorplan.value!!.id.isBlank()){
                    floorplanQueries.saveFloorplan(activeFloorplan.value!!)
                    _saveReturn.postValue(1)
                }else{
                    floorplanQueries.updateFloorplan(activeFloorplan.value!!)
                    _saveReturn.postValue(2)
                }
            }
        }
    }

    fun startDeleteFloorplan(){
        _requestDelete.value = true
    }

    fun deleteFloorplan(){
        viewModelScope.launch {
            if (_activeFloorplan.value != null){
                if (_activeFloorplan.value!!.id.isBlank()){
                    _floorplans.value!!.remove(_activeFloorplan.value!!)
                    if (_floorplans.value!!.isNotEmpty()){
                        _activeFloorplan.postValue(_floorplans.value!![0])
                    }else{
                        _activeFloorplan.postValue(null)
                    }

                    _deleteReturn.postValue(1)
                }else{
                    val b = floorplanQueries.deleteFloorplan(activeFloorplan.value!!)
                    if (b){
                        _floorplans.value!!.remove(_activeFloorplan.value!!)
                        if (_floorplans.value!!.isNotEmpty()){
                            _activeFloorplan.postValue(_floorplans.value!![0])
                        }else{
                            _activeFloorplan.postValue(null)
                        }
                        _deleteReturn.postValue(1)
                    }else{
                        _deleteReturn.postValue(2)
                    }
                }
            }
            _requestDelete.postValue(false)
        }
    }

    fun createFloorplan() {
        val newFloorplan = RestaurantFloorplan(
            tables = arrayListOf(),
            walls = arrayListOf(),
            companyId = settings.companyId,
            name = "Untitled floorplan",
            id = "",
            locationId = settings.locationId,
            archived = false,
            type = "floorplan",
            _rid = null,
            _self = null,
            _etag = null,
            _attachments = null,
            _ts = null
        )
        _activeFloorplan.value = newFloorplan
        val list = mutableListOf<RestaurantFloorplan>()
        list.add(newFloorplan)
        _floorplans.value = list

        _selectedFloorplanIndex.value = -1
        wallList = _activeFloorplan.value!!.walls
        tableList = _activeFloorplan.value!!.tables
    }

    fun setReloadTables(b: Boolean){
        _reloadTables.value = b
        saveFloorplanToCloud()
    }


    fun updateTable(tableId: Int, x: Int, y: Int): Boolean {
        val index = tableList.indexOfFirst { it.id == tableId }
        return if (index > -1) {
            tableList[index].left = x
            tableList[index].top = y
            true
        } else {
            false
        }
    }

    fun updateTable(table: RestaurantTable, id: Int = -1): Boolean {
        var index = -1
        index = if (id == -1) {
            tableList.indexOfFirst { it.id == table.id }
        } else {
            tableList.indexOfFirst { it.id == id }
        }
        if ( index> -1) {
            tableList[index] = table
        }
        return true
    }

    fun updateWall(wallId: Int, x: Int, y: Int): Boolean {
        val index = wallList.indexOfFirst { it.id == wallId }

        return if (index > -1) {
            wallList[index].left = x
            wallList[index].top = y
            true
        } else {
            false
        }
    }

    fun updateWall(wall: FloorplanWall): Boolean {
        val index = wallList.indexOfFirst { it.id == wall.id }
        if (index > -1) {
            wallList[index] = wall
        }
        return true
    }

    fun getUnitId(type: Int): Int {
        var id = 0
        if (type == 0) { // Table
            tableList.forEach {
                if (it.id > id) id = it.id
                if (it.isCombination) {
                    val combinationTables = it.combinationTables
                    combinationTables?.forEach { c_it ->
                        if (c_it.id > id) {
                            id = c_it.id
                        }
                    }
                }
            }
            return id + 1
        } else { // Wall
            wallList.forEach {
                if (it.id!! > id) id = it.id!!
            }
            return id + 1
        }
    }

    fun addTable(table: RestaurantTable) {
        tableList.add(tableList.count(), table)
        if (_activeFloorplan.value == null){
            createFloorplan()
        }
        _activeFloorplan.value?.tables = tableList
    }

    fun addWall(wall: FloorplanWall) {
        wallList.add(wallList.count(), wall)
    }

    fun getWallById(id: Int): FloorplanWall {
        val index = wallList.indexOfFirst { it.id == id }
        return wallList[index]
    }

    fun getTableById(id: Int): RestaurantTable {
        val index = tableList.indexOfFirst { it.id == id }
        return tableList[index]
    }

    fun removeWall(wall: FloorplanWall): Boolean {
        val index = wallList.indexOfFirst { it.id == wall.id }
        if (index > -1) {
            wallList.removeAt(index)
        }
        return true
    }

    fun removeTable(table: RestaurantTable): Boolean {
        val index = tableList.indexOfFirst { it.id == table.id }
        if (index > -1) {
            tableList.removeAt(index)
        }
        return true
    }

    fun getTableType(resId: Int): TableType {
        when (resId) {
            R.drawable.ic_booth -> return TableType.Booth
            R.drawable.ic_round_two -> return TableType.Round_Two
            R.drawable.ic_round_four -> return TableType.Round_Four
            R.drawable.ic_round_eight -> return TableType.Round_Eight
            R.drawable.ic_round_ten -> return TableType.Round_Ten
            R.drawable.ic_round_bar_stool -> return TableType.Round_Stool
            R.drawable.ic_round_booth -> return TableType.Round_Booth
            R.drawable.ic_rect_two -> return TableType.Rect_Two
            R.drawable.ic_rect_four -> return TableType.Rect_Four
            R.drawable.ic_rect_six -> return TableType.Rect_Six
            R.drawable.ic_rect_horz_six -> return TableType.Rect_Horz_Six
            R.drawable.ic_rect_horz_eight -> return TableType.Rect_Horz_Eight
            R.drawable.ic_rect_horz_four -> return TableType.Rect_Horz_Four
            R.drawable.ic_rect_horz_ten -> return TableType.Rect_Horz_Ten
            R.drawable.ic_square_two -> return TableType.Square_Two
            R.drawable.ic_square_four -> return TableType.Square_Four
            R.drawable.ic_square_bar_stool -> return TableType.Square_Stool

            else -> return TableType.Square_Stool
        }
    }

}