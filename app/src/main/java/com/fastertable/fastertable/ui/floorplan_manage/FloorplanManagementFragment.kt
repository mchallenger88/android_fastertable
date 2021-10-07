package com.fastertable.fastertable.ui.floorplan_manage

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipDescription
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.Point
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.fastertable.fastertable.MainActivity
import com.fastertable.fastertable.R
import com.fastertable.fastertable.adapters.FloorplanSidebarAdapter
import com.fastertable.fastertable.data.models.FloorplanWall
import com.fastertable.fastertable.data.models.IdLocation
import com.fastertable.fastertable.data.models.RestaurantTable
import com.fastertable.fastertable.data.models.WallDirection
import com.fastertable.fastertable.databinding.FloorplanManagementFragmentBinding
import com.fastertable.fastertable.ui.dialogs.FloorplanSettingDialog
import com.fastertable.fastertable.ui.dialogs.FloorplanTableDialog
import com.fastertable.fastertable.ui.dialogs.FloorplanWallDialog
import com.fastertable.fastertable.ui.floorplan.FloorWall
import com.fastertable.fastertable.ui.floorplan.FloorplanTable
import com.fastertable.fastertable.ui.floorplan.FloorplanTableListener
import com.fastertable.fastertable.utils.DoubleClickListener


enum class ControllerType {
    Top, Left, Right, Bottom, Property
}

class FloorplanManagementFragment: Fragment() {
    private var _binding: FloorplanManagementFragmentBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FloorplanManageViewModel by activityViewModels()
    private lateinit var tableListener: FloorplanTableListener

    private var selectedTable: RestaurantTable? = null
    private var selectedWall: FloorplanWall? = null
    private var rangeTrash: ImageView? = null
    private var floorplanSpinner: Spinner? = null
    private var viewX = 0.0f
    private var viewY = 0.0f

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FloorplanManagementFragmentBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        loadFloorplans(binding)
        createObservers()
        loadSidebar(binding)
        handleDrop(binding)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setOnClickListener {
            selectedTable=null
            selectedWall=null
            loadTables(binding)
        }

        rangeTrash = view.findViewById(R.id.trashRange)
        handleTrashRange((rangeTrash as ImageView))

        binding.btnSettingsFloorplan.setOnClickListener {
            activity?.supportFragmentManager?.let { it1 ->
                val floorplanSettingDialog = FloorplanSettingDialog()
                floorplanSettingDialog.show(it1, "Floorplan Property")
            }
        }
        floorplanSpinner = view.findViewById(R.id.floorplans)

        binding.btnTop.setOnClickListener { positionController(ControllerType.Top) }
        binding.btnLeft.setOnClickListener { positionController(ControllerType.Left) }
        binding.btnRight.setOnClickListener { positionController(ControllerType.Right) }
        binding.btnBottom.setOnClickListener { positionController(ControllerType.Bottom) }
        binding.btnSettings.setOnClickListener{ positionController(ControllerType.Property) }
        binding.btnRotationLeft.setOnClickListener { rotateController(1) }
        binding.btnRotationRight.setOnClickListener { rotateController(-1) }

        binding.btnSaveFloorplan.setOnClickListener {
            viewModel.saveFloorplanToCloud()
        }

        binding.btnNewFloorplan.setOnClickListener {
            if (viewModel.activeFloorplan.value != null){
                viewModel.saveFloorplanToCloud()
                viewModel.createFloorplan()
            }
        }

        binding.btnDeleteFloorplan.setOnClickListener {
            viewModel.deleteFloorplan()
        }

    }

    private fun loadFloorplans(binding: FloorplanManagementFragmentBinding){
        viewModel.getFloorplans()

        viewModel.floorplans.observe(viewLifecycleOwner, { it ->
            if (it != null){
                loadTables(binding)
                initFloorplanSpinner()
            }
        })
    }

    private fun createObservers(){
        viewModel.saveReturn.observe(viewLifecycleOwner, { it ->
            when (it){
//                0 -> {(activity as MainActivity).alertMessage("The Floorplan was not save.")}
                1 -> { (activity as MainActivity).alertMessage("Saved the Floorplan Successfully!")}
                2 -> {(activity as MainActivity).alertMessage("Updated the Floorplan Successfully!")}
                else -> {(activity as MainActivity).alertMessage("The Floorplan was not saved.")}
            }
        })

        viewModel.activeFloorplan.observe(viewLifecycleOwner, {
            if (it != null){
                loadTables(binding)
            }
        })

        viewModel.deleteReturn.observe(viewLifecycleOwner, {
            when (it){
                1 -> {
                    refreshSpinnerList()
                    (activity as MainActivity).alertMessage("The Floorplan was deleted!")}
                2 -> {(activity as MainActivity).alertMessage("An error occurred when deleting the Floorplan!")}
            }
        })

    }

    private fun handleDrop(binding: FloorplanManagementFragmentBinding) {

        val dragListener = View.OnDragListener { v, event ->
            // Handles each of the expected events
            when (event.action) {
                DragEvent.ACTION_DRAG_STARTED -> true
                DragEvent.ACTION_DRAG_ENTERED -> true
                DragEvent.ACTION_DRAG_LOCATION -> true
                DragEvent.ACTION_DRAG_EXITED -> true
                DragEvent.ACTION_DROP -> {
                    val scale = resources.displayMetrics.density
                    var innerPaddingX = (60.0f * scale + 0.5f).toInt()
                    val innerPaddingY = (20.0f * scale + 0.5f).toInt()
                    val data = event.clipData.getItemAt(0)
                    if (data.text.toString() == "move_table") {
                        val tableId = event.clipData.getItemAt(1).text.toString().toInt()
                        val tableIdLength = tableId.toString().length
                        innerPaddingX = (15.0f * tableIdLength * scale + 0.5f).toInt()
                        viewModel.updateTable(tableId, event.x.toInt() - viewX.toInt() - innerPaddingX, event.y.toInt() - viewY.toInt() - innerPaddingY)
                    } else if (data.text.toString() == "move_wall") {
                        val wallId = event.clipData.getItemAt(1).text.toString().toInt()
                        viewModel.updateWall(wallId, event.x.toInt() - viewX.toInt(), event.y.toInt() - viewY.toInt())
                    } else {
                        val resId = data.text.toString().toInt()
                        val paddingX = event.clipData.getItemAt(1).text.toString().toFloat().toInt()
                        val paddingY = event.clipData.getItemAt(2).text.toString().toFloat().toInt()
                        if (resId != R.drawable.ic_wall) {
                            val tableType = viewModel.getTableType(resId)
                            val idLength =viewModel.getUnitId(0).toString().length
                            innerPaddingX = (15.0f * idLength * scale + 0.5f).toInt()
                            val table = RestaurantTable(
                                id = viewModel.getUnitId(0),
                                type = tableType.name,
                                rotate = 0,
                                locked = false,
                                reserved = false,
                                active = false,
                                id_location = IdLocation.TopLeft.name,
                                maxSeats = 6,
                                minSeats = 2,
                                left = event.x.toInt() - paddingX - innerPaddingX,
                                top = event.y.toInt() - paddingY - innerPaddingY,
                                isCombination = false,
                                combinationTables = arrayListOf()
                            )

                            selectedWall = null
                            selectedTable = table
                            viewModel.addTable(table)
                        } else {
                            val wall = FloorplanWall(
                                id = viewModel.getUnitId(1),
                                left = event.x.toInt() - paddingX,
                                top = event.y.toInt() - 1,
                                height = 2,
                                width = 100,
                                direction = WallDirection.Horizontal.name,
                                thickness = 10
                            )
                            selectedTable = null
                            selectedWall = wall
                            viewModel.addWall(wall)
                        }
                    }
                    loadTables(binding)
                    v.invalidate()
                    true
                }
                DragEvent.ACTION_DRAG_ENDED -> true
                else -> false
            }
        }

        binding.layoutFloorplan.setOnDragListener(dragListener)
    }

    private fun handleTrashRange(range: ImageView) {
        val dragListener = View.OnDragListener { v, event ->
            // Handles each of the expected events
            when (event.action) {
                DragEvent.ACTION_DRAG_STARTED -> true
                DragEvent.ACTION_DRAG_ENTERED -> {
                    (v as ImageView).setColorFilter(Color.RED)
                    true
                }
                DragEvent.ACTION_DRAG_LOCATION -> true
                DragEvent.ACTION_DRAG_EXITED -> {
                    (v as ImageView).clearColorFilter()
                    true
                }
                DragEvent.ACTION_DROP -> {
                    val data = event.clipData.getItemAt(0)
                    if (data.text.toString() == "move_table") {
                        val tableId = event.clipData.getItemAt(1).text.toString().toInt()
                        val table = viewModel.getTableById(tableId)
                        viewModel.removeTable(table)
                        selectedTable = null
                    } else if (data.text.toString() == "move_wall") {
                        val wallId = event.clipData.getItemAt(1).text.toString().toInt()
                        val wall = viewModel.getWallById(wallId)
                        viewModel.removeWall(wall)
                        selectedWall = null
                    }
                    (v as ImageView).clearColorFilter()
                    loadTables(binding)
                    v.invalidate()
                    true
                }
                DragEvent.ACTION_DRAG_ENDED -> true
                else -> false
            }
        }

        range.setOnDragListener(dragListener)
    }

    @SuppressLint("ClickableViewAccessibility")
    fun loadTables(binding: FloorplanManagementFragmentBinding){
        binding.layoutFloorplan.removeAllViews()
        if (viewModel.activeFloorplan.value !== null) {
            viewModel.activeFloorplan.value!!.tables.forEach { table ->
                val btnTable = FloorplanTable(activity?.applicationContext!!)
                btnTable.id = ViewCompat.generateViewId()
                btnTable.loadTable(table)
                if (selectedTable != null) {
                    if (table.id == selectedTable!!.id) {
                        btnTable.setTableColorFilter(Color.GREEN)
                    }
                }
                btnTable.setOnClickListener() {
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

                btnTable.getTableImage().setOnTouchListener { v, event ->
                    if (event != null) {
                        if (event.action == MotionEvent.ACTION_DOWN) {
                            val screenX = event.x
                            val screenY = event.y
                            v?.left?.let { it1 -> screenX.minus(it1) }
                            v?.top?.let { it1 -> screenY.minus(it1) }
                            viewX = screenX
                            viewY = screenY
                        }
                    }
                    val item = ClipData.Item("move_table")
                    btnTable.setTableColorFilter(Color.GREEN)
                    v.invalidate()

                    selectedTable = table
                    selectedWall = null
                    val dragData = ClipData(
                        v?.tag as? CharSequence,
                        arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN),
                        item
                    )
                    dragData.addItem(ClipData.Item(table.id.toString()))

                    val shadowBuilder = MyDragShadowBuilder(btnTable.getTableImage(), viewX.toInt(), viewY.toInt())

                    v?.startDragAndDrop(
                        dragData,
                        shadowBuilder,
                        btnTable.getTableImage(),
                        0
                    )
                    false
                }
            }
            viewModel.activeFloorplan.value!!.walls.forEach { wall ->
                val floorWall = FloorWall(activity?.applicationContext!!)
                floorWall.loadWall(wall)
                if (selectedWall != null) {
                    if (wall.id == selectedWall!!.id) {
                        floorWall.getWallImage().setBackgroundColor(Color.GREEN)
                    }
                }
                floorWall.id = ViewCompat.generateViewId()
                val currentLayout = ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.MATCH_PARENT,
                    ConstraintLayout.LayoutParams.MATCH_PARENT
                )

                currentLayout.marginStart = wall.left
                currentLayout.topMargin = wall.top
                floorWall.layoutParams = currentLayout
                binding.layoutFloorplan.addView(floorWall)
                floorWall.getWallImage().setOnTouchListener { v, event ->
                    if (event != null) {
                        if (event.action == MotionEvent.ACTION_DOWN) {
                            val screenX = event.x
                            val screenY = event.y
                            v?.left?.let { it1 -> screenX.minus(it1) }
                            v?.top?.let { it1 -> screenY.minus(it1) }
                            viewX = screenX
                            viewY = screenY
                        }
                    }
                    val item = ClipData.Item("move_wall")
                    selectedTable = null
                    v.setBackgroundColor(Color.GREEN)
                    v.invalidate()
                    selectedWall = wall
                    val dragData = ClipData(
                        v?.tag as? CharSequence,
                        arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN),
                        item
                    )
                    dragData.addItem(ClipData.Item(wall.id.toString()))
                    dragData.addItem(ClipData.Item(wall.direction.toString()))
                    dragData.addItem(ClipData.Item(wall.width.toString()))
                    val shadowBuilder = MyDragShadowBuilder(floorWall.getWallImage(), viewX.toInt(), viewY.toInt())

                    v?.startDragAndDrop(
                        dragData,
                        shadowBuilder,
                        null,
                        0
                    )
                    true
                }
            }

        }

    }

    private fun loadSidebar(binding: FloorplanManagementFragmentBinding) {

        val sidebarWrapper = binding.sidebar
        val adapter = FloorplanSidebarAdapter()
        sidebarWrapper.adapter = adapter
        sidebarWrapper.numColumns = 2
        sidebarWrapper.horizontalSpacing = 15
        sidebarWrapper.verticalSpacing = 15
        sidebarWrapper.stretchMode = GridView.STRETCH_COLUMN_WIDTH
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is FloorplanTableListener) {
            tableListener = context
        } else {
            throw RuntimeException(requireContext().toString() + " must implement OnFragmentInteractionListener")
        }
    }

    private fun positionController(controllerType: ControllerType) {
        val self = this
        when (controllerType) {
            ControllerType.Left -> {
                selectedWall?.let { selectedWall?.id?.let { it1 -> viewModel.updateWall(it1, selectedWall!!.left.minus(2), selectedWall!!.top) } }
                selectedTable?.let { selectedTable?.id?.let { it1 -> viewModel.updateTable(it1, selectedTable!!.left.minus(2), selectedTable!!.top) } }
                loadTables(binding)
            }
            ControllerType.Right -> {
                selectedWall?.let { selectedWall?.id?.let { it1 -> viewModel.updateWall(it1, selectedWall!!.left.plus(2), selectedWall!!.top) } }
                selectedTable?.let { selectedTable?.id?.let { it1 -> viewModel.updateTable(it1, selectedTable!!.left.plus(2), selectedTable!!.top) } }
                loadTables(binding)
            }
            ControllerType.Top -> {
                selectedWall?.let { selectedWall?.id?.let { it1 -> viewModel.updateWall(it1, selectedWall!!.left, selectedWall!!.top.minus(2)) } }
                selectedTable?.let { selectedTable?.id?.let { it1 -> viewModel.updateTable(it1, selectedTable!!.left, selectedTable!!.top.minus(2)) } }
                loadTables(binding)
            }
            ControllerType.Bottom -> {
                selectedWall?.let { selectedWall?.id?.let { it1 -> viewModel.updateWall(it1, selectedWall!!.left, selectedWall!!.top.plus(2)) } }
                selectedTable?.let { selectedTable?.id?.let { it1 -> viewModel.updateTable(it1, selectedTable!!.left, selectedTable!!.top.plus(2)) } }
                loadTables(binding)
            }
            ControllerType.Property -> {
                if (selectedTable != null) {
                    activity?.supportFragmentManager?.let { it1 ->
                        val floorplanTableDialog = FloorplanTableDialog()
                        floorplanTableDialog.setParentFragment(self, binding)
                        val args = Bundle()
                        args.putParcelable("table", selectedTable)
                        floorplanTableDialog.arguments = args
                        floorplanTableDialog.show(it1, "Table Property")
                    }
                }
                if (selectedWall != null) {
                    activity?.supportFragmentManager?.let { it1 ->
                        val floorplanWallDialog = FloorplanWallDialog()
                        floorplanWallDialog.setParentFragment(self, binding)
                        val args = Bundle()
                        args.putParcelable("wall", selectedWall)
                        floorplanWallDialog.arguments = args
                        floorplanWallDialog.show(it1, "Wall Property")
                    }
                }
            }
        }
    }

    private fun rotateController(rotateDirection: Int) {
        when (rotateDirection) {
            1 -> {
                selectedTable?.let { selectedTable!!.rotate = selectedTable!!.rotate - 10 }
            }
            -1 -> {
                selectedTable?.let { selectedTable!!.rotate = selectedTable!!.rotate + 10 }
            }
        }

        selectedTable?.let { viewModel.updateTable(it) }
        loadTables(binding)
    }

    class MyDragShadowBuilder(v: View, x: Int, y: Int) : View.DragShadowBuilder(v) {
        private val touchX = x
        private val touchY = y
        override fun onProvideShadowMetrics(size: Point, touch: Point) {
            val width: Int = view.width
            val height: Int = view.height
            size.set(width, height)
            touch.set(touchX, touchY)
        }
    }

    private fun refreshSpinnerList(){
        val floorPlanNames = arrayListOf<String>()
        viewModel.floorplans.value?.forEach {
            if (it.name.length > 0) {
                floorPlanNames.add(it.name)
            } else {
                floorPlanNames.add("Untitled -> " + it.id)
            }
        }
        val floorPlansAdapter = activity?.let {
            ArrayAdapter(
                it.applicationContext,
                android.R.layout.simple_spinner_item,
                floorPlanNames
            )
        }
        floorPlansAdapter?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        floorplanSpinner!!.adapter = floorPlansAdapter
    }

    private fun initFloorplanSpinner() {
        val floorPlanNames = arrayListOf<String>()
        var isDisplayModal = true

        viewModel.floorplans.value?.forEach {
            if (it.name.length > 0) {
                floorPlanNames.add(it.name)
            } else {
                floorPlanNames.add("Untitled -> " + it.id)
            }
        }
        // Set layout to use when the list of choices appear
        val floorPlansAdapter = activity?.let {
            ArrayAdapter(
                it.applicationContext,
                android.R.layout.simple_spinner_item,
                floorPlanNames
            )
        }
        floorPlansAdapter?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        floorplanSpinner!!.adapter = floorPlansAdapter

        if (viewModel.getCurrentIndex() > -1) {
            Log.d("Floorplan Position_1", viewModel.getCurrentIndex().toString())
            floorplanSpinner!!.setSelection(viewModel.getCurrentIndex());
        }
        floorplanSpinner!!.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                viewModel.saveFloorplanToCloud()
                val floorplan = viewModel.floorplans.value?.get(id.toInt())
                if (floorplan != null){
                    viewModel.selectFloorplan(floorplan)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }
}
