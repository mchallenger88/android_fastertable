package com.fastertable.fastertable2022.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.fastertable.fastertable2022.data.models.RestaurantFloorplan
import com.fastertable.fastertable2022.databinding.FloorplanPropertyDialogBinding
import com.fastertable.fastertable2022.ui.floorplan_manage.FloorplanManageViewModel

class FloorplanSettingDialog: DialogFragment() {
    private var floorplan: RestaurantFloorplan? = null
    private val viewModel: FloorplanManageViewModel by activityViewModels()
    private lateinit var binding: FloorplanPropertyDialogBinding


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FloorplanPropertyDialogBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        floorplan = viewModel.activeFloorplan.value
        floorplan?.let {
            binding.txtFloorplanName.setText(it.name)
        }

        binding.btnFloorplanSave.setOnClickListener {
            saveChanges()
            dismiss()
        }
        binding.btnFloorplanCancel.setOnClickListener { dismiss() }
    }

    override fun onStart() {
        super.onStart()
        var width = (resources.displayMetrics.widthPixels * 0.85).toInt()
        if (width > 1500) {
            width = 1500
        }
        dialog?.let {
            it.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        }

    }

    private fun saveChanges() {
        val name = binding.txtFloorplanName.text.toString()
        viewModel.setFloorplanName(name)
        viewModel.saveFloorplanToCloud()
    }


}