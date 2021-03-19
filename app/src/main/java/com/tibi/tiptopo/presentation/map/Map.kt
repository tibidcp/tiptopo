package com.tibi.tiptopo.presentation.map

import androidx.compose.foundation.layout.Box
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.maps.MapView
import com.google.maps.android.ktx.awaitMap
import com.tibi.tiptopo.presentation.login.FirebaseUserLiveData
import kotlinx.coroutines.launch

@Composable
fun Map(projectId: String, mapViewModel: MapViewModel, onLogOut: () -> Unit) {
    val authState: FirebaseUserLiveData.AuthenticationState by
    mapViewModel.authenticationState
        .observeAsState(FirebaseUserLiveData.AuthenticationState.AUTHENTICATED)

    when (authState) {
        FirebaseUserLiveData.AuthenticationState.AUTHENTICATED -> {
            Box {
                val mapView = rememberMapViewWithLifecycle()
                MapViewContainer(mapView)
                val context = LocalContext.current
                Button(onClick = { AuthUI.getInstance().signOut(context) }) {
                    Text("Logout")
                }
            }
        }
        FirebaseUserLiveData.AuthenticationState.UNAUTHENTICATED -> onLogOut()
    }
}

@Composable
private fun MapViewContainer(map: MapView) {
    val coroutineScope = rememberCoroutineScope()
    AndroidView({ map }) { mapView ->
        coroutineScope.launch {
            val googleMap = mapView.awaitMap()
        }
    }
}
