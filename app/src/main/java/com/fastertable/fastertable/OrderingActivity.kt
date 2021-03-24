package com.fastertable.fastertable

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class OrderingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        var fasterToolbar: Toolbar = findViewById(R.id.fastertoolbar)
        toolbar.visibility = View.VISIBLE
        fasterToolbar.visibility = View.VISIBLE


    }
}