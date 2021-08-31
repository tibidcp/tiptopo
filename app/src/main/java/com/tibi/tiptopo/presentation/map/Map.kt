package com.tibi.tiptopo.presentation.map

import android.content.Intent
import android.graphics.Color
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import app.akexorcist.bluetotohspp.library.DeviceList
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.ktx.awaitMap
import com.tibi.tiptopo.R
import com.tibi.tiptopo.data.Resource
import com.tibi.tiptopo.domain.LineType
import com.tibi.tiptopo.domain.Measurement
import com.tibi.tiptopo.domain.PointType
import com.tibi.tiptopo.domain.Project
import com.tibi.tiptopo.domain.Station
import com.tibi.tiptopo.presentation.login.FirebaseUserLiveData
import com.tibi.tiptopo.presentation.toast
import com.tibi.tiptopo.presentation.ui.ItemEntryInput
import com.tibi.tiptopo.presentation.ui.ProgressCircular
import kotlinx.coroutines.launch


val colorList = listOf(
    Color.BLACK,
    Color.CYAN,
    Color.BLUE,
    Color.GREEN,
    Color.MAGENTA,
    Color.RED,
    Color.YELLOW,
    Color.GRAY
)

@ExperimentalComposeUiApi
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

@ExperimentalComposeUiApi
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

@ExperimentalComposeUiApi
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
            MapBox(mapView, mapViewModel, currentStation, onSetStation)
        }
    }
}

@Composable
fun TopBarMap(mapViewModel: MapViewModel, project: Project) {
    when (mapViewModel.mapState) {
        is MapState.LineEdit -> TobBarLine(mapViewModel)
        is MapState.Main -> TopBarMain(project, mapViewModel)
        is MapState.MeasurementEdit -> TopBarMeasurement(mapViewModel)
    }
}

@Composable
fun TopBarMeasurement(mapViewModel: MapViewModel) {
    TopAppBar(
        title = { Text(text = stringResource(R.string.edit_point)) },
        actions = {
            IconButton(onClick = {
                mapViewModel.onFetchCurrentNote()
            }) {
                Icon(
                    Icons.Default.Message,
                    stringResource(R.string.add_note)
                )
            }
            IconButton(onClick = {
                mapViewModel.onUpdateSelectedMeasurementType()
            }) {
                Icon(
                    Icons.Default.Update,
                    stringResource(R.string.update_measurement_type)
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
                mapViewModel.onResetMapState()
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
        }
    )
}

@Composable
fun TobBarLine(mapViewModel: MapViewModel) {
    val currentLine = mapViewModel.currentLine
    val currentPolyline = mapViewModel.currentPolyline
    TopAppBar(
        title = {
            Text(text =
                if (currentPolyline == null) {
                    stringResource(R.string.add_first_point)
                } else {
                    stringResource(R.string.add_next_point)
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
                    mapViewModel.onResetMapState()
                    mapViewModel.onResetCurrentLine()
                }
            }) {
                Icon(
                    Icons.Default.Delete,
                    stringResource(R.string.delete)
                )
            }
            IconButton(onClick = {
                mapViewModel.onResetMapState()
                mapViewModel.onResetCurrentPolyline()
                mapViewModel.onResetCurrentLine()
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

@ExperimentalComposeUiApi
@ExperimentalAnimationApi
@ExperimentalFoundationApi
@Composable
fun MapBox(
    map: MapView,
    mapViewModel: MapViewModel,
    station: Station,
    onSetStation: () -> Unit,
) {
    DeviceList(mapViewModel)
    Box {
        MapViewContainer(map, mapViewModel, station)
        TopButtonsRow(mapViewModel, station, onSetStation)
        MapControls(mapViewModel)
        DrawingTools(mapViewModel)
    }
}

@Composable
fun MapControls(mapViewModel: MapViewModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.Center
    ) {
        Column(Modifier.padding(4.dp)) {
            IconButton(onClick = { mapViewModel.onSetBoundsStart() }, Modifier
                .padding(4.dp)
                .background(
                    color = androidx.compose.ui.graphics.Color.Gray.copy(alpha = 0.15f), CircleShape
                )
            ) {
                Icon(
                    Icons.Default.Fullscreen,
                    stringResource(R.string.set_map_bounds),
                    Modifier.fillMaxSize(),
                    tint = androidx.compose.ui.graphics.Color.DarkGray
                )
            }

            IconButton(onClick = { mapViewModel.onMoveMapToLastPointStart() },
                Modifier
                    .padding(4.dp)
                    .background(
                        color = androidx.compose.ui.graphics.Color.Gray.copy(alpha = 0.15f),
                        CircleShape
                    )
            ) {
                Icon(
                    Icons.Outlined.LocationOn,
                    stringResource(R.string.move_map_to_last_point),
                    Modifier.fillMaxSize(),
                    tint = androidx.compose.ui.graphics.Color.DarkGray
                )
            }

            IconButton(onClick = { mapViewModel.onZoomInStart() },
                Modifier
                    .padding(4.dp)
                    .background(
                        color = androidx.compose.ui.graphics.Color.Gray.copy(alpha = 0.15f),
                        CircleShape
                    )
            ) {
                Icon(
                    Icons.Default.Add,
                    "",
                    Modifier.fillMaxSize(),
                    tint = androidx.compose.ui.graphics.Color.DarkGray
                )
            }

            IconButton(onClick = { mapViewModel.onZoomOutStart() },
                Modifier
                    .padding(4.dp)
                    .background(
                        color = androidx.compose.ui.graphics.Color.Gray.copy(alpha = 0.15f),
                        CircleShape
                    )
            ) {
                Icon(
                    Icons.Default.Remove,
                    "",
                    Modifier.fillMaxSize(),
                    tint = androidx.compose.ui.graphics.Color.DarkGray
                )
            }
        }
    }
}

@Composable
fun DeviceList(mapViewModel: MapViewModel) {
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
}

@ExperimentalComposeUiApi
@Composable
fun TopButtonsRow(
    mapViewModel: MapViewModel,
    station: Station,
    onSetStation: () -> Unit
) {
    val currentNote = mapViewModel.currentNote
    Column {
        if (currentNote != null) {
            ItemEntryInput(
                stringResource(R.string.message),
                currentNote,
                mapViewModel::onAddMeasurementNote
            )
        }

        Row {
            StationButton(onSetStation = { onSetStation() }, text = station.name)

            Button(onClick = { mapViewModel.onConnectBluetooth() }, Modifier.padding(8.dp)) {
                Icon(
                    Icons.Default.Bluetooth,
                    stringResource(R.string.bluetooth_button_description)
                )
            }
        }
    }
}

@Composable
private fun MapViewContainer(
    map: MapView,
    mapViewModel: MapViewModel,
    station: Station
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val measurements = mapViewModel.measurements.observeAsState(Resource.Loading()).value
    val lines = mapViewModel.lines.observeAsState(Resource.Loading()).value
    val setBounds = mapViewModel.setBounds
    val currentLine = mapViewModel.currentLine
    val refreshAll = mapViewModel.refreshAll
    val currentMarker = mapViewModel.currentMarker
    val updateCurrentMarker = mapViewModel.updateCurrentMarker
    val deleteCurrentMarker = mapViewModel.deleteCurrentMarker
    val newMeasurement = mapViewModel.newMeasurement
    val mapState = mapViewModel.mapState
    val currentPolyline = mapViewModel.currentPolyline
    val deletePolyline = mapViewModel.deleteCurrentPolyline
    val moveMapToLastPoint = mapViewModel.moveMapToLastPoint
    val zoomIn = mapViewModel.zoomIn
    val zoomOut = mapViewModel.zoomOut
    val updatePolyline = mapViewModel.updatePolyline

    AndroidView({ map }) { mapView ->
        coroutineScope.launch {
            val googleMap = mapView.awaitMap()
            googleMap.apply {
                mapType = GoogleMap.MAP_TYPE_NONE
                setPadding(0, 100, 0, 0)

                if (mapState is MapState.LineEdit) {
                    when (currentLine) {
                        is Resource.Success -> {
                            //Continue line marker click listener
                            mapViewModel.setOnLineContinueMarkerClickListener(
                                googleMap, currentLine.data
                            )
                        }
                        is Resource.Loading -> {
                            //New line marker click listener
                            mapViewModel.setOnNewLineMarkerClickListener(googleMap)
                        }
                        is Resource.Failure -> {  }
                    }
                } else {
                    //Default marker click listener
                    mapViewModel.setOnDefaultMarkerClickListener(googleMap)
                }

                setOnMapClickListener {
                    when (mapState) {
                        //Reset current line with click on free space on map
                        MapState.LineEdit -> {
                            mapViewModel.onResetMapState()
                            mapViewModel.onResetCurrentPolyline()
                            mapViewModel.onResetCurrentLine()
                        }
                        //Reset current marker with click on free space on map
                        is MapState.MeasurementEdit -> {
                            mapViewModel.onResetMapState()
                            mapViewModel.onResetCurrentMarker()
                        }
                        MapState.Main -> {}
                    }
                }

                //Polyline click listener
                setOnPolylineClickListener { line ->
                    if (mapState !is MapState.LineEdit) {
                        mapViewModel.onSetCurrentPolyline(line)
                        mapViewModel.onSetCurrentLine(line.tag!!.toString())
                        mapViewModel.onSetMapState(MapState.LineEdit)
                    }
                }

                if (currentPolyline != null) {
                    if (updatePolyline) {
                        //Add new point to polyline
                        mapViewModel.onContinueCurrentPolyline(context, googleMap, currentPolyline)
                        mapViewModel.onUpdatePolylineComplete()
                    }
                    if (deletePolyline) {
                        mapViewModel.onDeletePolylineMarkers(currentPolyline.tag!!.toString())
                        mapViewModel.onResetCurrentPolyline()
                        currentPolyline.remove()
                        mapViewModel.onDeleteCurrentPolylineComplete()
                    }
                } else {
                    if (updatePolyline) {
                        //Add new polyline
                        mapViewModel.onCreateNewPolyline(context, googleMap)
                        mapViewModel.onUpdatePolylineComplete()
                    }
                }

                if (currentMarker != null) {
                    //Update marker
                    if (updateCurrentMarker) {
                        mapViewModel.onUpdateCurrentMarkerType(
                            googleMap,
                            context,
                            currentMarker
                        )
                    }
                    //Delete marker
                    if (deleteCurrentMarker) {
                        currentMarker.remove()
                        mapViewModel.onDeleteCurrentMarkerComplete()
                        mapViewModel.onResetCurrentMarker()
                    }
                }

                //Add new marker
                if (newMeasurement != null) {
                    mapViewModel.onCreateNewMarker(googleMap, context, newMeasurement)
                    if (mapState is MapState.LineEdit && newMeasurement.type == PointType.Point) {
                        when (currentLine) {
                            is Resource.Success -> {
                                //Continue line
                                mapViewModel.continueLine(currentLine.data, newMeasurement.id)
                            }
                            is Resource.Loading -> {
                                //New line
                                mapViewModel.createNewLine(newMeasurement.id)
                            }
                            is Resource.Failure -> {  }
                        }
                    }
                    mapViewModel.onResetNewMeasurement()
                    mapViewModel.onUpdateNewMeasurementsList(newMeasurement)
                }

                //Add new measurement with long click on map
                setOnMapLongClickListener {
                    if (measurements is Resource.Success) {
                        val maxNumber = measurements.data.maxByOrNull {
                                measurement -> measurement.number
                        }?.number ?: 0
                        mapViewModel.addMeasurement(
                            Measurement(
                                stationId = station.id,
                                isMeasured = false,
                                number = mapViewModel.getNewNumber(),
                                latitude = it.latitude,
                                longitude = it.longitude,
                                type = mapViewModel.currentPointObject,
                                name = it.toString()
                            ),
                            station.id
                        )
                    }
                }

                //Set Bounds
                if (setBounds) {
                    mapViewModel.onSetBounds(googleMap)
                }

                //Move map to last point
                if (moveMapToLastPoint) {
                    mapViewModel.onMoveMapToLastPoint(googleMap)
                }

                //ZoomIn
                if (zoomIn) {
                    animateCamera(CameraUpdateFactory.zoomIn())
                    mapViewModel.onZoomInComplete()
                }

                //ZoomOut
                if (zoomOut) {
                    animateCamera(CameraUpdateFactory.zoomOut())
                    mapViewModel.onZoomOutComplete()
                }

                //Initial draw all objects on map
                if (refreshAll) {
                    if (measurements is Resource.Success && lines is Resource.Success) {
                        moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(
                                    measurements.data.first().latitude,
                                    measurements.data.first().longitude
                                ),
                                InitialZoom
                            )
                        )
                        mapViewModel.drawAll(context, googleMap, measurements.data, lines.data)
                        mapViewModel.onRefreshAllComplete()
//                        mapViewModel.onSetBounds(googleMap)
                    }
                }
            }
        }
    }
}

@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun DrawingTools(mapViewModel: MapViewModel) {

    val lines = remember { LineType.values() }
    var showLines by remember { mutableStateOf(false) }
    val currentLineType = mapViewModel.currentLineType
    var showPointObjects by remember { mutableStateOf(false) }
    val pointObjects = remember { PointType.values() }
    var showColors by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        AnimatedVisibility(visible = showColors) {
            LazyVerticalGrid(cells = GridCells.Fixed(4)) {
                items(colorList.reversed()) { color ->
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
        AnimatedVisibility(visible = showLines) {
            LazyVerticalGrid(cells = GridCells.Fixed(4), Modifier.padding(4.dp)) {
                items(lines.reversed()) { line ->
                    FloatingActionButton(
                        onClick = {
                            mapViewModel.onResetCurrentPointObject()
                            mapViewModel.onSetCurrentLineType(line)
                            mapViewModel.onSetMapState(MapState.LineEdit)
                            showLines = false
                        },
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Icon(
                            painter = painterResource(line.lineVectorResId),
                            contentDescription = stringResource(line.lineContentDescription)
                        )
                    }
                }
            }
        }
        AnimatedVisibility(visible = showPointObjects) {
            LazyVerticalGrid(cells = GridCells.Fixed(6)) {
                items(pointObjects.reversed()) { pointObject ->
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
        Row(Modifier.padding(16.dp)) {
            FloatingActionButton(
                onClick = {
                    showLines = false
                    showPointObjects = false
                    showColors = !showColors
                },
                backgroundColor = androidx.compose.ui.graphics.Color(mapViewModel.currentColor),
                modifier = Modifier.padding(8.dp)
            ) {
            }

            FloatingActionButton(
                onClick = {
                    showColors = false
                    showPointObjects = false
                    showLines = !showLines
                          },
                modifier = Modifier.padding(8.dp)
            ) {
                if (currentLineType == null) {
                    Text(text = "Line")
                } else {
                    Icon(
                        painter = painterResource(id = currentLineType.lineVectorResId),
                        contentDescription = stringResource(
                            id = currentLineType.lineContentDescription
                        )
                    )
                }
            }
            FloatingActionButton(
                onClick = {
                    showLines = false
                    showColors = false
                    showPointObjects = !showPointObjects
                },
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
    }
}