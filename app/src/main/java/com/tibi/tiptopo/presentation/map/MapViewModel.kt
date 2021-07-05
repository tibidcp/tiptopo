package com.tibi.tiptopo.presentation.map

import android.app.Activity
import android.graphics.Color
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import app.akexorcist.bluetotohspp.library.BluetoothSPP
import app.akexorcist.bluetotohspp.library.BluetoothSPP.BluetoothConnectionListener
import app.akexorcist.bluetotohspp.library.BluetoothState
import com.tibi.tiptopo.data.Resource
import com.tibi.tiptopo.data.line.LineRepository
import com.tibi.tiptopo.data.measurement.MeasurementRepository
import com.tibi.tiptopo.data.project.ProjectRepository
import com.tibi.tiptopo.data.station.StationRepository
import com.tibi.tiptopo.domain.*
import com.tibi.tiptopo.presentation.di.CurrentProjectId
import com.tibi.tiptopo.presentation.getCoordinate
import com.tibi.tiptopo.presentation.login.FirebaseUserLiveData
import com.tibi.tiptopo.presentation.parser.NikonRawParser
import com.tibi.tiptopo.presentation.toLatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val projectRepository: ProjectRepository,
    private val stationRepository: StationRepository,
    private val measurementRepository: MeasurementRepository,
    private val lineRepository: LineRepository,
    private val bluetooth: BluetoothSPP,
    @CurrentProjectId private val projectId: String
) : ViewModel() {

    @Inject lateinit var authenticationState: LiveData<FirebaseUserLiveData.AuthenticationState>

    var currentLine by mutableStateOf<Resource<Line>>(Resource.Loading())
        private set

    var selectedMeasurementId by mutableStateOf("")
        private set

    var drawLine by mutableStateOf(false)
        private set

    var showMeasurements by mutableStateOf(true)
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

    val currentProject = liveData {
        emit(Resource.Loading())
        try {
            emit(projectRepository.getProject(projectId))
        } catch (e: Exception) {
            Resource.Failure<Resource<Project>>(e)
        }
    }

    val currentStation = liveData<Resource<Station>> {
        emit(Resource.Loading())
        try {
            stationRepository.getLastStation().collect {
                emit(it)
            }
        } catch (e: Exception) {
            emit(Resource.Failure(e))
        }
    }

    val measurements = liveData<Resource<List<Measurement>>> {
        emit(Resource.Loading())
        try {
            measurementRepository.getAllMeasurements().collect { emit(it) }
        } catch (e: Exception) {
            emit(Resource.Failure(e))
        }
    }

    val lines = liveData<Resource<List<Line>>> {
        emit(Resource.Loading())
        try {
            lineRepository.getAllLines().collect { emit(it) }
        } catch (e: Exception) {
            emit(Resource.Failure(e))
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

    fun onShowMeasurementsChangeState() {
        showMeasurements = !showMeasurements
    }

    fun addMeasurement(measurement: Measurement, stationId: String) {
        viewModelScope.launch {
            measurement.stationId = stationId
            measurementRepository.addMeasurement(measurement)
        }
    }

    fun addLine(line: Line) {
        viewModelScope.launch {
            currentLine = try {
                line.color = currentColor
                lineRepository.addLine(line)
            } catch (e: Exception) {
                Resource.Failure(e)
            }
        }
    }

    fun updateLine(line: Line) {
        viewModelScope.launch {
            currentLine = try {
                lineRepository.updateLine(line)
            } catch (e: Exception) {
                Resource.Failure(e)
            }
        }
    }

    fun onSetSelectedMeasurementId(id: String) {
        selectedMeasurementId = id
    }

    fun onResetSelectedMeasurementId() {
        selectedMeasurementId = ""
    }

    fun onSetBounds() {
        setBounds = true
    }

    fun onSetBoundsComplete() {
        setBounds = false
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

    fun onDrawLine() {
        drawLine = true
    }

    fun onDrawLineComplete() {
        drawLine = false
        onResetCurrentLine()
        onResetCurrentLineType()
    }

    fun onDeleteLine(lineId: String) {
        viewModelScope.launch {
            lineRepository.deleteLine(lineId)
        }
    }

    fun onDeleteSelectedMeasurement() {
        viewModelScope.launch {
            measurementRepository.deleteMeasurement(selectedMeasurementId)
        }
        onResetSelectedMeasurementId()
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
        val currentStationValue = currentStation.value
        val allMeasurementsValue = measurements.value
        if (currentStationValue is Resource.Success && allMeasurementsValue is Resource.Success) {
            val maxNumber = allMeasurementsValue.data.maxByOrNull { it.number }?.number ?: 0
            val station = currentStationValue.data
            val va = parser.parseVA()
            val ha = parser.parseHA()
            val sd = parser.parseSD()
            val latLng = station.getCoordinate(ha, va, sd).toLatLng()

            viewModelScope.launch {
                measurementRepository.addMeasurement(
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
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        bluetooth.setOnDataReceivedListener { _, _ ->  }
    }

    fun onUpdateSelectedMeasurementType() {
        viewModelScope.launch {
            val measurement = measurementRepository.getMeasurement(selectedMeasurementId)
            if (measurement is Resource.Success) {
                measurement.data.type = currentPointObject
                measurementRepository.updateMeasurement(measurement.data)
            }
        }
        onResetSelectedMeasurementId()
    }
}
