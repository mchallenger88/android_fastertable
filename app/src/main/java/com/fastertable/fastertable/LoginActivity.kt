package com.fastertable.fastertable

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
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
    }

    public fun goHome(){
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)

    }


}