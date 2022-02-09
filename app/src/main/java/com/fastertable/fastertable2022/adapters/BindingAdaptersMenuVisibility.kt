package com.fastertable.fastertable2022.adapters

import android.content.res.ColorStateList
import android.view.View
import android.widget.ImageButton
import android.widget.ScrollView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.fastertable.fastertable2022.R
import com.fastertable.fastertable2022.ui.order.MenusNavigation

@BindingAdapter("menuCategoryVisibility")
fun setMenuCategoryVisibility(layout: ConstraintLayout?, nav: MenusNavigation){
    if (nav == MenusNavigation.CATEGORIES){
        layout?.visibility = View.VISIBLE
    }else{
        layout?.visibility = View.GONE
    }
}

@BindingAdapter("menuItemsTextViewVisibility")
fun setMenuItemsTextViewVisibility(textView: TextView?, nav: MenusNavigation){
    if (nav == MenusNavigation.MENU_ITEMS){
        textView?.visibility = View.VISIBLE
    }else{
        textView?.visibility = View.GONE
    }
}

@BindingAdapter("btnMenuBackVisibility")
fun setBtnMenuBackVisibility(button: ImageButton?, nav: MenusNavigation){
    if (button != null){
        val offWhite = ContextCompat.getColor(button.context, R.color.offWhite_background)
        val white = ContextCompat.getColor(button.context, R.color.white)
        when (nav){
            MenusNavigation.CATEGORIES -> {
                button.backgroundTintList = ColorStateList.valueOf(offWhite)
                button.visibility = View.GONE
            }
            MenusNavigation.MENU_ITEMS ->{
                button.backgroundTintList = ColorStateList.valueOf(offWhite)
                button.visibility = View.VISIBLE
            }
            MenusNavigation.MENU_ITEM -> {
                button.backgroundTintList = ColorStateList.valueOf(white)
                button.visibility = View.VISIBLE
            }

        }
    }



}

@BindingAdapter("setScrollViewVisibility")
fun scrollViewVisibility(scrollView: ScrollView?, nav: MenusNavigation){
    if (nav == MenusNavigation.MENU_ITEMS){
        scrollView?.visibility = View.VISIBLE
    }else{
        scrollView?.visibility = View.GONE
    }
}

@BindingAdapter("menuItemVisibility")
fun setMenuItemVisibility(layout: ConstraintLayout?, nav: MenusNavigation){
    if (nav == MenusNavigation.MENU_ITEM){
        layout?.visibility = View.VISIBLE
    }else{
        layout?.visibility = View.GONE
    }
}

@BindingAdapter("editMenuItemVisibility")
fun editMenuItemVisibility(layout: ConstraintLayout?, nav: MenusNavigation){
    if (nav == MenusNavigation.EDIT_MENU_ITEM){
        layout?.visibility = View.VISIBLE
    }else{
        layout?.visibility = View.GONE
    }
}

@BindingAdapter("menuItemTvVisibility")
fun setMenuItemTvVisibility(textView: TextView?, nav: MenusNavigation){
    if (nav == MenusNavigation.MENU_ITEM){
        textView?.visibility = View.VISIBLE
    }else{
        textView?.visibility = View.GONE
    }
}

@BindingAdapter("menuItemNoteVisibility")
fun setMenuItemNoteVisibility(button: ImageButton?, nav: MenusNavigation){
    if (nav == MenusNavigation.MENU_ITEM){
        button?.visibility = View.VISIBLE
    }else{
        button?.visibility = View.GONE
    }
}

@BindingAdapter("backgroundLayoutMenus")
fun backgroundLayoutMenus(layout: ConstraintLayout?, nav: MenusNavigation){
    layout?.let {
        val offWhite = ContextCompat.getColor(layout.context, R.color.offWhite_background)
        val white = ContextCompat.getColor(layout.context, R.color.white)
        if (nav == MenusNavigation.MENU_ITEM){
            layout.backgroundTintList = ColorStateList.valueOf(white)
        }else{
            layout.backgroundTintList = ColorStateList.valueOf(offWhite)
        }
    }

}