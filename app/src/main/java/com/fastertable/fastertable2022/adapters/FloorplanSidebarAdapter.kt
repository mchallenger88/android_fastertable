package com.fastertable.fastertable2022.adapters

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipDescription
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.RelativeLayout
import com.fastertable.fastertable2022.R
import com.fastertable.fastertable2022.ui.floorplan_manage.FloorplanManagementFragment

class FloorplanSidebarAdapter: BaseAdapter() {
    private val tables = getShapes()
    override fun getCount(): Int {
        return tables.count()
    }

    override fun getItem(position: Int): Any {
        return tables[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val tableView = ImageView(parent?.context)
        tableView.setImageResource(tables[position])
        val  param: RelativeLayout.LayoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,100)
        val scale = parent?.context?.resources?.displayMetrics?.density
        scale?.let {
            param.width = (45.0f * scale + 0.5f).toInt()
            param.height =(45.0f * scale + 0.5f).toInt()
        }

        tableView.layoutParams = param
        var viewX = 0.0f
        var viewY = 0.0f
        tableView.setOnTouchListener { v, event ->
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
            val item = ClipData.Item(tables[position].toString())
            val dragData = ClipData(
                v.tag as? CharSequence,
                arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN),
                item
            )
            dragData.addItem(ClipData.Item(viewX.toString()))
            dragData.addItem(ClipData.Item(viewY.toString()))
            val shadowBuilder = FloorplanManagementFragment.MyDragShadowBuilder(
                tableView,
                viewX.toInt(),
                viewY.toInt()
            )

            v?.startDragAndDrop(
                dragData,
                shadowBuilder,
                null,
                0
            )
            true
        }

        return tableView
    }

    private fun getShapes(): List<Int> {
        return listOf(
            R.drawable.ic_booth,
            R.drawable.ic_rect_four,
            R.drawable.ic_rect_horz_eight,
            R.drawable.ic_rect_horz_four,
            R.drawable.ic_rect_horz_six,
            R.drawable.ic_rect_horz_ten,
            R.drawable.ic_rect_six,
            R.drawable.ic_rect_two,
            R.drawable.ic_round_bar_stool,
            R.drawable.ic_round_booth,
            R.drawable.ic_round_eight,
            R.drawable.ic_round_four,
            R.drawable.ic_round_ten,
            R.drawable.ic_round_two,
            R.drawable.ic_square_bar_stool,
            R.drawable.ic_square_four,
            R.drawable.ic_square_two,
            R.drawable.ic_wall
        )
    }
}