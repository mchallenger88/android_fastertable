package com.fastertable.fastertable.ui.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fastertable.fastertable.data.Settings
import com.google.gson.Gson
import java.io.BufferedReader
import java.io.File

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val app: Application = application
    private val _text = MutableLiveData<String>().apply {
        value = "This is home Fragment"
    }
    val text: LiveData<String> = _text

    private val _settings = MutableLiveData<Settings>()
    val settings: LiveData<Settings>
        get() = _settings

    init{}
}