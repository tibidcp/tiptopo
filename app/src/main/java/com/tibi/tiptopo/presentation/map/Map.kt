package com.tibi.tiptopo.presentation.map

import android.util.Log
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.geometry.Point
import com.google.maps.android.ktx.addMarker
import com.google.maps.android.ktx.awaitMap
import com.google.maps.android.ktx.utils.component1
import com.google.maps.android.ktx.utils.component2
import com.google.maps.android.ktx.utils.geometry.component1
import com.google.maps.android.ktx.utils.geometry.component2
import com.google.maps.android.projection.SphericalMercatorProjection
import com.tibi.tiptopo.R
import com.tibi.tiptopo.data.Resource
import com.tibi.tiptopo.domain.Project
import com.tibi.tiptopo.presentation.login.FirebaseUserLiveData
import com.tibi.tiptopo.presentation.ui.ProgressCircular
import kotlinx.coroutines.launch

@Composable
fun Map(projectId: String, mapViewModel: MapViewModel, onLogOut: () -> Unit) {
    val authState: FirebaseUserLiveData.AuthenticationState by
    mapViewModel.authenticationState
        .observeAsState(FirebaseUserLiveData.AuthenticationState.AUTHENTICATED)
    mapViewModel.setCurrentProject(projectId)

    when (authState) {
        FirebaseUserLiveData.AuthenticationState.AUTHENTICATED -> {
            when (val project = mapViewModel.currentProject) {
                is Resource.Loading -> { ProgressCircular() }
                is Resource.Success -> MapScreen(project.data, mapViewModel)
                is Resource.Failure -> { Text(text = stringResource(R.string.error)) }
            }
        }
        FirebaseUserLiveData.AuthenticationState.UNAUTHENTICATED -> onLogOut()
    }
}

@Composable
fun MapScreen(project: Project, mapViewModel: MapViewModel) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = project.name) },
                actions = {
                    val context = LocalContext.current
                    IconButton(onClick = { AuthUI.getInstance().signOut(context) }) {
                        Icon(Icons.Default.Logout, stringResource(R.string.logout_description))
                    }
                }
            )
        }
    ) {
        val mapView = rememberMapViewWithLifecycle()
        MapViewContainer(mapView)
    }
}

@Composable
private fun MapViewContainer(map: MapView) {
    val coroutineScope = rememberCoroutineScope()
    AndroidView({ map }) { mapView ->
        coroutineScope.launch {
            val googleMap = mapView.awaitMap()
            googleMap.uiSettings.isZoomControlsEnabled = true
            val latLng = LatLng(55.696113, 37.504172)
            googleMap.addMarker {
                position(latLng)
                title("New Point")
            }
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17f))

            val (x, y) = SphericalMercatorProjection(10000.0).toLatLng(Point(6050.0, 3130.0))
            Log.d("SphericalMercatorProjection", "$x, $y")
        }
    }
}
