package com.fastertable.fastertable2022

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.widget.Toolbar
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.*
import com.fastertable.fastertable2022.common.base.BaseActivity
import com.fastertable.fastertable2022.common.base.BaseContinueDialog
import com.fastertable.fastertable2022.common.base.DismissListener
import com.fastertable.fastertable2022.data.models.*
import com.fastertable.fastertable2022.data.repository.GetPayment
import com.fastertable.fastertable2022.data.repository.LoginRepository
import com.fastertable.fastertable2022.data.repository.OrderRepository
import com.fastertable.fastertable2022.data.repository.PaymentRepository
import com.fastertable.fastertable2022.ui.approvals.ApprovalsFragmentDirections
import com.fastertable.fastertable2022.ui.approvals.ApprovalsViewModel
import com.fastertable.fastertable2022.ui.approvals.CompletedApprovalsFragmentDirections
import com.fastertable.fastertable2022.ui.approvals.CompletedApprovalsViewModel
import com.fastertable.fastertable2022.ui.checkout.AddTipFragmentDirections
import com.fastertable.fastertable2022.ui.checkout.CheckoutFragmentDirections
import com.fastertable.fastertable2022.ui.checkout.CheckoutViewModel
import com.fastertable.fastertable2022.ui.clockout.ClockoutViewModel
import com.fastertable.fastertable2022.ui.confirm.ConfirmViewModel
import com.fastertable.fastertable2022.ui.dialogs.*
import com.fastertable.fastertable2022.ui.error.ErrorViewModel
import com.fastertable.fastertable2022.ui.floorplan.FloorplanFragmentDirections
import com.fastertable.fastertable2022.ui.floorplan.FloorplanTableListener
import com.fastertable.fastertable2022.ui.floorplan.FloorplanViewModel
import com.fastertable.fastertable2022.ui.floorplan_manage.FloorplanManageViewModel
import com.fastertable.fastertable2022.ui.gift.GiftCardViewModel
import com.fastertable.fastertable2022.ui.home.HomeFragmentDirections
import com.fastertable.fastertable2022.ui.home.HomeViewModel
import com.fastertable.fastertable2022.ui.order.*
import com.fastertable.fastertable2022.ui.payment.*
import com.fastertable.fastertable2022.ui.takeout.TakeoutFragmentDirections
import com.fastertable.fastertable2022.ui.takeout.TakeoutViewModel
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity: BaseActivity(), DismissListener, DialogListener, DateListener,
    FloorplanTableListener, BaseContinueDialog.ContinueListener {
    @Inject lateinit var loginRepository: LoginRepository
    @Inject lateinit var orderRepository: OrderRepository
    @Inject lateinit var paymentRepository: PaymentRepository
    @Inject lateinit var getPayment: GetPayment
    private lateinit var appBarConfiguration: AppBarConfiguration
    private var settings: Settings? = null
    private val homeViewModel: HomeViewModel by viewModels()
    private val orderViewModel: OrderViewModel by viewModels()
    private val paymentViewModel: PaymentViewModel by viewModels()
    private val continueCancelViewModel: ContinueCancelViewModel by viewModels()
    private val checkoutViewModel: CheckoutViewModel by viewModels()
    private val clockoutViewModel: ClockoutViewModel by viewModels()
    private val confirmViewModel: ConfirmViewModel by viewModels()
    private val giftCardViewModel: GiftCardViewModel by viewModels()
    private val errorViewModel: ErrorViewModel by viewModels()
    private val floorplanViewModel: FloorplanViewModel by viewModels()
    private val takeoutViewModel: TakeoutViewModel by viewModels()
    private val datePickerViewModel: DatePickerViewModel by viewModels()
    private val transferOrderViewModel: TransferOrderViewModel by viewModels()
    private val floorplanManageViewModel: FloorplanManageViewModel by viewModels()
    private val approvalsViewModel: ApprovalsViewModel by viewModels()
    private val completedApprovalsViewModel: CompletedApprovalsViewModel by viewModels()
    private val editItemViewModel: EditItemDialogViewModel by viewModels()
    private val splitPaymentViewModel: SplitPaymentViewModel by viewModels()
    private var progressDialog: ProgressDialog? = null
    private var user: OpsAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        settings = loginRepository.getSettings()
        user = loginRepository.getOpsUser()
        setSupportActionBar(toolbar)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        val exitButton: ImageButton = findViewById(R.id.exit_button)
        exitButton.setOnClickListener{
            exitUser()
        }

        val floorplanMenu = navView.menu.findItem(R.id.floorplanManagementFragment)
        val permission = Permissions.viewOrders.toString()
        val manager = user?.claims?.find { it.permission.name == permission && it.permission.value }
        manager?.let {
            floorplanMenu.isVisible = user?.claims?.contains(it) == true
        }

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home,
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        homeObservables(navController)
        orderObservables(navController)
        paymentObservables(navController)
        checkoutObservables(navController)
        floorplanObservables(navController)
        takeoutObservables(navController)
        giftCardObservables()
        datePickerObservables()
        transferOrderObservables(navController)
        floorplanManageObservables()
        approvalsObservables(navController)

        hideSystemUI()
    }

    private fun homeObservables(navController: NavController){
        homeViewModel.navigateToOrder.observe(this) { it ->
            if (it == "Counter") {
                orderViewModel.clearOrder()
                paymentViewModel.clearPayment()
                navController.navigate(HomeFragmentDirections.actionNavHomeToOrderFragment())
                homeViewModel.navigationEnd()
            }

            if (it.contains("O_")) {
                orderViewModel.clearOrder()
                paymentViewModel.clearPayment()
                orderViewModel.setCurrentOrderId(it)
                navController.navigate(HomeFragmentDirections.actionNavHomeToOrderFragment())
                homeViewModel.navigationEnd()
            }
        }

        homeViewModel.navigateToFloorplan.observe(this) {
            if (it) {
                floorplanViewModel.getFloorplans()
                navController.navigate(HomeFragmentDirections.actionNavHomeToFloorplanFragment())
                homeViewModel.setNavigateToFloorPlan(false)
            }
        }

        homeViewModel.navigateToTakeout.observe(this) {
            if (it) {
                navController.navigate(HomeFragmentDirections.actionNavHomeToTakeoutFragment())
                homeViewModel.setNavigateToTakeout(false)
            }
        }

        homeViewModel.navigateToPayment.observe(this) {
            if (it.contains("O_")) {
                orderViewModel.clearOrder()
                paymentViewModel.clearPayment()
                orderViewModel.setCurrentOrderId(it)

                paymentViewModel.setCurrentPayment(it.replace("O", "P"))
                navController.navigate(HomeFragmentDirections.actionNavHomeToPaymentFragment())
                homeViewModel.navigationEnd()
            }
        }

        homeViewModel.navigateToVoid.observe(this) {
            if (it.contains("O_")) {
                orderViewModel.clearOrder()
                paymentViewModel.clearPayment()
                lifecycleScope.launch {
                    checkActivePayment(it, navController)
                }

            }
        }
    }

    private suspend fun checkActivePayment(orderId: String, navController: NavController){
        var payment: Payment? = null
        orderViewModel.setCurrentOrderId(orderId)
        val job = lifecycleScope.launch{
            payment = paymentRepository.getPayment()
        }

        job.join()
        if (payment != null){
            paymentViewModel.setCurrentPayment(orderId.replace("O", "P"))
            navController.navigate(HomeFragmentDirections.actionNavHomeToVoidStartFragment())
        }else{
            navController.navigate(HomeFragmentDirections.actionNavHomeToOrderFragment())
        }

        homeViewModel.navigationEnd()
    }

    private fun floorplanObservables(navController: NavController){
        floorplanViewModel.navigateToOrder.observe(this) {
            if (it == "Table") {
                orderViewModel.clearOrder()
                paymentViewModel.clearPayment()
                navController.navigate(FloorplanFragmentDirections.actionFloorplanFragmentToOrderFragment())
                homeViewModel.navigationEnd()
            }

            if (it.contains("O_")) {
                orderViewModel.clearOrder()
                paymentViewModel.clearPayment()
                orderViewModel.setCurrentOrderId(it)
                navController.navigate(FloorplanFragmentDirections.actionFloorplanFragmentToOrderFragment())
                homeViewModel.navigationEnd()
            }
        }
    }

    private fun takeoutObservables(navController: NavController){
        takeoutViewModel.navigateToOrder.observe(this) {
            if (it == "Takeout") {
                orderViewModel.clearOrder()
                paymentViewModel.clearPayment()
                navController.navigate(TakeoutFragmentDirections.actionTakeoutFragmentToOrderFragment())
                homeViewModel.navigationEnd()
            }
        }
    }

    private fun orderObservables(navController: NavController){
        orderViewModel.showTableDialog.observe(this) {
            if (it) {
                if (!checkLoginCheckoutStatus()) {
                    AssignTableDialog().show(supportFragmentManager, AssignTableDialog.TAG)
                } else {
                    continueCancelViewModel.setTitle("Checkout Error")
                    continueCancelViewModel.setMessage("You have checked out. Click 'Continue' to reopen your checkout.")
                    ContinueCancelFragment().show(
                        supportFragmentManager,
                        ContinueCancelFragment.TAG
                    )
                }
            }
        }

        orderViewModel.noOrderItems.observe(this) {
            val view = findViewById<View>(R.id.nav_host_fragment)
            Snackbar.make(view, R.string.kitchen_warning_message, Snackbar.LENGTH_LONG).show()
        }

        orderViewModel.sendKitchen.observe(this) {
            if (!checkLoginCheckoutStatus()) {
//                sendToKitchen()
            } else {
                continueCancelViewModel.setTitle("Checkout Error")
                continueCancelViewModel.setMessage("You have checked out. Click 'Continue' to reopen your checkout.")
                ContinueCancelFragment().show(supportFragmentManager, ContinueCancelFragment.TAG)
            }
        }

        orderViewModel.orderItemClicked.observe(this) {
            orderItemClicked(it)
        }

        orderViewModel.showOrderNote.observe(this) {
            if (it) {
                OrderNotesDialogFragment().show(
                    supportFragmentManager,
                    OrderNotesDialogFragment.TAG
                )
            }
        }

        orderViewModel.paymentClicked.observe(this) {
            if (it) {
                goToPayment(navController)
            }
        }

        orderViewModel.orderMore.observe(this) {
            if (it) {
                OrderMoreDialog().show(supportFragmentManager, OrderMoreDialog.TAG)
                orderViewModel.setOpenMore(false)
            }
        }

        orderViewModel.showTransfer.observe(this) {
            if (it) {
                orderViewModel.activeOrder.value?.id?.let { id ->
                    navController.navigate(OrderFragmentDirections.actionOrderFragmentToTransferOrderFragment(id))
                    hideUpButton()
                }
            }
        }

        orderViewModel.drinkList.observe(this) {
            if (it.isNotEmpty()) {
                ReorderDrinksDialogFragment().show(
                    supportFragmentManager,
                    ReorderDrinksDialogFragment.TAG
                )
            }
        }

        orderViewModel.editItem.observe(this) {
            if (it != null) {
                EditItemDialogFragment().show(supportFragmentManager, EditItemDialogFragment.TAG)
            }
        }

        orderViewModel.error.observe(this) { it ->
            if (it) {
                errorViewModel.setMessage(orderViewModel.errorMessage.value ?: "")
                errorViewModel.setTitle(orderViewModel.errorTitle.value ?: "")

                ErrorAlertBottomSheet().show(supportFragmentManager, ErrorAlertBottomSheet.TAG)
                orderViewModel.clearError()
            }
        }

        orderViewModel.activeOrderSet.observe(this) {
            if (it) {
                orderViewModel.activeOrder.value?.let { o ->
                    if (o.closeTime == null) {
                        navController.navigate(HomeFragmentDirections.actionNavHomeToOrderFragment())
                        homeViewModel.navigationEnd()
                    } else if (o.closeTime != null) {
                        goToPayment(navController)
                    }
                }
            }
        }

        orderViewModel.kitchenButtonEnabled.observe(this) {
            if (it != null && !it) {
                alertMessage("Your order has been sent to the kitchen!")
            }
        }

        orderViewModel.transferComplete.observe(this) {
            if (it) {
                navController.navigate(TransferItemFragmentDirections.actionTransferItemFragmentToNavHome())
                orderViewModel.setTransferComplete(false)
            }
        }

        orderViewModel.activeOrder.observe(this) {
            homeViewModel.getOrdersFromFile()
        }

        orderViewModel.navHome.observe(this) {
            if (it) {
                navController.navigate(OrderFragmentDirections.actionOrderFragmentToNavHome())
                orderViewModel.setNavHome(false)
            }
        }

        orderViewModel.navOrder.observe(this){
            if (it){
                navController.navigate(TransferItemFragmentDirections.actionTransferItemFragmentToOrderFragment())
                orderViewModel.navToOrder(false)
            }
        }
    }

    private fun paymentObservables(navController: NavController){
        paymentViewModel.amountOwed.observe(this) {
            paymentViewModel.activePayment.value?.tickets?.forEach { t ->
                if (t.uiActive) {
                    val pt = t.paymentList?.last()
                    if (pt?.paymentType == "Cash") {
                        CashBackDialogFragment().show(
                            supportFragmentManager,
                            CashBackDialogFragment.TAG
                        )
                    }
                }
            }
        }

        paymentViewModel.activePayment.observe(this) { it ->
            if (it != null) {
                homeViewModel.getOrdersFromFile()
                if (it.closed && orderViewModel.activeOrder.value != null && orderViewModel.activeOrder.value?.closeTime == null) {
                    orderViewModel.closeOrder()
                }
            }
        }

        paymentViewModel.managePayment.observe(this) { it ->
            if (it) {
                goToManagePayment(navController)
            }
        }

        paymentViewModel.error.observe(this) { it ->
            if (it) {
                AlertDialogBottomSheet().show(supportFragmentManager, AlertDialogBottomSheet.TAG)
                paymentViewModel.clearError()
            }
        }

        paymentViewModel.showTicketMore.observe(this) { it ->
            if (it) {
                TicketMoreBottomSheet().show(supportFragmentManager, TicketMoreBottomSheet.TAG)
            }
        }

        paymentViewModel.showTicketItemMore.observe(this) { it ->
            if (it) {
                TicketItemMoreBottomSheet().show(
                    supportFragmentManager,
                    TicketItemMoreBottomSheet.TAG
                )
            }
        }

        paymentViewModel.navigateToPayment.observe(this) {
            if (it) {
                navController.navigate(SplitPaymentFragmentDirections.actionPaymentSplitFragmentToPaymentFragment())
            }
        }

        paymentViewModel.navigateToHome.observe(this) {
            if (it) {
                homeViewModel.getOrdersFromFile()
                navController.navigate(PaymentFragmentDirections.actionPaymentFragmentToNavHome())
                paymentViewModel.setReturnHome(false)
            }
        }

        paymentViewModel.voidPayment.observe(this) {
            if (it) {
                navController.navigate(PaymentFragmentDirections.actionPaymentFragmentToVoidFragment())
                paymentViewModel.setVoidPayment(false)
            }
        }

        paymentViewModel.voidStart.observe(this) {
            if (it) {
                navController.navigate(VoidStartFragmentDirections.actionVoidStartFragmentToVoidFragment())
                paymentViewModel.setVoidStart(false)
            }
        }

        paymentViewModel.navigateToDashboardFromVoid.observe(this) {
            if (it) {
                navController.navigate(VoidFragmentDirections.actionVoidFragmentToNavHome())
                paymentViewModel.setReturnDashboardFromVoid(false)
            }
        }

        paymentViewModel.navigateToHomeFromVoid.observe(this) {
            if (it) {
                navController.navigate(VoidStartFragmentDirections.actionVoidStartFragmentToNavHome())
                paymentViewModel.setReturnHomeFromVoidStart(false)
            }
        }

        paymentViewModel.approvalSaved.observe(this) {
            if (it) {
                lifecycleScope.launch {
                    approvalsViewModel.loadApprovals()
                }
            }
        }

        paymentViewModel.splitEvenlyDialog.observe(this) {
            if (it) {
                AddGuestEvenSplitDialogFragment().show(
                    supportFragmentManager,
                    AddGuestEvenSplitDialogFragment.TAG
                )
                paymentViewModel.setSplitEvenlyDialog(false)
            }
        }

        paymentViewModel.splitAdhocDialog.observe(this) {
            if (it) {
                navController.navigate(SplitPaymentFragmentDirections.actionPaymentSplitFragmentToAdhocPaymentFragment())
                AddGuestAdhocSplitDialogFragment().show(
                    supportFragmentManager,
                    AddGuestAdhocSplitDialogFragment.TAG
                )
                paymentViewModel.setAdhocSplitDialog(false)
            }
        }

        splitPaymentViewModel.navigatePayment.observe(this) {
            if (it) {
                navController.navigate(AdhocPaymentFragmentDirections.actionAdhocPaymentFragmentToPaymentFragment())
                splitPaymentViewModel.setNavigatePayment(false)
            }
        }

        paymentViewModel.homeOrderListUpdate.observe(this) {
            if (it) {
                homeViewModel.getOrdersFromFile()
                paymentViewModel.setHomeListUpdate(false)
            }
        }
    }

    private fun checkoutObservables(navController: NavController){
        checkoutViewModel.navToTip.observe(this) {
            if (it == "Tip") {
                navController.navigate(CheckoutFragmentDirections.actionCheckoutFragmentToAddTipFragment())
                checkoutViewModel.navigationEnd()
            }

            if (it.startsWith("O_")) {
                orderViewModel.clearOrder()
                paymentViewModel.clearPayment()
                orderViewModel.setCurrentOrderId(it)
                navController.navigate(CheckoutFragmentDirections.actionCheckoutFragmentToOrderFragment())
                checkoutViewModel.navigationEnd()
            }

            if (it == "Checkout") {
                navController.navigate(AddTipFragmentDirections.actionAddTipFragmentToCheckoutFragment())
                checkoutViewModel.navigationEnd()
            }
        }

        checkoutViewModel.checkoutComplete.observe(this) {
            if (it != "") {
                CheckoutDialogBottomSheet().show(
                    supportFragmentManager,
                    CheckoutDialogBottomSheet.TAG
                )
            }
        }

        clockoutViewModel.clockedOut.observe(this) {
            if (it == true) {
                exitUser()
            }
        }

    }

    private fun floorplanManageObservables(){
        floorplanManageViewModel.requestDelete.observe(this) {
            if (it) {
                continueCancelViewModel.setTitle("Delete Floorplan Request")
                continueCancelViewModel.setMessage("Are you sure you want to delete this floorplan? If yes, click Continue.")
                ContinueCancelFragment().show(supportFragmentManager, ContinueCancelFragment.TAG)
            }

        }
    }

    private fun approvalsObservables(navController: NavController){
        approvalsViewModel.orderSaved.observe(this) {
            if (it) {
                homeViewModel.getOrdersFromFile()
            }
        }

        approvalsViewModel.navigateToCompleted.observe(this) {
            if (it) {
                lifecycleScope.launch {
                    approvalsViewModel.loadApprovals()
                    navController.navigate(ApprovalsFragmentDirections.actionApprovalsFragmentToCompletedApprovalsFragment())
                    approvalsViewModel.setPending(false)
                }

            }
        }

        completedApprovalsViewModel.navigateToApprovals.observe(this) {
            if (it) {
                navController.navigate(CompletedApprovalsFragmentDirections.actionCompletedApprovalsFragmentToApprovalsFragment())
            }
        }

        approvalsViewModel.navigateToHome.observe(this) {
            if (it) {
                homeViewModel.getOrdersFromFile()
                navController.navigate(ApprovalsFragmentDirections.actionApprovalsFragmentToNavHome())
                paymentViewModel.setReturnHome(false)
            }
        }
    }

    private fun datePickerObservables(){
        datePickerViewModel.source.observe( this) {
            if (it.isNotEmpty()) {
                DatePickerDialogFragment().show(
                    supportFragmentManager,
                    DatePickerDialogFragment.TAG
                )
            }
        }
    }

    private fun giftCardObservables(){
        giftCardViewModel.balanceResponse.observe(this) {
            if (it != "") {
                val balance = it.toDouble()
                errorViewModel.setTitle("Balance Inquiry")
                errorViewModel.setMessage("The gift card has a balance of $${"%.2f".format(balance)}")

                ErrorAlertBottomSheet().show(supportFragmentManager, ErrorAlertBottomSheet.TAG)
            }
        }

        giftCardViewModel.swipeResponse.observe(this) {
            if (it != null) {
                errorViewModel.setTitle("Gift Card")
                val add = it[0].toDouble()
                val balance = it[1].toDouble()
                errorViewModel.setMessage(
                    "$${"%.2f".format(add)} has been added to the gift card. The total balance on this card is $${
                        "%.2f".format(
                            balance
                        )
                    }"
                )

                ErrorAlertBottomSheet().show(supportFragmentManager, ErrorAlertBottomSheet.TAG)
            }
        }

        giftCardViewModel.amountOwed.observe(this) {
            giftCardViewModel.activePayment.value?.tickets?.forEach { t ->
                if (t.uiActive) {
                    val pt = t.paymentList?.last()
                    if (pt?.paymentType == "Cash") {
                        CashBackDialogFragment().show(
                            supportFragmentManager,
                            CashBackDialogFragment.TAG
                        )
                    }
                }
            }
        }

        giftCardViewModel.activePayment.observe(this) { it ->
            if (it != null) {
                if (it.closed) {
                    giftCardViewModel.closeOrder()
                    homeViewModel.getOrdersFromFile()
                }
            }
        }

        giftCardViewModel.error.observe(this) {
            if (it) {
                errorViewModel.setMessage(giftCardViewModel.errorMessage.value ?: "")
                errorViewModel.setTitle(giftCardViewModel.errorTitle.value ?: "")

                ErrorAlertBottomSheet().show(supportFragmentManager, ErrorAlertBottomSheet.TAG)
                giftCardViewModel.clearError()
            }
        }
    }

    private fun transferOrderObservables(navController: NavController){
        transferOrderViewModel.transferComplete.observe(this) {
            if (it) {
                homeViewModel.getOrdersFromFile()
                try {
                    navController.navigate(TransferOrderFragmentDirections.actionTransferOrderFragmentToNavHome())
                } catch (e: Throwable) {
//                    onSessionLogout()
                }
            }
        }

        transferOrderViewModel.navOrder.observe(this){
            if (it){
                navController.navigate(TransferOrderFragmentDirections.actionTransferOrderFragmentToOrderFragment())
                transferOrderViewModel.navToOrder(false)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        hideKeyboard()
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return item.onNavDestinationSelected(findNavController(R.id.nav_host_fragment))
    }

    private fun checkLoginCheckoutStatus(): Boolean{
        val user = loginRepository.getOpsUser()
        return user?.userClock?.checkout == true || user?.userClock?.checkoutApproved == true
    }

    private fun hideKeyboard() {
        WindowInsetsControllerCompat(window, window.decorView).hide(WindowInsetsCompat.Type.ime())
    }


    private fun goToPayment(navController: NavController){
        var okToPay = true
        val terminal = loginRepository.getTerminal()
        val order = orderViewModel.activeOrder.value

        lifecycleScope.launch{
            val p = paymentRepository.getPayment()
            val pid = order?.id?.replace("O_", "P_")
            if (p == null){
                paymentViewModel.getCloudPayment(pid ?: "", order?.locationId ?: "")
            }else{
                if (p.id != pid){
                    paymentViewModel.getCloudPayment(pid ?: "", order?.locationId ?: "")
                }
            }

            if (paymentViewModel.activePayment.value == null){
                if (order?.orderItems != null) {
                    okToPay = !order.orderItems.any { it.status == "Started" }

                        if (okToPay){
                            settings?.let { s ->
                                paymentViewModel.getCloudPayment(order.id.replace("O_", "P_"), s.locationId)
                            }

                            if (paymentViewModel.activePayment.value == null){
                                terminal?.let { t ->
                                    settings?.let { s ->
                                        val payment = paymentRepository.createNewPayment(order, t, s.additionalFees)
                                        orderViewModel.activeOrder.value?.let {
                                            paymentViewModel.setActiveOrder(it)
                                        }

                                        paymentViewModel.setLivePayment(payment)
                                    }

                                }

                            }
                            navController.navigate(OrderFragmentDirections.actionOrderFragmentToPaymentFragment())
                            orderViewModel.navToPayment(false)
                        }else{
                            val view = findViewById<View>(R.id.nav_host_fragment)
                            Snackbar.make(view, R.string.payment_warning_message1, Snackbar.LENGTH_LONG).show()
                        }
//                    }
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
        }else{
            orderViewModel.setTableNumber(-1)
        }

    }

    override fun returnContinue(value: String){
        when (value){
            "Checkout Error" -> {
                //Reopen Checkout then Send to Kitchen
                checkoutViewModel.reopenCheckout()
                orderViewModel.reOpenCheckoutSendOrder()
            }
            "Delete Floorplan Request" -> {
                floorplanManageViewModel.deleteFloorplan()
            }
        }
    }

    override fun returnValue(value: String) {
        when (value){
            "Discount" -> {
                return paymentViewModel.setPaymentScreen(ShowPayment.DISCOUNT, "Discount Ticket")
            }
            "Discount Item" -> {
                return paymentViewModel.setPaymentScreen(ShowPayment.DISCOUNT, "Discount Item")
            }
            "Modify Price" -> {
                return paymentViewModel.setPaymentScreen(ShowPayment.MODIFY_PRICE, null)
            }
            "Transfer Order" -> {
                return orderViewModel.showTransferOrder(true)
            }
            "Transfer Order Item" -> {
                openOrderItemTransfer()
            }
            "Misc Menu Item" -> {
                MenuItemDialog().show(supportFragmentManager, MenuItemDialog.TAG)
            }
        }
    }

    private fun openOrderItemTransfer(){
        orderViewModel.getOrdersFromFile()
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        navController.navigate(OrderFragmentDirections.actionOrderFragmentToTransferItemFragment())
        hideUpButton()
    }

    fun exitUser(){
        loginRepository.clearUser()
        this.onSessionLogout()
    }


    override fun returnDate(value: DateDialog) {
        when (value.source){
            "Confirm" -> confirmViewModel.setDate(value.date)
        }
    }

    override fun onClick(table: RestaurantTable) {
        //TODO; Will need to add permissions
        if (!table.locked) {
            floorplanViewModel.tableClicked(table)
        } else {
            Log.d("Table", "Table Locked!");
        }
    }

    fun loadDialog(type: Int, msg: String) {
        if (type == 1) {
            val args = Bundle()
            args.putString("msg", msg)
            progressDialog?.let {
                it.arguments = args
                it.show(supportFragmentManager, "Progress Dialog")
            }
        } else {
            progressDialog?.let {
                it.dismiss()
            }
        }
    }

    fun alertMessage(msg: String) {
        runOnUiThread {
            val myToast = Toast.makeText(applicationContext,msg, Toast.LENGTH_SHORT)
            myToast.show()
        }
    }

    fun showUpButton() {
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    fun hideUpButton() {
        supportActionBar!!.setDisplayHomeAsUpEnabled(false)
    }

//    override fun onBackPressed() {
//        supportFragmentManager.executePendingTransactions()
//        val count = supportFragmentManager.backStackEntryCount
//        if (count <= 1) {
//            finish()
//        } else {
//            supportFragmentManager.popBackStack()
//        }
//    }

}

