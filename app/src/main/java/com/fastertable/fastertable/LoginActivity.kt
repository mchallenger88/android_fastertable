package com.fastertable.fastertable

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Bundle
import android.view.Menu
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.fastertable.fastertable.ui.ui.login.company.CompanyLoginViewModel
import com.fastertable.fastertable.ui.ui.login.user.UserLoginFragmentDirections
import com.fastertable.fastertable.ui.ui.login.user.UserLoginViewModel
import com.google.android.material.navigation.NavigationView

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)
        val navView: NavigationView = findViewById(R.id.login_nav_view)
        val navLoginFragment =
                supportFragmentManager.findFragmentById(R.id.nav_login_fragment) as NavHostFragment
        val navController: NavController = navLoginFragment.navController
        navView.setupWithNavController(navController)

//        val userViewModel: UserLoginViewModel by viewModels()
//        userViewModel.navigate.observe(this, Observer {
//                val intent = Intent(this, MainActivity::class.java)
//                intent.setFlags(FLAG_ACTIVITY_NEW_TASK)
//                startActivity(intent)
//
//                this.findNavController(R.id.login_nav_view).navigate(UserLoginFragmentDirections.actionUserLoginFragmentToHomeFragment())
//        })
    }

    public fun goHome(){
//        val intent = Intent(this, MainActivity::class.java)
//        intent.setFlags(FLAG_ACTIVITY_NEW_TASK)
//        app.startActivity(intent)

    }


}