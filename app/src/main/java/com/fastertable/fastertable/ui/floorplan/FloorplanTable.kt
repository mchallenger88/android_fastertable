package com.fastertable.fastertable.ui.floorplan

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.fastertable.fastertable.R
import com.fastertable.fastertable.data.models.IdLocation
import com.fastertable.fastertable.data.models.RestaurantTable
import com.fastertable.fastertable.data.models.TableType


class FloorplanTable  @JvmOverloads
constructor(private val ctx: Context, private val attributeSet: AttributeSet? = null, private val defStyleAttr: Int = 0)
    : ConstraintLayout(ctx, attributeSet, defStyleAttr) {
    lateinit var restaurantTable: RestaurantTable
    private var tableId: TextView
    private var tableImage: ImageView

    init {

        // get the inflater service from the android system
        val inflater = ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        // inflate the layout into "this" component
        inflater.inflate(R.layout.table, this)
        tableId = findViewById(R.id.txt_table_id)
        tableImage = findViewById(R.id.img_table)

    }



    fun loadTable(table: RestaurantTable){
        restaurantTable = table
        tableId.text = table.id.toString()
        getTableImage(table)
    }

    private fun getTableImage(table: RestaurantTable){
        if (!table.locked){
            when (table.type){
                TableType.Round_Booth -> tableImage.setImageResource(R.drawable.ic_round_booth)
                TableType.Booth -> tableImage.setImageResource(R.drawable.ic_booth)
                else -> tableImage.setImageResource(R.drawable.ic_booth)
            }
        }

        if (table.locked){
            when (table.type){
                TableType.Round_Booth -> tableImage.setImageResource(R.drawable.ic_round_booth_locked)
                TableType.Booth -> tableImage.setImageResource(R.drawable.ic_booth_locked)
                else -> tableImage.setImageResource(R.drawable.ic_booth_locked)
            }
        }

    }

    private fun setTableId(table: RestaurantTable){
        val currentLayout = tableId.layoutParams as ConstraintLayout.LayoutParams

        if (table.id_location == IdLocation.TopCenter){
            currentLayout.bottomToTop = R.id.img_table
            currentLayout.startToStart = R.id.img_table
            currentLayout.marginStart = 17
        }
        tableId.layoutParams = currentLayout
    }

}

interface FloorplanTableListener{
    fun onClick(table: FloorplanTable)
}