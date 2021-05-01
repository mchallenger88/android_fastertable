package com.fastertable.fastertable

import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.fastertable.fastertable.common.base.BaseActivity
import com.fastertable.fastertable.common.base.DismissListener
import com.fastertable.fastertable.data.models.OrderItem
import com.fastertable.fastertable.data.repository.LoginRepository
import com.fastertable.fastertable.data.repository.OrderRepository
import com.fastertable.fastertable.ui.dialogs.*
import com.fastertable.fastertable.ui.order.OrderViewModel
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity: BaseActivity(), DismissListener, DialogListener, ItemNoteListener {
    @Inject lateinit var loginRepository: LoginRepository
    @Inject lateinit var orderRepository: OrderRepository
    private lateinit var appBarConfiguration: AppBarConfiguration
    private val orderViewModel: OrderViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)

        setSupportActionBar(toolbar)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController: NavController = navHostFragment.navController

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        orderViewModel.sendKitchen.observe(this, {
            sendToKitchen()

        })

        orderViewModel.orderItemClicked.observe(this, {
            orderItemClicked(it)
        })

        orderViewModel.showOrderNote.observe(this, {
            if (it){
                OrderNotesDialogFragment().show(supportFragmentManager, OrderNotesDialogFragment.TAG)
            }
        })


        hideSystemUI()
    }


    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun sendToKitchen(){
        var send = false
        val settings = loginRepository.getSettings()!!
        var order = orderViewModel.liveOrder.value!!
        order.guests?.forEach { guest ->
            guest.orderItems?.forEach {
                if (it.status == "Started"){
                    send = true }}}

        if (send){
            if (settings.restaurantType == "Counter Service"){
                if (order.orderNumber == 99 && order.tableNumber == null && order.orderType == "Counter"){
                    AssignTableDialog().show(supportFragmentManager, AssignTableDialog.TAG)


//                    orderViewModel.saveOrderToCloud(order)
                    //TODO Send to Kitchen that is print kitchen ticket
                    //TODO Create a Payment and then Send to Payment Activity
                }else{
                    //TODO Send to Kitchen that is print kitchen ticket
                    //TODO Create a Payment and then Send to Payment Activity
                }
            }
            if (settings.restaurantType == "Full Service"){
                orderViewModel.saveOrderToCloud(order)
                //Send to Kitchen printing
                order.printKitchenTicket()
                orderRepository.clearNewOrder()
                //TODO: Clear new payment
                //TODO: Go Back to Home;
            }
        }

        if (!send){
            val view = findViewById<View>(R.id.nav_host_fragment)
            Snackbar.make(view, R.string.kitchen_warning_message, Snackbar.LENGTH_LONG).show()
        }
    }

    private fun hideSystemUI() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.let {

                it.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
        }
    }

    private fun orderItemClicked(item: OrderItem){
        ItemMoreBottomSheetDialog().show(supportFragmentManager, ItemMoreBottomSheetDialog.TAG)
    }

    override fun getReturnValue(value: String) {
        if (value != ""){
            orderViewModel.setTableNumber(value.toInt())
            orderViewModel.saveOrderToCloud(orderViewModel.liveOrder.value!!)
        }

    }

    override fun returnValue(value: String) {
        orderViewModel.actionOnItemClicked(value)
    }

    override fun returnNote(value: String) {
        orderViewModel.saveOrderNote(value)
    }

}

