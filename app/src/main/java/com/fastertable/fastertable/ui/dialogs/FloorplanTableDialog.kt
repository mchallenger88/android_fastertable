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


class FloorplanTableDialog: DialogFragment() {
    private var table: RestaurantTable? = null
    private var idView: EditText? = null
    private var tableTypes: Spinner? = null
    private var idLocationSpinner: Spinner? = null
    private var editMinSeats: EditText? = null
    private var editMaxSeats: EditText? = null
    private var tableLocked: CheckBox? = null
    private var btn_cancel: Button? = null
    private var btn_save: Button? = null
    private var btn_remove: Button? = null
    private val viewModel: FloorplanManageViewModel by activityViewModels()
    private var parentFragment: FloorplanManagementFragment? = null
    private var parentFragmentBinding: FloorplanManagementFragmentBinding? = null
    private var rotationValue: EditText? = null

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
        return inflater.inflate(R.layout.table_property_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        table = arguments?.getParcelable("table");
        idView = view.findViewById(R.id.id_table_id_value)
        tableTypes = view.findViewById(R.id.id_type)
        idLocationSpinner = view.findViewById(R.id.id_location_list);
        editMinSeats = view.findViewById(R.id.id_table_min_seats_value)
        editMaxSeats = view.findViewById(R.id.id_table_max_seats_value)
        btn_cancel = view.findViewById(R.id.btn_cancel)
        btn_remove = view.findViewById(R.id.btn_remove)
        btn_save = view.findViewById(R.id.btn_save)
        rotationValue = view.findViewById(R.id.id_rotation_value);
        idView?.setText(table!!.id.toString())
        editMaxSeats?.setText(table!!.minSeats.toString())
        editMaxSeats?.setText(table!!.maxSeats.toString())
        rotationValue?.setText(table!!.rotate.toString())
        btn_cancel?.setOnClickListener{
            dialog?.dismiss()
        }
        btn_save?.setOnClickListener{
            hideKeyboardFrom(requireContext(), requireView())
            updateTable()
        }
        btn_remove?.setOnClickListener{
            removeTable()
        }
        tableTypes?.setSelection(getPosition(TableType.valueOf(table!!.type)))
        idLocationSpinner?.setSelection(getPositionIdLocation(IdLocation.valueOf(table!!.id_location)))
        tableTypes?.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
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
        idLocationSpinner?.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
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
        return tableTypeList.indexOf(tableType);
    }

    private fun getPositionIdLocation(idLocation: IdLocation): Int {
        return idLocationList.indexOf(idLocation);
    }

    private fun updateTable() {
        val id = idView?.text
        val originId = table?.id
        val maxSeats = editMaxSeats?.text
        val minSeats = editMinSeats?.text
        val checked = tableLocked?.isChecked
        val rotate = rotationValue?.text
        if (id.toString().length > 0) {
            table?.id = id.toString().toInt()
        }
        if (minSeats.toString().length > 0) {
            table?.minSeats = minSeats.toString().toInt()
        } else {
            table?.minSeats = 0
        }
        if (maxSeats.toString().length > 0) {
            table?.maxSeats = maxSeats.toString().toInt()
        } else {
            table?.maxSeats = 0
        }
        if (rotate.toString().length > 0) {
            table?.rotate = rotate.toString().toInt()
        } else {
            table?.rotate = 0;
        }
        if (checked != null) {
            table?.locked = checked
        } else {
            table?.locked = false
        }
        Log.d("Floorplan", id.toString() + ", " + originId.toString())

        if (originId != null) {
            table?.let { viewModel.updateTable(it, originId.toInt()) }
        } else {
            table?.let { viewModel.updateTable(it) }
        }
        parentFragmentBinding?.let { parentFragment?.loadTables(it) }
        dialog?.dismiss()
    }

    private fun removeTable() {
        table?.let { viewModel.removeTable(it) };
        parentFragmentBinding?.let { parentFragment?.loadTables(it) }
        dialog?.dismiss()
    }

    private fun hideKeyboardFrom(context: Context, view: View) {
        val imm: InputMethodManager =
            context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}

