package com.bsoftwares.chatexample.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.bsoftwares.chatexample.utils.FirebaseUserLiveData

class LoginFragmentViewModel : ViewModel(){

    enum class AuthenticationState {
        AUTHENTICATED, UNAUTHENTICATED, INVALID_AUTHENTICATION
    }

    val authenticationState = FirebaseUserLiveData().map { user ->
        if (user != null) {
            AuthenticationState.AUTHENTICATED
        }else{
            AuthenticationState.UNAUTHENTICATED
        }
    }

    val currentUserUid = FirebaseUserLiveData().map { user ->
        user?.uid
    }

}