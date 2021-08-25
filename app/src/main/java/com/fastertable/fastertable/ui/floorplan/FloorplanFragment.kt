package com.fastertable.fastertable.ui.floorplan

import android.app.ActionBar
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.constraintlayout.helper.widget.Flow
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.fragment.app.activityViewModels
import com.fastertable.fastertable.common.base.BaseFragment
import androidx.fragment.app.activityViewModels
import com.fastertable.fastertable.adapters.FloorplanAdapter
import com.fastertable.fastertable.adapters.ModifierAdapter
import com.fastertable.fastertable.data.models.IdLocation
import com.fastertable.fastertable.data.models.RestaurantTable
import com.fastertable.fastertable.data.models.TableType
import com.fastertable.fastertable.databinding.FloorplanFragmentBinding
import com.fastertable.fastertable.ui.dialogs.DialogListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FloorplanFragment: BaseFragment() {
    private val viewModel: FloorplanViewModel by activityViewModels()
    private lateinit var tableListener: FloorplanTableListener
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FloorplanFragmentBinding.inflate(inflater)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        loadTables(binding)

        return binding.root
    }

    private fun loadTables(binding: FloorplanFragmentBinding){
        binding.layoutFloorplan.removeAllViews()
        viewModel.floorplans.observe(viewLifecycleOwner, {
            if (it?.isNotEmpty() == true){
                it[0].tables.forEach { table ->
                    val btnTable = FloorplanTable(activity?.applicationContext!!)
                    btnTable.id = ViewCompat.generateViewId()
                    btnTable.loadTable(table)
                    btnTable.setOnClickListener {
                        tableListener.onClick(table)
                    }

                    val currentLayout = RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT
                    )

                    currentLayout.marginStart = table.left
                    currentLayout.topMargin = table.top
                    btnTable.layoutParams = currentLayout
                    binding.layoutFloorplan.addView(btnTable)
                }

                it[0].walls.forEach { wall ->
                    val floorWall = FloorWall(activity?.applicationContext!!)
                    floorWall.loadWall(wall)

                    floorWall.id = ViewCompat.generateViewId()
                    val currentLayout = ConstraintLayout.LayoutParams(
                        ConstraintLayout.LayoutParams.MATCH_PARENT,
                        ConstraintLayout.LayoutParams.MATCH_PARENT
                    )

                    currentLayout.marginStart = wall.left ?: 0
                    currentLayout.topMargin = wall.top ?: 0
                    floorWall.layoutParams = currentLayout
                    binding.layoutFloorplan.addView(floorWall)
                }
        }})
    }

    fun tableClick(table: RestaurantTable){
        println(table)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is FloorplanTableListener) {
            tableListener = context
        } else {
            throw RuntimeException(requireContext().toString() + " must implement OnFragmentInteractionListener")
        }
    }

}