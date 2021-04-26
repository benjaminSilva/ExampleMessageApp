package com.bsoftwares.chatexample

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.viewModels
import com.bsoftwares.chatexample.ui.home.HomeActivity
import com.bsoftwares.chatexample.viewModel.UserViewModel

class StartActivity : AppCompatActivity() {

    private val viewModel by viewModels<UserViewModel>()

    override fun onStart() {
        super.onStart()
        viewModel.authenticationState.observe(this, androidx.lifecycle.Observer {
            if (it == UserViewModel.AuthenticationState.AUTHENTICATED){
                startActivity(Intent(this, HomeActivity::class.java))
            }else{
                startActivity(Intent(this, MainActivity::class.java))
            }
            finish()
        })
    }

}