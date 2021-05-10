package com.fastertable.fastertable

import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.fastertable.fastertable.common.base.BaseActivity
import com.fastertable.fastertable.ui.login.company.CompanyLoginFragmentDirections
import com.google.android.material.navigation.NavigationView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : BaseActivity() {

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
    }


}