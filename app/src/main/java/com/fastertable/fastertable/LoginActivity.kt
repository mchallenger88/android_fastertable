package com.fastertable.fastertable

import EpsonDiscovery
import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.fastertable.fastertable.common.base.BaseLoginActivity
import com.fastertable.fastertable.ui.dialogs.ClockinDialog
import com.fastertable.fastertable.ui.dialogs.DialogListener
import com.fastertable.fastertable.ui.dialogs.ErrorAlertBottomSheet
import com.fastertable.fastertable.ui.error.ErrorViewModel
import com.fastertable.fastertable.ui.login.company.CompanyLoginFragmentDirections
import com.fastertable.fastertable.ui.login.terminal.TerminalSelectFragmentDirections
import com.fastertable.fastertable.ui.login.terminal.TerminalSelectViewModel
import com.fastertable.fastertable.ui.login.user.KitchenClockoutFragmentDirections
import com.fastertable.fastertable.ui.login.user.KitchenClockoutViewModel
import com.fastertable.fastertable.ui.login.user.UserLoginFragmentDirections
import com.fastertable.fastertable.ui.login.user.UserLoginViewModel
import com.google.android.material.navigation.NavigationView
import dagger.hilt.android.AndroidEntryPoint
import technology.master.kotlinprint.printer.DiscoveredPrinter
import technology.master.kotlinprint.printer.DiscoverySettings
import technology.master.kotlinprint.printer.PrinterDriver
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class LoginActivity : BaseLoginActivity(), DialogListener {
    private val userViewModel: UserLoginViewModel by viewModels()
    private val errorViewModel: ErrorViewModel by viewModels()
    private val terminalSelectViewModel: TerminalSelectViewModel by viewModels()
    private val kitchenClockoutViewModel: KitchenClockoutViewModel by viewModels()
    private val REQUEST_CODE = 2211
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)
        val navView: NavigationView = findViewById(R.id.login_nav_view)
        val navLoginFragment =
                supportFragmentManager.findFragmentById(R.id.nav_login_fragment) as NavHostFragment
        val navController: NavController = navLoginFragment.navController
        navView.setupWithNavController(navController)

        val intentFragment = intent.extras?.getString("fragmentToLoad")
        if (intentFragment != null){
            navController.navigate(CompanyLoginFragmentDirections.actionCompanyLoginFragmentToUserLoginFragment())
        }

        userViewModel.clockin.observe(this, {
            if (it){
                userViewModel.loginTime.value?.let { time ->
                    val clockin = DateTimeFormatter.ofPattern("h:mm a").withZone(ZoneId.systemDefault())
                        .format(java.time.Instant.ofEpochSecond(time))
                    errorViewModel.setTitle("User Clockin")
                    errorViewModel.setMessage("You are now clocked in. You were clocked in at $clockin")
                    ClockinDialog().show(supportFragmentManager, ClockinDialog.TAG)

                    kitchenClockoutViewModel.clockedOut.observe(this, { kit ->
                        if (kit){
                            Thread.sleep(1000)
                            navController.navigate(KitchenClockoutFragmentDirections.actionKitchenClockoutFragmentToUserLoginFragment())
                        }
                    })
                }

            }
        })

        userViewModel.validUser.observe(this, { it ->
            if (it != null && it == false){
                errorViewModel.setMessage("The User PIN you have entered is not valid")
                errorViewModel.setTitle("User Login Error")
                ErrorAlertBottomSheet().show(supportFragmentManager, ErrorAlertBottomSheet.TAG)
                userViewModel.setUserValid()
            }
        })

        userViewModel.kitchen.observe(this, {
            navController.navigate(UserLoginFragmentDirections.actionUserLoginFragmentToKitchenClockoutFragment())
        })

        userViewModel.navigateTerminal.observe(this, {
            if (it){
                navController.navigate(UserLoginFragmentDirections.actionUserLoginFragmentToTerminalSelectFragment())
            }
        })

        terminalSelectViewModel.terminal.observe(this, { terminal ->
            if (terminalSelectViewModel.onPage.value == true && terminal != null){
                userViewModel.setTerminal(terminal)
                navController.navigate(TerminalSelectFragmentDirections.actionTerminalSelectFragmentToUserLoginFragment())
            }
        })

        terminalSelectViewModel.discover.observe(this, {
            if (it){
                findBluetoothPrinters()
            }
        })
    }

    override fun returnValue(value: String) {
        val employee = userViewModel.employee.value
        if (employee !=  null){
            when (employee.employeeDetails.department){
                "Manager" -> userViewModel.navigateToHome()
                "Waitstaff" -> userViewModel.navigateToHome()
                "Admin" -> userViewModel.navigateToHome()
                "Support" -> userViewModel.navigateToKitchen()
                "Kitchen" -> userViewModel.navigateToKitchen()
                "Host" -> userViewModel.navigateToHome()
                "Barstaff" -> userViewModel.navigateToHome()
                "Back Office" -> userViewModel.navigateToKitchen()
                else -> null
            }
        }
    }

    fun findBluetoothPrinters(){
        PrinterDriver.setContext(this)
        if (!checkDiscoveryPermissions()) {
            this.updateStatus("Missing Permissions, grant and try again.")
            getDiscoveryPermissions()
        }
        this.updateStatus("Searching for devices...")
        setButtonEnabled(false)
        val discovery = EpsonDiscovery(DiscoverySettings(debugging = true, forceDriver = 2));

        discovery.searchAll().then { p ->
            val printers: MutableList<DiscoveredPrinter> = p as MutableList<DiscoveredPrinter>
            val count = printers.count();

            // No BT modules, found
            if (count == 0) {
                this.updateStatus("No Printers found")
                setButtonEnabled(true)
            } else if (count == 1) {
                val item = printers[0]
                this.updateStatus("Found 1 printer: " + item.name)
                terminalSelectViewModel.updateAddress(item.address.toString())
                setButtonEnabled(true)
            } else {
                val item = printers[0]
                this.updateStatus("Found several printers, choosing the first: "+item.name)
                this.updateStatus("", false)
                for (printer in printers) {
                    // Skip showing first item
                    if (item.address == printer.address) { continue; }
                    this.updateStatus(printer.name+":  " + printer.address, false)
                }
                terminalSelectViewModel.updateAddress(item.address.toString())
                setButtonEnabled(true)
            }
        }.catch { err ->
            Log.d("Testing", "Discovery error: $err");
            this.updateStatus("Error: $err");
            setButtonEnabled(true)
        }

    }

    private fun checkDiscoveryPermissions(): Boolean {
        if (android.os.Build.VERSION.SDK_INT >= 29) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                return true
            }
        } else {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED) { return true }
        }
        return false
    }

    private fun getDiscoveryPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= 29) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),
                REQUEST_CODE + 1
            )
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),
                REQUEST_CODE + 2
            )
        }
    }

    private fun updateStatus(str: String, clear: Boolean = true) {
        if (clear) {
            terminalSelectViewModel.updateStatus(str)
        } else {
            terminalSelectViewModel.updateStatus("\r\n" + str)
        }

    }

    private fun setButtonEnabled(b: Boolean){
        terminalSelectViewModel.setButtonEnabled(b)
    }

}