package com.fastertable.fastertable

import android.os.Bundle
import androidx.activity.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.fastertable.fastertable.common.base.BaseActivity
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
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class LoginActivity : BaseActivity(), DialogListener {
    private val userViewModel: UserLoginViewModel by viewModels()
    private val errorViewModel: ErrorViewModel by viewModels()
    private val terminalSelectViewModel: TerminalSelectViewModel by viewModels()
    private val kitchenClockoutViewModel: KitchenClockoutViewModel by viewModels()

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
                val clockin = DateTimeFormatter.ofPattern("h:mm a").withZone(ZoneId.systemDefault())
                    .format(java.time.Instant.ofEpochSecond(userViewModel.loginTime.value!!))
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


    }

    override fun returnValue(value: String) {
        val employee = userViewModel.employee.value
        if (employee !=  null){
            when (employee.employeeDetails.department){
                "Manager" -> userViewModel.navigateToHome()
                "Waitstaff" -> userViewModel.navigateToHome()
                else -> null
            }
        }


    }


}