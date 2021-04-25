package com.fastertable.fastertable.ui.dialogs

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.fastertable.fastertable.R
import com.fastertable.fastertable.databinding.OrderNoteDialogBinding
import com.fastertable.fastertable.ui.order.OrderViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OrderNotesDialogFragment : BaseDialog() {
    private val viewModel: OrderViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = OrderNoteDialogBinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        binding.btnSaveOrderNote.setOnClickListener {
            close()
        }

        return binding.root
    }

    private fun close(){
        viewModel.saveOrderNote()
        dismiss()

    }

    override fun onStart() {
        super.onStart()
        val width = 800
        dialog!!.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        var viewParent = view
        while (viewParent is View) {
            viewParent.fitsSystemWindows = false
            viewParent.setOnApplyWindowInsetsListener { _, insets -> insets }
            viewParent = viewParent.parent as View?
        }
    }

    @SuppressLint("RestrictedApi")
    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            dialog.window?.setDecorFitsSystemWindows(false)
            dialog.window?.insetsController?.let {

                it.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }}else {
                @Suppress("DEPRECATION")
                dialog.window?.decorView?.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
            }
//        dialog?.window?.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

    }

    companion object {
        fun newInstance(): OrderNotesDialogFragment {
            return OrderNotesDialogFragment()
        }

        const val TAG = "OrderNotesDialogFragment"
    }
}