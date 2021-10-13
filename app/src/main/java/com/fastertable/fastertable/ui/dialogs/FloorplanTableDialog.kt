package com.fastertable.fastertable.ui.dialogs

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.fastertable.fastertable.R
import com.fastertable.fastertable.data.models.IdLocation
import com.fastertable.fastertable.data.models.RestaurantTable
import com.fastertable.fastertable.data.models.TableType
import com.fastertable.fastertable.databinding.FloorplanManagementFragmentBinding
import com.fastertable.fastertable.ui.floorplan_manage.FloorplanManageViewModel
import com.fastertable.fastertable.ui.floorplan_manage.FloorplanManagementFragment
import android.app.Activity
import android.content.Context
import android.view.inputmethod.InputMethodManager
import com.fastertable.fastertable.databinding.TablePropertyDialogBinding
import android.icu.lang.UCharacter.GraphemeClusterBreak.T





class FloorplanTableDialog: DialogFragment() {
    private lateinit var binding: TablePropertyDialogBinding

    private var table: RestaurantTable? = null
    private val viewModel: FloorplanManageViewModel by activityViewModels()
    private var parentFragment: FloorplanManagementFragment? = null
    private var parentFragmentBinding: FloorplanManagementFragmentBinding? = null

    private val tableTypeList = arrayOf(
        TableType.Booth, TableType.Round_Two, TableType.Round_Four, TableType.Round_Eight,
        TableType.Round_Ten, TableType.Round_Stool, TableType.Round_Booth, TableType.Rect_Two,
        TableType.Rect_Four, TableType.Rect_Six, TableType.Rect_Horz_Six, TableType.Rect_Horz_Eight,
        TableType.Rect_Horz_Four, TableType.Rect_Horz_Ten, TableType.Square_Four, TableType.Square_Stool
    )
    private val idLocationList = arrayOf(
        IdLocation.TopLeft, IdLocation.TopCenter, IdLocation.TopRight,
        IdLocation.MiddleLeft, IdLocation.MiddleCenter, IdLocation.MiddleRight,
        IdLocation.BottomLeft, IdLocation.BottomCenter, IdLocation.BottomRight
    );
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = TablePropertyDialogBinding.inflate(inflater)

        val tableTypes = resources.getStringArray(R.array.table_types)
        val adapter = ArrayAdapter(requireContext(), R.layout.support_simple_spinner_dropdown_item, tableTypes)
        binding.actTableTypesAuto.setAdapter(adapter)

        val idLocations = resources.getStringArray(R.array.id_locations)
        val adapter2 = ArrayAdapter(requireContext(), R.layout.support_simple_spinner_dropdown_item, idLocations)
        binding.actIdLocations.setAdapter(adapter2)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        table = arguments?.getParcelable("table")

        binding.txtTableIdValue.setText(table!!.id.toString())
        binding.txtMaxSeatsValue.setText(table!!.maxSeats.toString())
        binding.txtMinSeatsValue.setText(table!!.minSeats.toString())
        binding.txtRotateValue.setText(table!!.rotate.toString())
        binding.txtLeftValue.setText(table!!.left.toString())
        binding.txtTopValue.setText(table!!.top.toString())

        binding.btnCancel.setOnClickListener{
            dialog?.dismiss()
        }
        binding.btnSave.setOnClickListener{
            hideKeyboardFrom(requireContext(), requireView())
            updateTable()
        }
        binding.btnRemove.setOnClickListener{
            removeTable()
        }

        binding.btnCopy.setOnClickListener {
            copyTable()
        }

        binding.actIdLocations.setText(table!!.id_location, false)
        binding.actTableTypesAuto.setText(table!!.type, false)

        binding.actTableTypesAuto.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                table?.type = tableTypeList[position].name;
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        binding.actIdLocations.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                table?.id_location = idLocationList[position].name
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

    }

    fun setParentFragment(fragment: FloorplanManagementFragment, binding: FloorplanManagementFragmentBinding) {
        parentFragment = fragment;
        parentFragmentBinding = binding;
    }

    override fun onStart() {
        super.onStart()
        var width = (resources.displayMetrics.widthPixels * 0.85).toInt()
        if (width > 1500) {
            width = 1500
        }
        dialog!!.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun getPosition( tableType:TableType ): Int {
        return tableTypeList.indexOfFirst { it.name == tableType.name }
    }

    private fun getPositionIdLocation(idLocation: IdLocation): Int {
        return idLocationList.indexOfFirst { it.name == idLocation.name }
    }

    private fun updateTable() {
        val id = binding.txtTableIdValue.text
        val originId = table?.id
        val maxSeats = binding.txtMaxSeatsValue.text
        val minSeats = binding.txtMinSeatsValue.text
        val rotate = binding.txtRotateValue.text
        val top = binding.txtTopValue.text
        val left = binding.txtLeftValue.text

        if (id.toString().isNotEmpty()) {
            table?.id = id.toString().trim().toInt()
        }
        if (minSeats.toString().isNotEmpty()) {
            table?.minSeats = minSeats.toString().trim().toInt()
        } else {
            table?.minSeats = 0
        }
        if (maxSeats.toString().isNotEmpty()) {
            table?.maxSeats = maxSeats.toString().trim().toInt()
        } else {
            table?.maxSeats = 0
        }
        if (rotate.toString().isNotEmpty()) {
            table?.rotate = rotate.toString().trim().toInt()
        } else {
            table?.rotate = 0;
        }

        if (top.toString().isNotEmpty()){
            table?.top = top.toString().trim().toInt()
        }else{
            table?.top = 10
        }

        if (left.toString().isNotEmpty()){
            table?.left = left.toString().trim().toInt()
        }else{
            table?.left = 10
        }

        if (binding.actIdLocations.text.isNotEmpty()){
            table?.id_location = binding.actIdLocations.text.toString()
        }

        if (binding.actTableTypesAuto.text.isNotEmpty()){
            table?.type = binding.actTableTypesAuto.text.toString().replace(" ", "_")
        }


        if (originId != null) {
            table?.let { viewModel.updateTable(it, originId.toInt()) }
        } else {
            table?.let { viewModel.updateTable(it) }
        }

        viewModel.setReloadTables(true)
        dismiss()
    }

    private fun removeTable() {
        table?.let { viewModel.removeTable(it) };
        viewModel.setReloadTables(true)
        dismiss()
    }

    private fun copyTable(){
        val newTable = table?.clone()
        newTable?.id = getNextId(newTable!!)
        newTable.left = 10
        newTable.top = 10
        newTable.let { viewModel.addTable(it) }
        viewModel.setReloadTables(true)
        dismiss()
    }

    private fun getNextId(table: RestaurantTable): Int {
        val ts = viewModel.activeFloorplan.value?.sortedTableList()
        val idArray = mutableListOf<Int>()
        if (ts != null) {
            for (t in ts){
                idArray.add(t.id)
            }
        }
        var id = table.id.plus(1)
        var b = true

        while (b){
            b = idArray.contains(id)
            if (b){
                id++
            }
        }

        return id
    }

    private fun hideKeyboardFrom(context: Context, view: View) {
        val imm: InputMethodManager =
            context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

}

