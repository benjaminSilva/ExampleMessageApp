package com.bsoftwares.chatexample

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bsoftwares.chatexample.ui.HomeActivity
import com.bsoftwares.chatexample.ui.MainActivity
import com.bsoftwares.chatexample.viewModel.LoginFragmentViewModel

class StartActivity : AppCompatActivity() {

    private val viewModel by viewModels<LoginFragmentViewModel>()

    override fun onStart() {
        super.onStart()
        viewModel.authenticationState.observe(this, androidx.lifecycle.Observer {
            if (it == LoginFragmentViewModel.AuthenticationState.AUTHENTICATED){
                startActivity(Intent(this,HomeActivity::class.java))
            }else{
                startActivity(Intent(this,MainActivity::class.java))
            }
            finish()
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_splash)
    }


}