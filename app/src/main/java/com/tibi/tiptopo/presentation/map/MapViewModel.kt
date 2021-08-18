package com.tibi.tiptopo.presentation.map

import android.app.Activity
import android.app.Application
import android.content.Context
import android.graphics.Color
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.*
import app.akexorcist.bluetotohspp.library.BluetoothSPP
import app.akexorcist.bluetotohspp.library.BluetoothSPP.BluetoothConnectionListener
import app.akexorcist.bluetotohspp.library.BluetoothState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.Polyline
import com.google.maps.android.ktx.addMarker
import com.google.maps.android.ktx.addPolyline
import com.squareup.moshi.*
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.tibi.tiptopo.data.Resource
import com.tibi.tiptopo.data.line.LineRepository
import com.tibi.tiptopo.data.measurement.MeasurementRepository
import com.tibi.tiptopo.data.project.ProjectRepository
import com.tibi.tiptopo.data.station.StationRepository
import com.tibi.tiptopo.domain.*
import com.tibi.tiptopo.presentation.bitmapDescriptorFromVector
import com.tibi.tiptopo.presentation.di.CurrentProjectId
import com.tibi.tiptopo.presentation.format
import com.tibi.tiptopo.presentation.getCoordinate
import com.tibi.tiptopo.presentation.login.FirebaseUserLiveData
import com.tibi.tiptopo.presentation.parser.NikonRawParser
import com.tibi.tiptopo.presentation.toLatLng
import com.tibi.tiptopo.presentation.toRawDegrees
import com.tibi.tiptopo.presentation.ui.setPolylinePattern
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val projectRepository: ProjectRepository,
    private val stationRepository: StationRepository,
    private val measurementRepository: MeasurementRepository,
    private val lineRepository: LineRepository,
    private val bluetooth: BluetoothSPP,
    @CurrentProjectId private val projectId: String,
    application: Application
) : AndroidViewModel(application) {

    @Inject lateinit var authenticationState: LiveData<FirebaseUserLiveData.AuthenticationState>

    private val newMeasurements = mutableListOf<Measurement>()

    var mapState by mutableStateOf<MapState>(MapState.Main)
        private set

    var currentLine by mutableStateOf<Resource<Line>>(Resource.Loading())
        private set

    var currentPolyline: Polyline? by mutableStateOf(null)
        private set

    var currentMarker: Marker? by mutableStateOf(null)
        private set

    var newMeasurement: Measurement? by mutableStateOf(null)
        private set

    var updateCurrentMarker by mutableStateOf(false)
        private set

    var rawText by mutableStateOf("")
        private set

    var linearJsonText by mutableStateOf("")
        private set

    var measurementJsonText by mutableStateOf("")
        private set

    var deleteCurrentMarker by mutableStateOf(false)
        private set

    var deleteCurrentPolyline by mutableStateOf(false)
        private set

    var refreshAll by mutableStateOf(true)
        private set

    var setBounds by mutableStateOf(false)
        private set

    var showDeviceList by mutableStateOf(false)
        private set

    var currentPointObject by mutableStateOf(PointType.Point)
        private set

    var currentLineType: LineType? by mutableStateOf(null)
        private set

    var currentColor by mutableStateOf(Color.BLACK)
        private set

    var showToast by mutableStateOf("")
        private set

    private val refreshTrigger = MutableLiveData(Unit)

    val currentProject = liveData {
        emit(Resource.Loading())
        try {
            emit(projectRepository.getProject(projectId))
        } catch (e: Exception) {
            Resource.Failure<Resource<Project>>(e)
        }
    }

    val measurements = refreshTrigger.switchMap {
        liveData {
            measurementRepository.getAllMeasurements().collect { emit(it) }
        }
    }

    val lines = refreshTrigger.switchMap {
        liveData {
            lineRepository.getAllLines().collect { emit(it) }
        }
    }

    val stations = refreshTrigger.switchMap {
        liveData {
            stationRepository.getAllStations().collect { emit(it) }
        }
    }

    fun onUpdateNewMeasurementsList(measurement: Measurement) {
        newMeasurements.add(measurement)
    }

    fun onSetMapState(state: MapState) {
        mapState = state
    }

    fun onResetMapState() {
        mapState = MapState.Main
    }

    fun refresh() {
        refreshTrigger.value = Unit
    }

    private fun onSetRawText(text: String) {
        rawText = text
    }

    private fun onSetLinearJsonText(text: String) {
        linearJsonText = text
    }

    private fun onSetMeasurementJsonText(text: String) {
        measurementJsonText = text
    }

    fun onResetExportTexts() {
        rawText = ""
        linearJsonText = ""
        measurementJsonText = ""
    }

    private fun onSetNewMeasurement(measurement: Measurement) {
        newMeasurement = measurement
    }

    private fun onResetNewMeasurement() {
        newMeasurement = null
    }

    fun onSetCurrentPolyline(polyline: Polyline) {
        currentPolyline = polyline
    }

    fun onResetCurrentPolyline() {
        currentPolyline = null
    }

    private fun onSetCurrentMarker(marker: Marker) {
        currentMarker = marker
    }

    fun onResetCurrentMarker() {
        currentMarker = null
    }

    object DateAdapter {
        @FromJson
        fun fromJson(string: String) = Date(string.toLong())
        @ToJson
        fun toJson(value: Date) = value.time.toString()
    }

    fun exportRawFile() {
        val stationsValue = stations.value
        val measurementsValue = measurements.value
        val linesValue = lines.value
        if (stationsValue is Resource.Success && measurementsValue is Resource.Success
            && linesValue is Resource.Success) {
            val builderRaw = StringBuilder()

            stationsValue.data.sortedBy { it.date }.forEach { station ->
                val backsight = measurementsValue.data.firstOrNull { it.id == station.backsightId }
                builderRaw.append("ST,${station.name},,,,0.000,0.0000,0.0000\n")
                if (backsight != null) {
                    builderRaw.append(
                        "SS,S${backsight.number}," +
                                "${station.hi.format()},${station.backsightSD.format()}," +
                                "${station.backsightHA.toRawDegrees()}," +
                                "${station.backsightVA.toRawDegrees()},00:00:00,\n"
                    )
                }
                measurementsValue.data.filter { it.stationId == station.id }.sortedBy { it.date }
                    .forEach {
                        var name = it.number.toString()
                        if (it.type == PointType.Station) {
                            name = "S$name"
                        }
                        builderRaw.append(
                            "SS,$name," +
                                    "${station.hi.format()},${it.sd.format()}," +
                                    "${it.ha.toRawDegrees()},${it.va.toRawDegrees()},00:00:00,\n"
                        )
                    }
            }

            val moshi = Moshi.Builder()
                .add(DateAdapter)
                .addLast(KotlinJsonAdapterFactory())
                .build()
            val type1 = Types.newParameterizedType(List::class.java, Measurement::class.java)
            val jsonAdapter1: JsonAdapter<List<Measurement>> = moshi.adapter(type1)
            val measurementJson = jsonAdapter1.indent("  ").toJson(measurementsValue.data)
            onSetMeasurementJsonText(measurementJson)
            val type2 = Types.newParameterizedType(
                List::class.java,
                Line::class.java,
                Vertex::class.java
            )
            val jsonAdapter2: JsonAdapter<List<Line>> = moshi.adapter(type2)
            val linearJson = jsonAdapter2.indent("  ").toJson(linesValue.data)
            onSetLinearJsonText(linearJson)


            measurementsValue.data.sortedBy { it.date }

            onSetRawText(builderRaw.toString())
        }
    }

    fun onSetCurrentLineType(lineType: LineType) {
        currentLineType = lineType
    }

    private fun onResetCurrentLineType() {
        currentLineType = null
    }

    fun onResetCurrentPointObject() {
        currentPointObject = PointType.Point
    }

    fun addMeasurement(measurement: Measurement, stationId: String) {
        measurement.stationId = stationId
        onSetNewMeasurement(measurement)
        viewModelScope.launch {
            measurementRepository.addMeasurement(measurement)
        }
    }

    private fun addLine(line: Line) {
        line.color = currentColor
        currentLine = Resource.Success(line)
        viewModelScope.launch {
            lineRepository.addLine(line)
        }
    }

    private fun updateLine(line: Line) {
        currentLine = Resource.Loading()
        currentLine = Resource.Success(line)
        viewModelScope.launch {
            lineRepository.updateLine(line)
        }
    }

    fun onSetBoundsStart() {
        setBounds = true
    }

    private fun onSetBoundsComplete() {
        setBounds = false
    }

    private fun onUpdateCurrentMarker() {
        updateCurrentMarker = true
    }

    private fun onUpdateCurrentMarkerComplete() {
        updateCurrentMarker = false
    }

    private fun onDeleteCurrentMarker() {
        deleteCurrentMarker = true
    }

    fun onDeleteCurrentMarkerComplete() {
        deleteCurrentMarker = false
    }

    fun onRefreshAllComplete() {
        refreshAll = false
    }

    fun onSetCurrentPointObject(pointObject: PointType) {
        currentPointObject = pointObject
    }

    fun onSetCurrentColor(color: Int) {
        currentColor = color
    }

    fun onSetCurrentLine(lineId: String) {
        viewModelScope.launch {
            currentLine = try {
                lineRepository.getLine(lineId)
            } catch (e: Exception) {
                Resource.Failure(e)
            }
        }
    }

    fun onResetCurrentLine() {
        currentLine = Resource.Loading()
    }

    fun onDeleteLine(lineId: String) {
        deleteCurrentPolyline = true
        viewModelScope.launch {
            lineRepository.deleteLine(lineId)
        }
    }

    fun onDeleteCurrentPolylineComplete() {
        deleteCurrentPolyline = false
    }

    fun onDeleteSelectedMeasurement() {
        onDeleteCurrentMarker()
        val state = mapState
        viewModelScope.launch {
            if (state is MapState.MeasurementEdit) {
                measurementRepository.deleteMeasurement(state.id)
            }
        }
        mapState = MapState.Main
    }

    fun onConnectBluetooth() {
        if (!bluetooth.isBluetoothAvailable) {
            showToast = "Bluetooth is not available"
        } else if (!bluetooth.isBluetoothEnabled) {
            showToast = "Bluetooth is not enabled"
        } else {
            bluetooth.setupService()
            bluetooth.startService(BluetoothState.DEVICE_OTHER)

            bluetooth.setBluetoothConnectionListener(object : BluetoothConnectionListener {
                override fun onDeviceConnected(name: String?, address: String?) {
                    showToast = "Device connected"
                }

                override fun onDeviceDisconnected() {
                    showToast = "Device disconnected"
                }

                override fun onDeviceConnectionFailed() {
                    showToast = "Device connection failed"
                }

            })

            showDeviceList = true
        }
    }

    fun setBluetoothDataAndConnectionListener() {
        bluetooth.setOnDataReceivedListener { data, message ->
            Log.i("BluetoothTest", "data: $data; message: $message")
            val parser = NikonRawParser(message)
            autoAddMeasurement(message)
            Log.i("BluetoothTest", "va: ${parser.parseVA()}; ha: ${parser.parseHA()}; sd: ${parser.parseSD()}")
        }
    }

    fun onStopShowToast() {
        showToast = ""
    }

    fun onStopShowDeviceList(result: ActivityResult) {
        showDeviceList = false
        if (result.resultCode == Activity.RESULT_OK) {
            bluetooth.connect(result.data)
        }
    }

    private fun autoAddMeasurement(message: String) {
        val parser = NikonRawParser(message)
        val stationsValue = stations.value
        val allMeasurementsValue = measurements.value
        if (stationsValue is Resource.Success && allMeasurementsValue is Resource.Success) {
            val maxNumber = allMeasurementsValue.data.maxByOrNull { it.number }?.number ?: 0
            val station = stationsValue.data.sortedByDescending { it.date }.first()
            val va = parser.parseVA()
            val ha = parser.parseHA()
            val sd = parser.parseSD()
            val latLng = station.getCoordinate(ha, va, sd).toLatLng()

            val measurement =
                Measurement(
                    stationId = station.id,
                    type = currentPointObject,
                    number = maxNumber + 1,
                    rawString = message,
                    va = va,
                    ha = ha,
                    sd = sd,
                    latitude = latLng.latitude,
                    longitude = latLng.longitude
                )

            onSetNewMeasurement(measurement)

            viewModelScope.launch {
                measurementRepository.addMeasurement(
                    measurement
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        bluetooth.setOnDataReceivedListener { _, _ ->  }
    }

    fun onUpdateSelectedMeasurementType() {
        onUpdateCurrentMarker()
        viewModelScope.launch {
            val state = mapState
            if (state is MapState.MeasurementEdit) {
                val id = state.id
                val measurement = measurementRepository.getMeasurement(id)
                if (measurement is Resource.Success) {
                    measurement.data.type = currentPointObject
                    measurementRepository.updateMeasurement(measurement.data)
                }
            }
        }
        mapState = MapState.Main
    }

    fun onDeleteLastVertex(line: Line) {
        val lastVertex = line.vertices.maxByOrNull { it.index }
        if (lastVertex != null) {
            val vertices = line.vertices.toMutableList()
            vertices.remove(lastVertex)
            line.vertices = vertices
            updateLine(line)
        }
    }

    fun onUpdateCurrentLineTypeAndColor(line: Line) {
        val lineType = currentLineType
        if (lineType != null) {
            line.type = lineType
        }
        line.color = currentColor
        updateLine(line)
    }

    fun onReverseCurrentLine(line: Line) {
        if (line.vertices.size < 2) {
            return
        }
        val maxIndexVertex = line.vertices.maxByOrNull { it.index }
        if (maxIndexVertex != null) {
            val maxIndex = maxIndexVertex.index
            line.vertices.forEach { vertex ->
                vertex.index = maxIndex - vertex.index
            }
            updateLine(line)
        }
    }

    fun setOnLineContinueMarkerClickListener(googleMap: GoogleMap, line: Line) {
        googleMap.apply {
            setOnMarkerClickListener { marker ->
                val lastVertex = line.vertices.maxByOrNull { it.index }
                if (lastVertex != null &&
                    lastVertex.measurementId != marker.tag!!.toString()) {
                    val vertices = line.vertices + listOf(Vertex(
                        measurementId = marker.tag!!.toString(),
                        index = lastVertex.index + 1
                    ))
                    line.vertices = vertices
                    updateLine(line)
                }
                true
            }
        }
    }

    fun setOnNewLineMarkerClickListener(googleMap: GoogleMap) {
        googleMap.apply {
            setOnMarkerClickListener { marker ->
                val line = Line(
                    vertices = listOf(
                        Vertex(
                            measurementId = marker.tag!!.toString(),
                            index = 0
                        )
                    ),
                    type = currentLineType ?: LineType.Continuous
                )
                addLine(line)
                true
            }
        }
    }

    fun setOnDefaultMarkerClickListener(googleMap: GoogleMap) {
        googleMap.apply {
            setOnMarkerClickListener { marker ->
                onSetCurrentMarker(marker)
                onSetMapState(MapState.MeasurementEdit(marker.tag!!.toString()))
                marker.showInfoWindow()
                true
            }
        }
    }

    fun onContinueCurrentPolyline(
        googleMap: GoogleMap,
        polyline: Polyline
    ) {
        val measurementsValue = measurements.value
        val lineValue = currentLine
        googleMap.apply {
            if (measurementsValue is Resource.Success && lineValue is Resource.Success) {
                val line = lineValue.data
                val allMeasurements = measurementsValue.data + newMeasurements
                polyline.points.clear()
                polyline.color = line.color
                polyline.setPolylinePattern(line.type)
                polyline.points = line.vertices
                    .sortedBy { it.index }
                    .filter { vertex ->
                        allMeasurements.any {
                            it.id == vertex.measurementId
                        }
                    }
                    .map { vertex ->
                        val measurement = allMeasurements
                            .first { it.id == vertex.measurementId }
                        LatLng(measurement.latitude, measurement.longitude)
                    }
            }
        }
    }

    fun onCreateNewPolyline(googleMap: GoogleMap) {
        val measurementsValue = measurements.value
        val lineValue = currentLine
        googleMap.apply {
            if (measurementsValue is Resource.Success && lineValue is Resource.Success) {
                val line = lineValue.data
                val allMeasurements = measurementsValue.data + newMeasurements

                val polyline = addPolyline {
                    clickable(true)
                    color(line.color)
                    width(5f)

                    line.vertices
                        .sortedBy { it.index }
                        .filter { vertex ->
                            allMeasurements.any {
                                it.id == vertex.measurementId
                            }
                        }
                        .map { vertex ->
                            val measurement = allMeasurements
                                .first { it.id == vertex.measurementId }
                            LatLng(measurement.latitude, measurement.longitude)
                        }.forEach { add(it) }
                }
                polyline.setPolylinePattern(line.type)
                polyline.tag = line.id
                onSetCurrentPolyline(polyline)
            }
        }
    }

    fun onCreateNewMarker(googleMap: GoogleMap, context: Context, newMeasurement: Measurement) {
        googleMap.apply {
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
            onResetNewMeasurement()
        }
    }

    fun onUpdateCurrentMarkerType(googleMap: GoogleMap, context: Context, currentMarker: Marker) {
        googleMap.apply {
            currentMarker.setIcon(
                bitmapDescriptorFromVector(
                    context,
                    currentPointObject.vectorResId,
                    Color.BLACK
                )
            )
            currentMarker.setAnchor(
                currentPointObject.anchorX,
                currentPointObject.anchorY
            )
            onUpdateCurrentMarkerComplete()
            onResetCurrentMarker()
        }
    }

    fun onSetBounds(googleMap: GoogleMap) {
        val measurementsValue = measurements.value
        googleMap.apply {
            if (measurementsValue is Resource.Success) {
                val measurementsData = measurementsValue.data
                val bounds = LatLngBounds.builder()
                measurementsData.forEach {
                        bounds.include(LatLng(it.latitude, it.longitude))
                        animateCamera(CameraUpdateFactory.newLatLngBounds(
                            bounds.build(),
                            20
                        ))
                    }
            }
            onSetBoundsComplete()
        }
    }
}

sealed class MapState {
    object Main : MapState()
    object LineEdit : MapState()
    data class MeasurementEdit(val id: String) : MapState()
}
