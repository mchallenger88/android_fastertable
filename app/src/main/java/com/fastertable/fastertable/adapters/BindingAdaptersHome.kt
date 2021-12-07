package com.fastertable.fastertable.adapters

import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.fastertable.fastertable.R
import com.google.android.material.chip.Chip


    @BindingAdapter("btnOpenOrders")
    fun btnOpenOrders(chip: Chip, filter: String){
        val primary = R.color.primaryColor
        val secondary = R.color.secondaryColor
        chip.setTextAppearance(R.style.ChipTextAppearance)
        if (filter == "Open"){
            chip.setChipBackgroundColorResource(secondary)
        }else{
            chip.setChipBackgroundColorResource(primary)
        }
    }

    @BindingAdapter("btnClosedOrders")
    fun btnClosedOrders(chip: Chip, filter: String){
        val primary = R.color.primaryColor
        val secondary = R.color.secondaryColor
        chip.setTextAppearance(R.style.ChipTextAppearance)
        if (filter == "Closed"){
            chip.setChipBackgroundColorResource(secondary)
        }else{
            chip.setChipBackgroundColorResource(primary)
        }
    }

    @BindingAdapter("btnAllOrders")
    fun btnAllOrders(chip: Chip, filter: String){
        val primary = R.color.primaryColor
        val secondary = R.color.secondaryColor
        chip.setTextAppearance(R.style.ChipTextAppearance)
        if (filter == "All"){
            chip.setChipBackgroundColorResource(secondary)
        }else{
            chip.setChipBackgroundColorResource(primary)
        }
    }
