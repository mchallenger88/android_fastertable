package com.fastertable.fastertable.ui.floorplan

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import com.fastertable.fastertable.R
import com.fastertable.fastertable.data.models.FloorplanWall
import com.fastertable.fastertable.data.models.WallDirection

class FloorWall @JvmOverloads
constructor(ctx: Context, attributeSet: AttributeSet? = null, private val defStyleAttr: Int = 0)
    : ConstraintLayout(ctx, attributeSet, defStyleAttr) {
    private var wallView: ImageView

    init {
        val inflater = ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        inflater.inflate(R.layout.wall, this)
        wallView = findViewById(R.id.wallView)
    }


    fun loadWall(wall: FloorplanWall) {
        val param = LayoutParams(LayoutParams.WRAP_CONTENT, 100);
        when (wall.direction) {
            "Vertical" -> {
                param.height = wall.width ?: 0
                param.width = wall.thickness ?: 10
            }
            else -> {
                param.width = wall.width ?: 0
                param.height = wall.thickness ?: 10
            }
        }
        wallView.layoutParams = param;
        wallView.setBackgroundColor(Color.rgb(72, 72, 72));
    }

    fun getWallImage(): ImageView {
        return wallView;
    }
}