package com.tibi.tiptopo.presentation.map

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.AutoFixOff
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SendToMobile
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.outlined.Lightbulb
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
import app.akexorcist.bluetotohspp.library.DeviceList
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Dot
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.ktx.addMarker
import com.google.maps.android.ktx.addPolyline
import com.google.maps.android.ktx.awaitMap
import com.tibi.tiptopo.R
import com.tibi.tiptopo.data.Resource
import com.tibi.tiptopo.domain.Line
import com.tibi.tiptopo.domain.LineType
import com.tibi.tiptopo.domain.Measurement
import com.tibi.tiptopo.domain.PointType
import com.tibi.tiptopo.domain.Project
import com.tibi.tiptopo.domain.Station
import com.tibi.tiptopo.domain.Vertex
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
                is Resource.Success -> {
                    ExportMapFiles(mapViewModel)
                    ShowToast(mapViewModel)
                    MapScreen(currentProject.data, onSetStation, mapViewModel)
                }
                is Resource.Failure -> { Text(text = stringResource(R.string.error)) }
            }
        }
        FirebaseUserLiveData.AuthenticationState.UNAUTHENTICATED -> onLogOut()
    }
}

@Composable
fun ShowToast(mapViewModel: MapViewModel) {
    val showToast = mapViewModel.showToast
    if (showToast.isNotBlank()) {
        LocalContext.current.toast(showToast)
        mapViewModel.onStopShowToast()
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
    Scaffold(
        topBar = { TopBarMap(mapViewModel, project) }
    ) {
        MapView(mapViewModel, onSetStation)
    }
}



@ExperimentalAnimationApi
@ExperimentalFoundationApi
@Composable
fun MapView(mapViewModel: MapViewModel, onSetStation: () -> Unit) {
    val stations = mapViewModel.stations.observeAsState(Resource.Loading()).value
    val mapView = rememberMapViewWithLifecycle()
    when (stations) {
        is Resource.Failure -> Text(text = stations.throwable.message.toString())
        is Resource.Loading -> StationButton(onSetStation, stringResource(R.string.no_station))
        is Resource.Success -> {
            val currentStation = stations.data.sortedByDescending { it.date }.first()
            MapViewContainer(mapView, mapViewModel, currentStation, onSetStation)
        }
    }
}

@Composable
fun TopBarMap(mapViewModel: MapViewModel, project: Project) {
    when (mapViewModel.topBarState) {
        is MapTopBarState.LineEdit -> TobBarLine(mapViewModel)
        is MapTopBarState.Main -> TopBarMain(project, mapViewModel)
        is MapTopBarState.MeasurementEdit -> TopBarMeasurement(mapViewModel)
    }
}

@Composable
fun TopBarMeasurement(mapViewModel: MapViewModel) {
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
                mapViewModel.onResetTopBarState()
                mapViewModel.onResetCurrentMarker()
            }) {
                Icon(
                    Icons.Default.Check,
                    stringResource(R.string.end_measurement_edit)
                )
            }
        }
    )
}

@Composable
fun TopBarMain(project: Project, mapViewModel: MapViewModel) {
    TopAppBar(
        title = { Text(text = project.name) },
        actions = {
            IconButton(onClick = {
                mapViewModel.exportRawFile()
            }) {
                Icon(
                    Icons.Default.SendToMobile,
                    stringResource(R.string.export_raw_file)
                )
            }
            IconButton(onClick = {
                mapViewModel.onRefreshAll()
            }) {
                Icon(
                    Icons.Default.Refresh,
                    stringResource(R.string.refresh_all)
                )
            }
        }
    )
}

@Composable
fun TobBarLine(mapViewModel: MapViewModel) {
    val currentLine = mapViewModel.currentLine
    TopAppBar(
        title = {
            Text(text =
            when (currentLine) {
                is Resource.Success -> stringResource(R.string.add_next_point)
                is Resource.Loading -> stringResource(R.string.add_first_point)
                is Resource.Failure -> stringResource(R.string.error)
            }
            )
        },
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
                    mapViewModel.onResetTopBarState()
                }
            }) {
                Icon(
                    Icons.Default.Delete,
                    stringResource(R.string.delete)
                )
            }
            IconButton(onClick = {
                mapViewModel.onResetTopBarState()
                mapViewModel.onResetCurrentPolyline()
            }) {
                Icon(
                    Icons.Default.Check,
                    stringResource(R.string.complete_line_description)
                )
            }
        }
    )
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
    val showMeasurements = mapViewModel.showMeasurements
    val refreshAll = mapViewModel.refreshAll
    val currentPolyline = mapViewModel.currentPolyline
    val currentMarker = mapViewModel.currentMarker
    val deletePolyline = mapViewModel.deleteCurrentPolyline
    val updateCurrentMarker = mapViewModel.updateCurrentMarker
    val deleteCurrentMarker = mapViewModel.deleteCurrentMarker
    val newMeasurement = mapViewModel.newMeasurement
    val topBarState = mapViewModel.topBarState


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
                    //clear()
                    uiSettings.isZoomControlsEnabled = true
                    setPadding(0, 100, 0, 0)

                    if (topBarState is MapTopBarState.LineEdit) {
                        when (currentLine) {
                            is Resource.Success -> {
                                setOnMarkerClickListener { marker ->
                                    val line = currentLine.data
                                    val lastVertex = line.vertices.maxByOrNull { it.index }
                                    if (lastVertex != null &&
                                        lastVertex.measurementId != marker.tag!!.toString()) {
                                        val vertices = currentLine.data.vertices + listOf(Vertex(
                                            measurementId = marker.tag!!.toString(),
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
                                                measurementId = marker.tag!!.toString(),
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
                            mapViewModel.onSetCurrentMarker(marker)
                            mapViewModel.onSetTopBarState(
                                MapTopBarState.MeasurementEdit(marker.tag!!.toString())
                            )
                            marker.showInfoWindow()
                            true
                        }
                        setOnMapClickListener {
                            mapViewModel.onResetTopBarState()
                            mapViewModel.onResetCurrentMarker()
                        }
                    }

                    setOnPolylineClickListener { line ->
                        mapViewModel.onSetCurrentPolyline(line)
                        mapViewModel.onSetCurrentLine(line.tag!!.toString())
                        mapViewModel.onSetTopBarState(MapTopBarState.LineEdit)
                    }

                    if (deletePolyline) {
                        currentPolyline?.remove()
                        mapViewModel.onResetCurrentPolyline()
                        mapViewModel.onDeleteCurrentPolylineComplete()
                    }

                    if (currentPolyline != null) {
                        if (currentLine is Resource.Success) {
                            val line = currentLine.data

                            currentPolyline.points.clear()

                            currentPolyline.color = line.color


                            when (line.type) {
                                LineType.Continuous -> {
                                    currentPolyline.pattern = null
                                }
                                LineType.Dashed -> currentPolyline.pattern = listOf(
                                    Dash(20F),
                                    Gap(10F)
                                )
                                LineType.Dotted -> currentPolyline.pattern = listOf(
                                    Dot(),
                                    Gap(10F)
                                )
                                LineType.DashDotted -> currentPolyline.pattern = listOf(
                                    Dash(20F), Gap(10F),
                                    Dot(), Gap(10F)
                                )
                            }


                            if (measurements is Resource.Success) {
                                currentPolyline.points = line.vertices
                                    .sortedBy { it.index }
                                    .filter { vertex ->
                                        measurements.data.any {
                                            it.id == vertex.measurementId
                                        }
                                    }
                                    .map { vertex ->
                                        val measurement = measurements.data
                                            .first { it.id == vertex.measurementId }
                                        LatLng(measurement.latitude, measurement.longitude)
                                    }
                            }
                        }
                    } else {
                        if (currentLine is Resource.Success && measurements is Resource.Success) {
                            val line = currentLine.data
                            val polyline = addPolyline {
                                clickable(true)
                                color(line.color)
                                width(5f)

                                when (line.type) {
                                    LineType.Continuous -> {
                                    }
                                    LineType.Dashed -> pattern(
                                        listOf(
                                            Dash(20F),
                                            Gap(10F)
                                        )
                                    )
                                    LineType.Dotted -> pattern(
                                        listOf(
                                            Dot(),
                                            Gap(10F)
                                        )
                                    )
                                    LineType.DashDotted -> pattern(
                                        listOf(
                                            Dash(20F), Gap(10F),
                                            Dot(), Gap(10F)
                                        )
                                    )
                                }

                                line.vertices
                                    .sortedBy { it.index }
                                    .filter { vertex ->
                                        measurements.data.any {
                                            it.id == vertex.measurementId
                                        }
                                    }
                                    .map { vertex ->
                                        val measurement = measurements.data
                                            .first { it.id == vertex.measurementId }
                                        LatLng(measurement.latitude, measurement.longitude)
                                    }.forEach { add(it) }
                            }
                            polyline.tag = line.id
                            mapViewModel.onSetCurrentPolyline(polyline)
                        }
                    }

                    if (currentMarker != null) {
                        if (updateCurrentMarker) {
                            currentMarker.setIcon(
                                bitmapDescriptorFromVector(
                                    context,
                                    mapViewModel.currentPointObject.vectorResId,
                                    Color.BLACK
                                )
                            )

                            currentMarker.setAnchor(
                                mapViewModel.currentPointObject.anchorX,
                                mapViewModel.currentPointObject.anchorY
                            )
                            mapViewModel.onUpdateCurrentMarkerComplete()
                            mapViewModel.onResetCurrentMarker()
                        }

                        if (deleteCurrentMarker) {
                            currentMarker.remove()
                            mapViewModel.onDeleteCurrentMarkerComplete()
                            mapViewModel.onResetCurrentMarker()
                        }
                    } else {
                        if (newMeasurement != null) {
                            addMarker {
                                position(LatLng(newMeasurement.latitude, newMeasurement.longitude))
                                title(newMeasurement.number.toString())
                                icon(bitmapDescriptorFromVector(
                                    context,
                                    newMeasurement.type.vectorResId,
                                    Color.BLACK
                                ))
                                anchor(newMeasurement.type.anchorX, newMeasurement.type.anchorY)
                            }.tag = newMeasurement.id

                            mapViewModel.onResetNewMeasurement()
                        }
                    }

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


                    if (setBounds) {
                        if (measurements is Resource.Success) {
                            val bounds = LatLngBounds.builder()
                            measurements
                                .data
                                .forEach {
                                    bounds.include(LatLng(it.latitude, it.longitude))
                                    animateCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 20))
                                }
                        }
                        mapViewModel.onSetBoundsComplete()
                    }


                    if (refreshAll) {
                        clear()
                        mapViewModel.onRefreshAllComplete()
                        mapViewModel.refresh()

                        when (measurements) {
                            is Resource.Success -> {
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
                        mapViewModel.onSetTopBarState(MapTopBarState.LineEdit)
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
