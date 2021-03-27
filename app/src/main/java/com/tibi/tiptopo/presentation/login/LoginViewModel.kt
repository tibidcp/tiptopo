package com.tibi.tiptopo.presentation.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor() : ViewModel() {
    @Inject lateinit var authenticationState: LiveData<FirebaseUserLiveData.AuthenticationState>
}
