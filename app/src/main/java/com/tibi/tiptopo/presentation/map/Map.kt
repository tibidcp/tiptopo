package com.tibi.tiptopo.presentation.map

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import app.akexorcist.bluetotohspp.library.DeviceList
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.*
import com.google.maps.android.ktx.addMarker
import com.google.maps.android.ktx.addPolyline
import com.google.maps.android.ktx.awaitMap
import com.tibi.tiptopo.R
import com.tibi.tiptopo.data.Resource
import com.tibi.tiptopo.domain.*
import com.tibi.tiptopo.presentation.login.FirebaseUserLiveData
import com.tibi.tiptopo.presentation.toast
import com.tibi.tiptopo.presentation.ui.ProgressCircular
import kotlinx.coroutines.launch

val colorList = listOf(
    Color.BLACK,
    Color.CYAN,
    Color.BLUE,
    Color.GREEN,
    Color.MAGENTA,
    Color.RED,
    Color.YELLOW
)

@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun Map(
    mapViewModel: MapViewModel,
    onSetStation: () -> Unit,
    onLogOut: () -> Unit
) {
    mapViewModel.setBluetoothDataAndConnectionListener()

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
    val currentLine = mapViewModel.currentLine
    val drawLine = mapViewModel.drawLine
    val showToast = mapViewModel.showToast
    val selectedMeasurementId = mapViewModel.selectedMeasurementId

    if (showToast.isNotBlank()) {
        LocalContext.current.toast(showToast)
        mapViewModel.onStopShowToast()
    }

    Scaffold(
        topBar = {
            when (drawLine) {
                true -> TopAppBar(
                        title = { Text(text =
                        when (currentLine) {
                            is Resource.Success -> stringResource(R.string.add_next_point)
                            is Resource.Loading -> stringResource(R.string.add_first_point)
                            is Resource.Failure -> stringResource(R.string.error)
                        }
                        ) },
                        actions = {
                            IconButton(onClick = {
                                if (currentLine is Resource.Success) {
                                    mapViewModel.onDeleteLastVertex(currentLine.data)
                                }
                            }) {
                                Icon(
                                    Icons.Default.AutoFixOff,
                                    stringResource(R.string.delete_last_vertex)
                                )
                            }
                            IconButton(onClick = {
                                if (currentLine is Resource.Success) {
                                    mapViewModel.onReverseCurrentLine(currentLine.data)
                                }
                            }) {
                                Icon(
                                    Icons.Default.CompareArrows,
                                    stringResource(R.string.reverse_current_line)
                                )
                            }
                            IconButton(onClick = {
                                if (currentLine is Resource.Success) {
                                    mapViewModel.onUpdateCurrentLineTypeAndColor(currentLine.data)
                                }
                            }) {
                                Icon(
                                    Icons.Default.Update,
                                    stringResource(R.string.update_current_line_type_and_color)
                                )
                            }
                            IconButton(onClick = {
                                if (currentLine is Resource.Success) {
                                    mapViewModel.onDeleteLine(currentLine.data.id)
                                    mapViewModel.onDrawLineComplete()
                                }
                            }) {
                                Icon(
                                    Icons.Default.Delete,
                                    stringResource(R.string.delete)
                                )
                            }
                            IconButton(onClick = {
                                mapViewModel.onDrawLineComplete()
                            }) {
                                Icon(
                                    Icons.Default.Check,
                                    stringResource(R.string.complete_line_description)
                                )
                            }
                        }
                    )
                false -> {
                    if (selectedMeasurementId.isNotBlank()) {
                        TopAppBar(
                            title = { Text(text = stringResource(R.string.edit_point)) },
                            actions = {
                                IconButton(onClick = {
                                    mapViewModel.onUpdateSelectedMeasurementType()
                                }) {
                                    Icon(
                                        Icons.Default.Update,
                                        "Update measurement type"
                                    )
                                }
                                IconButton(onClick = {
                                    mapViewModel.onDeleteSelectedMeasurement()
                                }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        stringResource(R.string.delete)
                                    )
                                }
                                IconButton(onClick = {
                                    mapViewModel.onResetSelectedMeasurementId()
                                }) {
                                    Icon(
                                        Icons.Default.Check,
                                        stringResource(R.string.end_measurement_edit)
                                    )
                                }
                            }
                        )
                    } else
                    {
                        TopAppBar(
                            title = { Text(text = project.name) }
                        )
                    }
                }
            }
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
    val lines = mapViewModel.lines.observeAsState(Resource.Loading()).value
    val setBounds = mapViewModel.setBounds
    val currentLine = mapViewModel.currentLine
    val drawLine = mapViewModel.drawLine
    val showMeasurements = mapViewModel.showMeasurements


    val showDeviceList = mapViewModel.showDeviceList
    val openDeviceListActivity =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            mapViewModel.onStopShowDeviceList(result)
        }
    if (showDeviceList) {
        openDeviceListActivity.launch(
            Intent(LocalContext.current, DeviceList::class.java)
        )
    }

    Box {
        AndroidView({ map }) { mapView ->
            coroutineScope.launch {
                val googleMap = mapView.awaitMap()
                googleMap.apply {
                mapType = GoogleMap.MAP_TYPE_NONE
                    clear()
                    uiSettings.isZoomControlsEnabled = true
                    setPadding(0, 100, 0, 0)

                    if (drawLine) {
                        when (currentLine) {
                            is Resource.Success -> {
                                setOnMarkerClickListener { marker ->
                                    val line = currentLine.data
                                    val lastVertex = line.vertices.maxByOrNull { it.index }
                                    if (lastVertex != null &&
                                        lastVertex.measurementId != marker.tag.toString()) {
                                        val vertices = currentLine.data.vertices + listOf(Vertex(
                                            measurementId = marker.tag.toString(),
                                            index = lastVertex.index + 1
                                        ))
                                        line.vertices = vertices
                                        mapViewModel.updateLine(line)
                                    }
                                    true
                                }
                            }
                            is Resource.Loading -> {
                                setOnMarkerClickListener { marker ->
                                    val line = Line(
                                        vertices = listOf(
                                            Vertex(
                                                measurementId = marker.tag.toString(),
                                                index = 0
                                            )
                                        ),
                                        type = mapViewModel.currentLineType ?: LineType.Continuous
                                    )
                                    mapViewModel.addLine(line)
                                    true
                                }
                            }
                            is Resource.Failure -> {  }
                        }
                    } else {
                        setOnMarkerClickListener { marker ->
                            mapViewModel.onSetSelectedMeasurementId(marker.tag.toString())
                            marker.showInfoWindow()
                            true
                        }
                        setOnMapClickListener { mapViewModel.onResetSelectedMeasurementId() }
                    }

                    setOnPolylineClickListener { line ->
                        mapViewModel.onSetCurrentLine(line.tag.toString())
                        mapViewModel.onDrawLine()
                    }

//                    setOnMapLongClickListener {
//                        mapViewModel.addMeasurement(
//                            Measurement(
//                                latitude = it.latitude,
//                                longitude = it.longitude,
//                                type = mapViewModel.currentPointObject,
//                                name = it.toString()
//                            ),
//                            station.id
//                        )
//                    }

                    when (measurements) {
                        is Resource.Success -> {
                            if (setBounds) {
                                val bounds = LatLngBounds.builder()
                                measurements
                                    .data
                                    .forEach {
                                    bounds.include(LatLng(it.latitude, it.longitude))
                                    animateCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 20))
                                }
                                mapViewModel.onSetBoundsComplete()
                            }

                            if (showMeasurements) {
                                measurements.data.forEach { measurement ->
                                    addMarker {
                                        position(LatLng(measurement.latitude, measurement.longitude))
                                        title(measurement.number.toString())
                                        icon(bitmapDescriptorFromVector(
                                            context,
                                            measurement.type.vectorResId,
                                            Color.BLACK
                                        ))
                                        anchor(measurement.type.anchorX, measurement.type.anchorY)
                                    }.tag = measurement.id
                                }
                            }

                            when (lines) {
                                is Resource.Success -> {
                                    lines.data.forEach { line ->
                                        addPolyline {
                                            clickable(true)
                                            color(line.color)
                                            width(5f)

                                            when (line.type) {
                                                LineType.Continuous -> { }
                                                LineType.Dashed -> pattern(listOf(Dash(20F),
                                                    Gap(10F)))
                                                LineType.Dotted -> pattern(listOf(Dot(),
                                                    Gap(10F)))
                                                LineType.DashDotted ->  pattern(listOf(
                                                    Dash(20F), Gap(10F),
                                                    Dot(), Gap(10F)
                                                ))
                                            }

                                            line.vertices
                                                .sortedBy { it.index }
                                                .filter { vertex -> measurements.data.any {
                                                    it.id == vertex.measurementId}
                                                }
                                                .map { vertex ->
                                                    val measurement = measurements.data
                                                        .first { it.id == vertex.measurementId }
                                                    LatLng(measurement.latitude, measurement.longitude)
                                                }.forEach { add(it) }
                                        }.tag = line.id
                                    }
                                }
                                is Resource.Failure -> {  }
                                is Resource.Loading -> {  }
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

            Button(onClick = { mapViewModel.onConnectBluetooth() }, Modifier.padding(8.dp)) {
                Icon(
                    Icons.Default.Bluetooth,
                    stringResource(R.string.bluetooth_button_description)
                )
            }

            Button(onClick = { mapViewModel.onShowMeasurementsChangeState() }, Modifier.padding(8.dp)) {
                Icon(
                    if (showMeasurements) Icons.Filled.Lightbulb else Icons.Outlined.Lightbulb,
                    stringResource(R.string.show_or_hide_measurements)
                )
            }
        }
        DrawingTools(mapViewModel = mapViewModel)
    }
}

@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun DrawingTools(mapViewModel: MapViewModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Bottom
    ) {
        LineDrawing(mapViewModel)
        ColorPicker(mapViewModel)
        PointDrawing(mapViewModel)
    }
}


@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun LineDrawing(mapViewModel: MapViewModel) {
    var showLines by remember { mutableStateOf(false) }
    val lines = remember { LineType.values() }
    val currentLineType = mapViewModel.currentLineType

    AnimatedVisibility(visible = showLines) {
        LazyVerticalGrid(cells = GridCells.Fixed(6)) {
            items(lines) { line ->
                FloatingActionButton(
                    onClick = {
                        mapViewModel.onResetCurrentPointObject()
                        mapViewModel.onSetCurrentLineType(line)
                        mapViewModel.onDrawLine()
                        showLines = false
                    },
                    modifier = Modifier.padding(4.dp)
                ) {
                    Icon(
                        painter = painterResource(line.vectorResId),
                        contentDescription = stringResource(line.contentDescription)
                    )
                }
            }
        }
    }

    FloatingActionButton(
        onClick = { showLines = !showLines },
        modifier = Modifier.padding(8.dp)
    ) {
        if (currentLineType == null) {
            Text(text = "Line")
        } else {
            Icon(
                painter = painterResource(id = currentLineType.vectorResId),
                contentDescription = stringResource(
                    id = currentLineType.contentDescription
                )
            )
        }
    }
}

@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun PointDrawing(mapViewModel: MapViewModel) {
    var showPointObjects by remember { mutableStateOf(false) }
    val pointObjects = remember { PointType.values() }
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
        onClick = { showPointObjects = !showPointObjects },
        Modifier.padding(8.dp)
    ) {
        Icon(
            painter = painterResource(id = mapViewModel.currentPointObject.vectorResId),
            contentDescription = stringResource(
                id = mapViewModel.currentPointObject.contentDescription
            )
        )
    }
}

@ExperimentalAnimationApi
@ExperimentalFoundationApi
@Composable
fun ColorPicker(mapViewModel: MapViewModel) {
    var showColors by remember { mutableStateOf(false) }

    AnimatedVisibility(visible = showColors) {
        LazyVerticalGrid(cells = GridCells.Fixed(6)) {
            items(colorList) { color ->
                FloatingActionButton(
                    onClick = {
                        mapViewModel.onSetCurrentColor(color)
                        showColors = false
                    },
                    modifier = Modifier.padding(4.dp),
                    backgroundColor = androidx.compose.ui.graphics.Color(color)
                ) {
                }
            }
        }
    }

    FloatingActionButton(
        onClick = { showColors = !showColors },
        backgroundColor = androidx.compose.ui.graphics.Color(mapViewModel.currentColor),
        modifier = Modifier.padding(8.dp)
    ) {
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
