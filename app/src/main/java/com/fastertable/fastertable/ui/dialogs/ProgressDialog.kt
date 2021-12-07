package com.fastertable.fastertable.ui.dialogs


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment
import com.fastertable.fastertable.R


class ProgressDialog: DialogFragment() {
    private var statusText: TextView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.progress_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val msg = requireArguments().getString("msg")
        statusText = view.findViewById(R.id.statusText)
        statusText!!.text = msg
    }

    override fun onStart() {
        super.onStart()
        var width = 1000
        if (width > 1500) {
            width = 1500
        }
        dialog?.let {
            it.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }
}