package com.fastertable.fastertable

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.ImageButton
import androidx.activity.viewModels
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.*
import com.fastertable.fastertable.common.base.BaseActivity
import com.fastertable.fastertable.common.base.BaseContinueDialog
import com.fastertable.fastertable.common.base.DismissListener
import com.fastertable.fastertable.data.models.OrderItem
import com.fastertable.fastertable.data.models.ReopenCheckoutRequest
import com.fastertable.fastertable.data.repository.LoginRepository
import com.fastertable.fastertable.data.repository.OrderRepository
import com.fastertable.fastertable.data.repository.PaymentRepository
import com.fastertable.fastertable.ui.checkout.AddTipFragmentDirections
import com.fastertable.fastertable.ui.checkout.CheckoutFragmentDirections
import com.fastertable.fastertable.ui.checkout.CheckoutViewModel
import com.fastertable.fastertable.ui.clockout.ClockoutViewModel
import com.fastertable.fastertable.ui.confirm.ConfirmViewModel
import com.fastertable.fastertable.ui.dialogs.*
import com.fastertable.fastertable.ui.home.HomeFragmentDirections
import com.fastertable.fastertable.ui.home.HomeViewModel
import com.fastertable.fastertable.ui.order.OrderFragmentDirections
import com.fastertable.fastertable.ui.order.OrderViewModel
import com.fastertable.fastertable.ui.payment.PaymentFragmentDirections
import com.fastertable.fastertable.ui.payment.PaymentViewModel
import com.fastertable.fastertable.ui.payment.ShowPayment
import com.fastertable.fastertable.utils.GlobalUtils
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity: BaseActivity(), DismissListener, DialogListener, ItemNoteListener, BaseContinueDialog.ContinueListener {
    @Inject lateinit var loginRepository: LoginRepository
    @Inject lateinit var orderRepository: OrderRepository
    @Inject lateinit var paymentRepository: PaymentRepository
    private lateinit var appBarConfiguration: AppBarConfiguration
    private val homeViewModel: HomeViewModel by viewModels()
    private val orderViewModel: OrderViewModel by viewModels()
    private val paymentViewModel: PaymentViewModel by viewModels()
    private val continueCancelViewModel: ContinueCancelViewModel by viewModels()
    private val checkoutViewModel: CheckoutViewModel by viewModels()
    private val clockoutViewModel: ClockoutViewModel by viewModels()
    private val confirmViewModel: ConfirmViewModel by viewModels()

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

        val exitButton: ImageButton = findViewById(R.id.exit_button)
        exitButton.setOnClickListener{
            exitUser()
        }

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home,
                R.id.approvalsFragment
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        homeViewModel.navigateToOrder.observe(this, { it ->
            if (it == "Counter"){
                orderViewModel.clearOrder()
                paymentViewModel.clearPayment()
                navController.navigate(HomeFragmentDirections.actionNavHomeToOrderFragment())
                homeViewModel.navigationEnd()
            }

            if (it.contains("O_")){
                orderViewModel.clearOrder()
                paymentViewModel.clearPayment()
                orderViewModel.setCurrentOrderId(it)
                navController.navigate(HomeFragmentDirections.actionNavHomeToOrderFragment())
                homeViewModel.navigationEnd()
            }
        })

        orderViewModel.sendKitchen.observe(this, {
            if (!checkLoginCheckoutStatus()){
                sendToKitchen()
            }else{
                continueCancelViewModel.setTitle("Checkout Error")
                continueCancelViewModel.setMessage("You have checked out. Click 'Continue' to reopen your checkout.")
                ContinueCancelFragment().show(supportFragmentManager, ContinueCancelFragment.TAG)
            }

        })

        orderViewModel.orderItemClicked.observe(this, {
            orderItemClicked(it)
        })

        orderViewModel.showOrderNote.observe(this, {
            if (it){
                OrderNotesDialogFragment().show(supportFragmentManager, OrderNotesDialogFragment.TAG)
            }
        })

        orderViewModel.paymentClicked.observe(this, {
            if (it){
               goToPayment(navController)
            }
        })

        paymentViewModel.amountOwed.observe(this, {
            paymentViewModel.livePayment.value?.tickets?.forEach { t ->
                if (t.uiActive){
                    if (t.paymentType == "Cash"){
                        CashBackDialogFragment().show(supportFragmentManager, CashBackDialogFragment.TAG)
                    }
                }
            }
        })

        paymentViewModel.ticketPaid.observe(this, { it ->
            if (it){
                paymentViewModel.savePaymentToCloud()
            }
        })

        paymentViewModel.livePayment.observe(this, {it ->
            if (it != null) {
                if (it.closed){
                    orderViewModel.closeOrder()
                }
            }
        })

        paymentViewModel.managePayment.observe(this, { it ->
            if (it){
                goToManagePayment(navController)
            }
        })

        paymentViewModel.error.observe(this, {it ->
            if (it){
                AlertDialogBottomSheet().show(supportFragmentManager, AlertDialogBottomSheet.TAG)
                paymentViewModel.clearError()
            }
        })

        paymentViewModel.showTicketMore.observe(this, {it ->
            if (it){
                TicketMoreBottomSheet().show(supportFragmentManager, TicketMoreBottomSheet.TAG)
            }
        })

        paymentViewModel.showTicketItemMore.observe(this, {it ->
            if (it){
                TicketItemMoreBottomSheet().show(supportFragmentManager, TicketItemMoreBottomSheet.TAG)
            }
        })

        checkoutViewModel.navToTip.observe(this, {
            if (it == "Tip"){
                navController.navigate(CheckoutFragmentDirections.actionCheckoutFragmentToAddTipFragment())
                checkoutViewModel.navigationEnd()
            }

            if (it.startsWith("O_")){
                orderViewModel.clearOrder()
                paymentViewModel.clearPayment()
                orderViewModel.setCurrentOrderId(it)
                navController.navigate(CheckoutFragmentDirections.actionCheckoutFragmentToOrderFragment())
                checkoutViewModel.navigationEnd()
            }

            if (it == "Checkout"){
                navController.navigate(AddTipFragmentDirections.actionAddTipFragmentToCheckoutFragment())
                checkoutViewModel.navigationEnd()
            }
        })

        checkoutViewModel.checkoutComplete.observe(this, {
            if (it != ""){
                CheckoutDialogBottomSheet().show(supportFragmentManager, CheckoutDialogBottomSheet.TAG)
            }
        })

        clockoutViewModel.clockedOut.observe(this, {
            if (it == true){
                exitUser()
            }
        })

        confirmViewModel.openCalendar.observe(this, {
            if (it){

            }
        })


        hideSystemUI()
    }


    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return item.onNavDestinationSelected(findNavController(R.id.nav_host_fragment))
    }

    private fun checkLoginCheckoutStatus(): Boolean{
        val user = loginRepository.getOpsUser()
        return user?.userClock?.checkout == true || user?.userClock?.checkoutApproved == true

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
                    //TODO Create a Payment and then Send to Payment Activity
                }else{
                    orderViewModel.saveOrderToCloud()
                    //TODO Create a Payment and then Send to Payment Activity
                }
            }
            if (settings.restaurantType == "Full Service"){
                orderViewModel.saveOrderToCloud()
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

    private fun goToPayment(navController: NavController){
        var okToPay = true
        val terminal = loginRepository.getTerminal()!!
        val order = orderViewModel.liveOrder.value!!

        lifecycleScope.launch{
            val p = paymentRepository.getPayment()
            val pid = order.id.replace("O_", "P_")
            if (p == null){
                paymentViewModel.getCloudPayment(pid, order.locationId)
            }else{
                if (p.id != pid){
                    paymentViewModel.getCloudPayment(pid, order.locationId)
                }
            }

            if (paymentViewModel.livePayment.value == null){
                val flatten = order.getAllOrderItems()
                if (flatten.size > 0){
                    order.guests?.forEach { guest ->
                        guest.orderItems?.forEach {
                            if (it.status == "Started"){
                                okToPay = false }}}

                    if (okToPay){
                        val payment = paymentRepository.createNewPayment(order, terminal)
                        paymentViewModel.setLivePayment(payment)
                        navController.navigate(OrderFragmentDirections.actionOrderFragmentToPaymentFragment())
                        orderViewModel.navToPayment(false)
                    }else{
                        val view = findViewById<View>(R.id.nav_host_fragment)
                        Snackbar.make(view, R.string.payment_warning_message1, Snackbar.LENGTH_LONG).show()
                    }
                }
            }else{
                navController.navigate(OrderFragmentDirections.actionOrderFragmentToPaymentFragment())
                orderViewModel.navToPayment(false)
            }
        }
    }

    private fun goToManagePayment(navController: NavController){
        navController.navigate(PaymentFragmentDirections.actionPaymentFragmentToPaymentSplitFragment())
        paymentViewModel.setManagePayment()
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
            orderViewModel.saveOrderToCloud()
        }else{
            orderViewModel.saveOrderToCloud()
        }

    }

    override fun returnContinue(value: String){
        when (value){
            "Checkout Error" -> {
                //Reopen Checkout then Send to Kitchen
                checkoutViewModel.reopenCheckout()
                sendToKitchen()
            }
        }
    }

    override fun returnValue(value: String) {
        when (value){
            "Void Ticket" -> {
                return paymentViewModel.voidTicket(orderViewModel.liveOrder.value!!)
            }
            "Discount" -> {
                return paymentViewModel.setPaymentScreen(ShowPayment.DISCOUNT, "Discount Ticket")
            }
            "Void Item" -> {
                return paymentViewModel.voidTicketItem(orderViewModel.liveOrder.value!!)
            }
            "Discount Item" -> {
                return paymentViewModel.setPaymentScreen(ShowPayment.DISCOUNT, "Discount Item")
            }
            "Modify Price" -> {
                return paymentViewModel.setPaymentScreen(ShowPayment.MODIFY_PRICE, null)
            }
            "Delete" -> {
                return orderViewModel.actionOnItemClicked(value)
            }
            "Toggle Rush" -> {
                return orderViewModel.actionOnItemClicked(value)
            }
            "Toggle No Make" -> {
                return orderViewModel.actionOnItemClicked(value)
            }
            "Toggle Takeout" -> {
                return orderViewModel.actionOnItemClicked(value)
            }
        }

    }

    override fun returnNote(value: String) {
        orderViewModel.saveOrderNote(value)
    }

    fun exitUser(){
        loginRepository.clearUser()
        val intent = Intent(this, LoginActivity::class.java)
        intent.putExtra("fragmentToLoad", "User")
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

}

