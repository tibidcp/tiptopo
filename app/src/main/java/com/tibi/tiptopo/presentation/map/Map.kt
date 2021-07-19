package com.tibi.tiptopo.presentation.map

import android.content.ClipData
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
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.DrawableCompat
import app.akexorcist.bluetotohspp.library.DeviceList
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.maps.android.ktx.addMarker
import com.google.maps.android.ktx.addPolyline
import com.google.maps.android.ktx.awaitMap
import com.tibi.tiptopo.R
import com.tibi.tiptopo.data.Resource
import com.tibi.tiptopo.domain.*
import com.tibi.tiptopo.presentation.di.CurrentProjectId
import com.tibi.tiptopo.presentation.login.FirebaseUserLiveData
import com.tibi.tiptopo.presentation.toast
import com.tibi.tiptopo.presentation.ui.ProgressCircular
import kotlinx.coroutines.launch
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


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
    val stations = mapViewModel.stations.observeAsState(Resource.Loading()).value
    val measurements = mapViewModel.measurements.observeAsState(Resource.Loading()).value
    val lines = mapViewModel.lines.observeAsState(Resource.Loading()).value
    val currentLine = mapViewModel.currentLine
    val drawLine = mapViewModel.drawLine
    val showToast = mapViewModel.showToast
    val selectedMeasurementId = mapViewModel.selectedMeasurementId
    val rawText = mapViewModel.rawText
    val measurementJson = mapViewModel.measurementJsonText
    val linearJson = mapViewModel.linearJsonText

    if (rawText.isNotBlank() && measurementJson.isNotBlank() && linearJson.isNotBlank()) {

        val context = LocalContext.current

        val docPath = File(context.filesDir, "docs")
        val docFile = File(docPath, "raw.rdf")
        val docUri = FileProvider.getUriForFile(context, "com.tibi.tiptopo.fileprovider", docFile)


        val measurementFile = File(docPath, "measurements.json")
        val measurementUri = FileProvider.getUriForFile(context, "com.tibi.tiptopo.fileprovider", measurementFile)

        val linearFile = File(docPath, "lines.json")
        val linearUri = FileProvider.getUriForFile(context, "com.tibi.tiptopo.fileprovider", linearFile)

        val uris = arrayListOf(docUri, measurementUri, linearUri)

        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND_MULTIPLE
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
            type = "text/*"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)

        val dir = context.filesDir.absolutePath + File.separator + "docs"
        val projDir = File(dir)
        if (!projDir.exists()) {
            projDir.mkdirs()
        }
        val file = File(projDir, "raw.rdf")
        val stream = FileOutputStream(file)
        stream.use {
            stream.write(rawText.toByteArray())
        }

        val fileMeasurement = File(projDir, "measurements.json")
        val streamMeasurement = FileOutputStream(fileMeasurement)
        streamMeasurement.use {
            streamMeasurement.write(measurementJson.toByteArray())
        }

        val fileLinear = File(projDir, "lines.json")
        val streamLinear = FileOutputStream(fileLinear)
        streamLinear.use {
            streamLinear.write(linearJson.toByteArray())
        }

        context.startActivity(shareIntent)
        mapViewModel.onResetExportTexts()
    }

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
                                mapViewModel.onResetCurrentPolyline()
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
                                    mapViewModel.onResetCurrentMarker()
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
                            title = { Text(text = project.name) },
                            actions = {
                                IconButton(onClick = {
                                    if (stations is Resource.Success && measurements is Resource.Success && lines is Resource.Success) {
                                        mapViewModel.exportRawFile(stations.data, measurements.data, lines.data)
                                    }
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
    val refreshAll = mapViewModel.refreshAll
    val currentPolyline = mapViewModel.currentPolyline
    val currentMarker = mapViewModel.currentMarker
    val deletePolyline = mapViewModel.deleteCurrentPolyline
    val updateCurrentMarker = mapViewModel.updateCurrentMarker
    val deleteCurrentMarker = mapViewModel.deleteCurrentMarker
    val newMeasurement = mapViewModel.newMeasurement


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
                            mapViewModel.onSetCurrentMarker(marker)
                            mapViewModel.onSetSelectedMeasurementId(marker.tag.toString())
                            marker.showInfoWindow()
                            true
                        }
                        setOnMapClickListener {
                            mapViewModel.onResetSelectedMeasurementId()
                            mapViewModel.onResetCurrentMarker()
                        }
                    }

                    setOnPolylineClickListener { line ->
                        mapViewModel.onSetCurrentPolyline(line)
                        mapViewModel.onSetCurrentLine(line.tag.toString())
                        mapViewModel.onDrawLine()
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
