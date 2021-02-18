package com.tibi.tiptopo.presentation.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.tibi.tiptopo.presentation.login.FirebaseUserLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor() : ViewModel() {
    val authenticationState = FirebaseUserLiveData().map { user ->
        if (user != null) {
            FirebaseUserLiveData.AuthenticationState.AUTHENTICATED
        } else {
            FirebaseUserLiveData.AuthenticationState.UNAUTHENTICATED
        }
    }
}
