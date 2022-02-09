package com.fastertable.fastertable2022.common.base

import androidx.recyclerview.widget.RecyclerView


abstract class CheckboxListAdapter<T, VH : RecyclerView.ViewHolder> {

    internal interface OnItemCheckListener {
        fun onItemCheck(item: Any?)
        fun onItemUncheck(item: Any?)
    }
}