package com.tibi.tiptopo.presentation.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Logout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.ktx.addMarker
import com.google.maps.android.ktx.awaitMap
import com.tibi.tiptopo.R
import com.tibi.tiptopo.data.Resource
import com.tibi.tiptopo.domain.Measurement
import com.tibi.tiptopo.domain.PointType
import com.tibi.tiptopo.domain.Project
import com.tibi.tiptopo.domain.Station
import com.tibi.tiptopo.presentation.login.FirebaseUserLiveData
import com.tibi.tiptopo.presentation.ui.ProgressCircular
import kotlinx.coroutines.launch

@ExperimentalFoundationApi
@ExperimentalAnimationApi
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

@ExperimentalFoundationApi
@ExperimentalAnimationApi
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
        when (currentStation) {
            is Resource.Failure -> Text(text = currentStation.throwable.message.toString())
            is Resource.Loading -> StationButton(onSetStation, stringResource(R.string.no_station))
            is Resource.Success -> {
                MapViewContainer(mapView, mapViewModel, currentStation.data, onSetStation)
                currentStation.data.name
            }
        }
    }
}

@Composable
fun StationButton(onSetStation: () -> Unit, text: String) {
    Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Button(onClick = { onSetStation() }) {
            Text(text = stringResource(R.string.station))
        }
        Text(text = text, Modifier.padding(8.dp))
    }
}

@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
private fun MapViewContainer(
    map: MapView,
    mapViewModel: MapViewModel,
    station: Station,
    onSetStation: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val measurements = mapViewModel.measurements.observeAsState(Resource.Loading()).value
    val setBounds = mapViewModel.setBounds

    Box {
        AndroidView({ map }) { mapView ->
            coroutineScope.launch {
                val googleMap = mapView.awaitMap()
                googleMap.apply {
//                mapType = GoogleMap.MAP_TYPE_NONE
                    setPadding(0, 0, 0, 650)
                    uiSettings.isZoomControlsEnabled = true

                    setOnMapLongClickListener {
                        mapViewModel.addMeasurement(
                            Measurement(
                                latitude = it.latitude,
                                longitude = it.longitude,
                                type = mapViewModel.currentPointObject,
                                name = it.toString()
                            ),
                            station.id
                        )
                    }

                    when (measurements) {
                        is Resource.Success -> {
                            if (setBounds) {
                                val bounds = LatLngBounds.builder()
                                measurements.data.forEach {
                                    bounds.include(LatLng(it.latitude, it.longitude))
                                    animateCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 20))
                                }
                                mapViewModel.onSetBoundsComplete()
                            }

                            measurements.data.forEach { measurement ->
                                addMarker {
                                    position(LatLng(measurement.latitude, measurement.longitude))
                                    title(measurement.name)
                                    icon(bitmapDescriptorFromVector(context, measurement.type.vectorResId, Color.CYAN))
                                    anchor(measurement.type.anchorX, measurement.type.anchorY)
                                }
                            }
                        }
                        is Resource.Failure -> {  }
                        is Resource.Loading -> {  }
                    }
                }
            }
        }

        Row {
            StationButton(onSetStation = { onSetStation() }, text = station.name)

            Button(onClick = { mapViewModel.onSetBounds() }, Modifier.padding(8.dp)) {
                Icon(Icons.Default.Fullscreen, "")
            }
        }
        DrawingTools(mapViewModel = mapViewModel)
    }
}

@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun DrawingTools(mapViewModel: MapViewModel) {
    var showPointObjects by remember { mutableStateOf(false) }
    val pointObjects = remember { PointType.values() }

    Column(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        AnimatedVisibility(visible = showPointObjects) {
            LazyVerticalGrid(cells = GridCells.Fixed(6)) {
                items(pointObjects) { pointObject ->
                    FloatingActionButton(
                        onClick = {
                            mapViewModel.onSetCurrentPointObject(pointObject)
                            showPointObjects = false
                        },
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Icon(
                            painter = painterResource(pointObject.vectorResId),
                            contentDescription = stringResource(pointObject.contentDescription)
                        )
                    }
                }
            }
        }
        FloatingActionButton(
            onClick = { showPointObjects = !showPointObjects }
        ) {
            Icon(
                painter = painterResource(id = mapViewModel.currentPointObject.vectorResId),
                contentDescription = stringResource(
                    id = mapViewModel.currentPointObject.contentDescription
                )
            )
        }
    }
}

private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int, color: Int): BitmapDescriptor? {
    return ContextCompat.getDrawable(context, vectorResId)?.run {
        DrawableCompat.setTint(this, color)
        setBounds(0, 0, intrinsicWidth, intrinsicHeight)
        val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
        draw(Canvas(bitmap))
        BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}
