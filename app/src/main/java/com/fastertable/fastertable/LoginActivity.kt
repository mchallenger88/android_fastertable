package com.fastertable.fastertable

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.fastertable.fastertable.common.base.BaseActivity
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