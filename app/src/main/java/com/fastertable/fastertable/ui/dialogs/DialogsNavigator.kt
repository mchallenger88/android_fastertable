package com.fastertable.fastertable.ui.dialogs

import androidx.fragment.app.FragmentManager
import javax.inject.Inject

class DialogsNavigator @Inject constructor(private val fragmentManager: FragmentManager) {

    fun showOrderNoteDialog() {
        fragmentManager.beginTransaction()
                .add(OrderNotesDialogFragment.newInstance(), null)
                .commitAllowingStateLoss()
    }
}