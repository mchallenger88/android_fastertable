package com.fastertable.fastertable.ui.dialogs

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.fastertable.fastertable.R
import com.fastertable.fastertable.data.models.FloorplanWall
import com.fastertable.fastertable.data.models.WallDirection
import com.fastertable.fastertable.databinding.FloorplanManagementFragmentBinding
import com.fastertable.fastertable.ui.floorplan_manage.FloorplanManageViewModel
import com.fastertable.fastertable.ui.floorplan_manage.FloorplanManagementFragment


class FloorplanWallDialog: DialogFragment() {
    private var widthEditor: EditText? = null
    private var thicknessEditor: EditText? = null
    private var directionSelector: Spinner? = null
    private var wall: FloorplanWall?= null
    private var btn_save: Button? = null
    private var btn_remove: Button? = null
    private var btn_cancel: Button? = null
    private val viewModel: FloorplanManageViewModel by activityViewModels()
    private var parentFragment: FloorplanManagementFragment? = null
    private var parentFragmentBinding: FloorplanManagementFragmentBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.wall_property_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        wall = arguments?.getParcelable("wall")
        widthEditor = view.findViewById(R.id.id_width_value)
        thicknessEditor = view.findViewById(R.id.id_thickness_value)
        directionSelector = view.findViewById(R.id.id_direction)
        btn_save = view.findViewById(R.id.btn_save)
        btn_remove = view.findViewById(R.id.btn_remove)
        btn_cancel = view.findViewById(R.id.btn_cancel)
        widthEditor?.setText(wall!!.width.toString())
        thicknessEditor?.setText(wall!!.thickness.toString())
        if (wall?.direction == WallDirection.Horizontal.name) {
            directionSelector?.setSelection(0)
        } else {
            directionSelector?.setSelection(1)
        }
        directionSelector?.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position == 0) {
                    wall?.direction = WallDirection.Horizontal.name;
                } else {
                    wall?.direction = WallDirection.Vertical.name;
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }


        btn_save?.setOnClickListener {
            hideKeyboardFrom(requireContext(), requireView())
            updateWall()
        }

        btn_remove?.setOnClickListener {
            removeWall()
        }

        btn_cancel?.setOnClickListener {
            dialog?.dismiss()
        }
    }

    private fun updateWall() {
        if (widthEditor?.text.toString().length > 0) {
            wall?.width = widthEditor?.text.toString().toInt()
        }
        if (thicknessEditor?.text.toString().length > 0) {
            wall?.thickness = thicknessEditor?.text.toString().toInt()
        }
        wall?.let { viewModel.updateWall(it) }
        parentFragmentBinding?.let { parentFragment?.loadTables(it) }
        dialog?.dismiss()
    }

    private fun removeWall() {
        wall?.let { viewModel.removeWall(it) }
        parentFragmentBinding?.let { parentFragment?.loadTables(it) }
        dialog?.dismiss()
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

    private fun hideKeyboardFrom(context: Context, view: View) {
        val imm: InputMethodManager =
            context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}