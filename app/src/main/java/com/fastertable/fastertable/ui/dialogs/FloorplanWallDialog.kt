package com.fastertable.fastertable.ui.dialogs

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.fastertable.fastertable.R
import com.fastertable.fastertable.data.models.FloorplanWall
import com.fastertable.fastertable.data.models.TableType
import com.fastertable.fastertable.data.models.WallDirection
import com.fastertable.fastertable.databinding.FloorplanManagementFragmentBinding
import com.fastertable.fastertable.databinding.WallPropertyDialogBinding
import com.fastertable.fastertable.ui.floorplan_manage.FloorplanManageViewModel
import com.fastertable.fastertable.ui.floorplan_manage.FloorplanManagementFragment


class FloorplanWallDialog: DialogFragment() {
    private lateinit var binding: WallPropertyDialogBinding
    private var wall: FloorplanWall?= null
    private val viewModel: FloorplanManageViewModel by activityViewModels()
    private var parentFragment: FloorplanManagementFragment? = null
    private var parentFragmentBinding: FloorplanManagementFragmentBinding? = null

    private val directions = arrayOf(
        WallDirection.Horizontal, WallDirection.Vertical
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = WallPropertyDialogBinding.inflate(inflater)

        val wallDirections = resources.getStringArray(R.array.id_directions)
        val adapter = ArrayAdapter(requireContext(), R.layout.support_simple_spinner_dropdown_item, wallDirections)
        binding.actWallDirection.setAdapter(adapter)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        wall = arguments?.getParcelable("wall")
        binding.txtWallWidthValue.setText(wall!!.width.toString())
        binding.txtWallThicknessValue.setText(wall!!.thickness.toString())
        binding.txtWallLeftValue.setText(wall!!.left.toString())
        binding.txtWallTopValue.setText(wall!!.top.toString())
        binding.actWallDirection.setText(wall!!.direction, false)

        binding.actWallDirection.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
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


        binding.btnWallSave.setOnClickListener {
            hideKeyboardFrom(requireContext(), requireView())
            updateWall()
        }

        binding.btnWallRemove.setOnClickListener {
            removeWall()
        }

        binding.btnWallCancel.setOnClickListener {
            dismiss()
        }

        binding.btnWallCopy.setOnClickListener {
            copyWall()
            dismiss()
        }
    }

    private fun updateWall() {
        val top = binding.txtWallTopValue.text
        val left = binding.txtWallLeftValue.text
        val thickness = binding.txtWallThicknessValue.text
        val width = binding.txtWallWidthValue.text

        if (top.toString().isNotEmpty()){
            wall?.top = top.toString().toInt()
        }else{
            wall?.top = 10
        }

        if (left.toString().isNotEmpty()){
            wall?.left = left.toString().toInt()
        }else{
            wall?.left = 10
        }

        if (width.toString().isNotEmpty()){
            wall?.width = width.toString().toInt()
        }else{
            wall?.width = 10
        }

        if (thickness.toString().isNotEmpty()){
            wall?.thickness = thickness.toString().toInt()
        }else{
            wall?.thickness = 100
        }

        if (binding.actWallDirection.text.isNotEmpty()){
            wall?.direction = binding.actWallDirection.text.toString()
        }

        wall?.let { viewModel.updateWall(it) }
        viewModel.setReloadTables(true)
        dismiss()
    }

    private fun removeWall() {
        wall?.let { viewModel.removeWall(it) }
        parentFragmentBinding?.let { parentFragment?.loadTables(it) }
        dialog?.dismiss()
    }

    private fun copyWall(){
        val newWall = wall?.clone()
        newWall?.id = viewModel.wallList.count().plus(1)
        newWall?.left = 10
        newWall?.top = 10
        newWall?.let { viewModel.addWall(it) }
        viewModel.setReloadTables(true)
        dismiss()
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