package com.tibi.tiptopo.presentation.login

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.activity.compose.registerForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.tibi.tiptopo.R

const val TAG = "Login"

@Composable
fun Login(loginViewModel: LoginViewModel, onLoginComplete: () -> Unit) {
    val authState: FirebaseUserLiveData.AuthenticationState by
    loginViewModel.authenticationState.observeAsState(
        FirebaseUserLiveData.AuthenticationState.UNAUTHENTICATED
    )

    val openLoginActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val response = IdpResponse.fromResultIntent(result.data)
            if (result.resultCode == Activity.RESULT_OK) {
                Log.i(
                    TAG, "Successfully signed in user " +
                            "${FirebaseAuth.getInstance().currentUser?.displayName}!"
                )
            } else {
                Log.i(TAG, "Sign in unsuccessful ${response?.error?.errorCode}")
            }
        }
    when (authState) {
        FirebaseUserLiveData.AuthenticationState.UNAUTHENTICATED -> {
            Column(
                modifier = Modifier.fillMaxWidth().fillMaxHeight(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(onClick = { launchSignInFlow(openLoginActivity) }) {
                    Text(stringResource(R.string.login))
                }
            }
        }
        FirebaseUserLiveData.AuthenticationState.AUTHENTICATED -> onLoginComplete()
    }
}

private fun launchSignInFlow(openLoginActivity: ActivityResultLauncher<Intent>) {
    val providers = arrayListOf(
        AuthUI.IdpConfig.EmailBuilder().build(),
        AuthUI.IdpConfig.GoogleBuilder().build()
    )

    openLoginActivity.launch(
        AuthUI.getInstance().createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setIsSmartLockEnabled(false)
            .build()
    )
}
