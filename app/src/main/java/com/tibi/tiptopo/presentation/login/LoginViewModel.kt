package com.tibi.tiptopo.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor() : ViewModel() {
    val authenticationState = FirebaseUserLiveData().map { user ->
        if (user != null) {
            FirebaseUserLiveData.AuthenticationState.AUTHENTICATED
        } else {
            FirebaseUserLiveData.AuthenticationState.UNAUTHENTICATED
        }
    }
}