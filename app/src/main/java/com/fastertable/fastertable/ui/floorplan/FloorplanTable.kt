package com.fastertable.fastertable.ui.floorplan

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.MotionEventCompat
import androidx.core.view.get
import com.fastertable.fastertable.R
import com.fastertable.fastertable.data.models.IdLocation
import com.fastertable.fastertable.data.models.RestaurantTable
import com.fastertable.fastertable.data.models.TableType

interface FloorplanTableListener{
    fun onClick(table: RestaurantTable)
}

private const val DEBUG_TAG = "Gestures"

class FloorplanTable  @JvmOverloads
constructor(private val ctx: Context, private val attributeSet: AttributeSet? = null, private val defStyleAttr: Int = 0)
    : ConstraintLayout(ctx, attributeSet, defStyleAttr) {
    lateinit var restaurantTable: RestaurantTable
    private var tableId: TextView
    private var tableImage: ImageView
    private var tableImages: LinearLayout
    private lateinit var tableImageList: ArrayList<ImageView>
    private val idWidthUnit = 15.0f

    init {
        val inflater = ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.table, this)
        tableId = findViewById(R.id.txt_table_id)
        tableImage = findViewById(R.id.img_table)
        tableImages = findViewById(R.id.tableImages)
    }

    fun getTableImage() : ImageView {
        return tableImage;
    }

    fun getTableImageList() : ArrayList<ImageView> {
        return tableImageList
    }


    fun loadTable(table: RestaurantTable){
        restaurantTable = table
        tableId.text = table.id.toString()
        val idLength = tableId.text.length

        val scale = resources.displayMetrics.density
        val param: RelativeLayout.LayoutParams = RelativeLayout.LayoutParams((100.0f * scale + 0.5f).toInt() ,(20.0f * scale + 0.5f).toInt());
        val idWidth = (idWidthUnit * idLength * scale + 0.5f).toInt()
        val idHeight = (20.0f * scale + 0.5f).toInt()
        var tableCnt = 1
        if (table.combinationTables != null){
            if (table.combinationTables?.size!! > 0) {
                tableCnt = table.combinationTables!!.size
            }
        }

        val imageWidth = ((idWidthUnit * 2 * idLength + 60.0f * tableCnt) * scale + 0.5f).toInt()
        val imageHeight = ((100.0f) * scale + 0.5f).toInt()

        param.width = idWidth
        param.height = idHeight
        val paddingY = idHeight
        val paddingX = idWidth
        val tableImageLayoutParam = RelativeLayout.LayoutParams(((60.0f + idLength * idWidthUnit * 2) * scale + 0.5f).toInt() ,(100.0f * scale + 0.5f).toInt())
        tableImages.setPadding(paddingX, paddingY, paddingX, paddingY)

        tableImages.layoutParams = tableImageLayoutParam

        tableId.layoutParams = param
        when (table.id_location) {
            "BottomLeft" -> {
                param.setMargins(0, (imageHeight - paddingY),imageWidth - paddingX, 0)
                tableId.textAlignment = TEXT_ALIGNMENT_TEXT_END
            }
            "BottomRight" -> {
                param.setMargins(imageWidth - paddingX, (imageHeight - paddingY),0, 0)
                tableId.textAlignment = TEXT_ALIGNMENT_TEXT_START;
            }
            "MiddleCenter" -> {
                param.setMargins((imageWidth - idWidth) / 2, (imageHeight - idHeight) / 2,(imageWidth - idWidth) / 2, (imageHeight - idHeight) / 2)
            }
            "MiddleLeft" -> {
                param.setMargins(0, (imageHeight - idHeight) / 2,imageWidth - paddingX, (imageHeight - idHeight) / 2)
                tableId.textAlignment = TEXT_ALIGNMENT_TEXT_END;
            }
            "MiddleRight" -> {
                param.setMargins((imageWidth - paddingX), (imageHeight - idHeight) / 2,0, (imageHeight - idHeight) / 2)
                tableId.textAlignment = TEXT_ALIGNMENT_TEXT_START;
            }
            "TopCenter" -> {
                param.setMargins((imageWidth - idWidth) / 2 , 0,(imageWidth - idWidth) / 2, (imageHeight - paddingY))
            }
            "TopLeft" -> {
                param.setMargins(0, 0,imageWidth - paddingX, (imageHeight - paddingY))
                tableId.textAlignment = TEXT_ALIGNMENT_TEXT_END;
            }
            "TopRight" -> {
                param.setMargins((imageWidth - paddingX), 0,0, (imageHeight - paddingY))
                tableId.textAlignment = TEXT_ALIGNMENT_TEXT_START;
            }
            else -> param.setMargins((imageWidth - idWidth) / 2, (imageHeight - paddingY),(imageWidth - idWidth) / 2, 0)

        }
        getTableImage(table)
    }

    fun loadReservation(table: RestaurantTable, lastName: String) {
        tableId.text = lastName
        val param: RelativeLayout.LayoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT)
        val scale = resources.displayMetrics.density

        val imageHeight = ((100.0f) * scale + 0.5f).toInt()
        if (!table.isCombination) {
            param.width = (120.0f * scale + 0.5f).toInt()
        } else {
            val tableCnt = table.combinationTables?.count();
            param.width = ((60.0f + 60.0f * tableCnt!!) * scale + 0.5f).toInt()
        }
        param.height =  (40.0f * scale + 0.5f).toInt()
        val paddingY = (20.0f * scale + 0.5f).toInt()
        tableId.layoutParams = param

        param.setMargins(0, (imageHeight - paddingY),0, 0)
        getTableImage(table)
    }

    fun setTableColorFilter(color: Int) {
        val count = tableImages.childCount - 1
        for(index in 0..count) {
            val imageView = tableImages.get(index)
            (imageView as ImageView).setColorFilter(color)
        }
    }

    fun clearTableColorFilter() {
        val count = tableImages.childCount - 1
        for(index in 0..count) {
            val imageView = tableImages.get(index)
            (imageView as ImageView).clearColorFilter()
        }
    }

    private fun getTableImage(table: RestaurantTable){
        val tables: ArrayList<RestaurantTable>
        if (table.isCombination) {
            tables = table.combinationTables!!
        } else {
            tables = arrayListOf(table)
        }
        tableImageList = arrayListOf()
        var index = 0
        tables.forEach {
            index++
            run {
                val tableType = it.type
                var resourceId = R.drawable.ic_booth;
                when (tableType) {
                    "Booth" -> resourceId =
                        if (!table.locked) R.drawable.ic_booth else R.drawable.ic_booth_locked
                    "Round_Two" -> resourceId =
                        if (!table.locked) R.drawable.ic_round_two else R.drawable.ic_round_two_locked
                    "Round_Four" -> resourceId =
                        if (!table.locked) R.drawable.ic_round_four else R.drawable.ic_round_four_locked
                    "Round_Eight" -> resourceId =
                        if (!table.locked) R.drawable.ic_round_eight else R.drawable.ic_round_eight_locked
                    "Round_Ten" -> resourceId =
                        if (!table.locked) R.drawable.ic_round_ten else R.drawable.ic_round_ten_locked
                    "Round_Stool" -> resourceId =
                        if (!table.locked) R.drawable.ic_round_bar_stool else R.drawable.ic_round_bar_stool_locked
                    "Round_Booth" -> resourceId =
                        if (!table.locked) R.drawable.ic_round_booth else R.drawable.ic_round_booth_locked
                    "Rect_Two" -> resourceId =
                        if (!table.locked) R.drawable.ic_rect_two else R.drawable.ic_rect_two_locked
                    "Rect_Four" -> resourceId =
                        if (!table.locked) R.drawable.ic_rect_four else R.drawable.ic_rect_four_locked
                    "Rect_Six" -> resourceId =
                        if (!table.locked) R.drawable.ic_rect_six else R.drawable.ic_rect_six_locked
                    "Rect_Horz_Six" -> resourceId =
                        if (!table.locked) R.drawable.ic_rect_horz_six else R.drawable.ic_rect_horz_six_locked
                    "Rect_Horz_Eight" -> resourceId =
                        if (!table.locked) R.drawable.ic_rect_horz_eight else R.drawable.ic_rect_horz_eight_locked
                    "Rect_Horz_Four" -> resourceId =
                        if (!table.locked) R.drawable.ic_rect_horz_four else R.drawable.ic_rect_horz_four_locked
                    "Rect_Horz_Ten" -> resourceId =
                        if (!table.locked) R.drawable.ic_rect_horz_ten else R.drawable.ic_rect_horz_ten_locked
                    "Square_Two" -> resourceId =
                        if (!table.locked) R.drawable.ic_square_two else R.drawable.ic_square_two_locked
                    "Square_Four" -> resourceId =
                        if (!table.locked) R.drawable.ic_square_four else R.drawable.ic_square_four_locked
                    "Square_Stool" -> resourceId =
                        if (!table.locked) R.drawable.ic_square_bar_stool else R.drawable.ic_square_bar_stool_locked
                }
                if (index == 1) {
                    tableImage.setImageResource(resourceId)
                    tableImageList.add(tableImage);
                } else {
                    val imageView = ImageView(context)
                    tableImageList.add(imageView);
                    val scale = resources.displayMetrics.density
                    imageView.layoutParams =
                        LinearLayout.LayoutParams((60.0f * scale + 0.5f).toInt(), (60.0f * scale + 0.5f).toInt()) // value is in pixels
                    imageView.setImageResource(resourceId);
                    tableImages.addView(imageView)
                }
            }
        }
        tableImage.rotation = table.rotate.toFloat()
    }


}

