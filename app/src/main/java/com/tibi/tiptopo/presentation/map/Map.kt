package com.tibi.tiptopo.presentation.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.ktx.addMarker
import com.google.maps.android.ktx.awaitMap
import com.tibi.tiptopo.R
import com.tibi.tiptopo.data.Resource
import com.tibi.tiptopo.domain.Measurement
import com.tibi.tiptopo.domain.Project
import com.tibi.tiptopo.presentation.login.FirebaseUserLiveData
import com.tibi.tiptopo.presentation.ui.ProgressCircular
import kotlinx.coroutines.launch

@Composable
fun Map(
    mapViewModel: MapViewModel,
    onSetStation: () -> Unit,
    onLogOut: () -> Unit
) {
    val authState: FirebaseUserLiveData.AuthenticationState by mapViewModel.authenticationState
        .observeAsState(FirebaseUserLiveData.AuthenticationState.AUTHENTICATED)
    val currentProject = mapViewModel.currentProject.observeAsState(Resource.Loading()).value

    when (authState) {
        FirebaseUserLiveData.AuthenticationState.AUTHENTICATED -> {
            when (currentProject) {
                is Resource.Loading -> { ProgressCircular() }
                is Resource.Success -> MapScreen(currentProject.data, onSetStation, mapViewModel)
                is Resource.Failure -> { Text(text = stringResource(R.string.error)) }
            }
        }
        FirebaseUserLiveData.AuthenticationState.UNAUTHENTICATED -> onLogOut()
    }
}

@Composable
fun MapScreen(
    project: Project,
    onSetStation: () -> Unit,
    mapViewModel: MapViewModel
) {
    val currentStation = mapViewModel.currentStation.observeAsState(Resource.Loading()).value

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
        Box {
            val text = when (currentStation) {
                is Resource.Failure -> currentStation.throwable.message.toString()
                is Resource.Loading -> stringResource(R.string.no_station)
                is Resource.Success -> {
                    MapViewContainer(mapView, mapViewModel, currentStation.data.id)
                    currentStation.data.name
                }
            }
            Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = { onSetStation() }) {
                    Text(text = stringResource(R.string.station))
                }
                Text(text = text, Modifier.padding(8.dp))
            }
        }
    }
}

@Composable
private fun MapViewContainer(map: MapView, mapViewModel: MapViewModel, stationId: String) {
    val coroutineScope = rememberCoroutineScope()
    AndroidView({ map }) { mapView ->
        coroutineScope.launch {
            val googleMap = mapView.awaitMap()
            googleMap.apply {
//                mapType = GoogleMap.MAP_TYPE_NONE
                uiSettings.isZoomControlsEnabled = true
                val latLng = LatLng(55.67520559996388, 37.8)
                addMarker {
                    position(latLng)
                    title("New Point")
                }
                moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17f))

                setOnMapLongClickListener {
                    mapViewModel.addMeasurement(Measurement(latLng = it), stationId)
                }
            }
        }
    }
}
