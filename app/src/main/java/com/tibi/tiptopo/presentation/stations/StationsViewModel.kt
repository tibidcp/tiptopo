package com.tibi.tiptopo.presentation.stations

import android.content.SharedPreferences
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import app.akexorcist.bluetotohspp.library.BluetoothSPP
import com.google.android.gms.maps.model.LatLng
import com.tibi.tiptopo.data.Resource
import com.tibi.tiptopo.data.measurement.MeasurementRepository
import com.tibi.tiptopo.data.station.StationRepository
import com.tibi.tiptopo.domain.Measurement
import com.tibi.tiptopo.domain.PointType
import com.tibi.tiptopo.domain.Station
import com.tibi.tiptopo.presentation.directAngTo
import com.tibi.tiptopo.presentation.login.FirebaseUserLiveData
import com.tibi.tiptopo.presentation.parser.NikonRawParser
import com.tibi.tiptopo.presentation.toPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StationsViewModel @Inject constructor(
    private val stationRepository: StationRepository,
    private val measurementRepository: MeasurementRepository,
    private val sharedPreferences: SharedPreferences,
    private val bluetooth: BluetoothSPP
) : ViewModel() {

    @Inject
    lateinit var authenticationState: LiveData<FirebaseUserLiveData.AuthenticationState>

    var bluetoothMessage by mutableStateOf("")
        private set

    var waitMeasurement by mutableStateOf(false)
        private set

    var selectedStation: Measurement? by mutableStateOf(null)
        private set

    var selectedBacksight: Measurement? by mutableStateOf(null)
        private set

    val stations = liveData<Resource<List<Station>>> {
        emit(Resource.Loading())
        try {
            stationRepository.getAllStations().collect {
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

    var addedStation: Resource<Station> by mutableStateOf(Resource.Loading())
        private set

    fun addStation(station: Station) {
        viewModelScope.launch {
            addedStation = stationRepository.addStation(station)
            measurementRepository.addMeasurement(
                Measurement(
                    type = PointType.Station,
                    latitude = 55.667005122,
                    longitude = 37.498174661
                )
            )
        }
    }

    fun addSelectedStation() {
        val parser = NikonRawParser(bluetoothMessage)
        if (!parser.isValid()) {
            return
        }
        val stationMeasurement = selectedStation
        val backsight = selectedBacksight
        if (stationMeasurement == null || backsight == null) {
            return
        }
        val stationPoint = LatLng(
            stationMeasurement.latitude,
            stationMeasurement.longitude
        ).toPoint()
        val station = Station(
            name = "S" + stationMeasurement.number,
            backsightId = backsight.id,
            backsightHA = parser.parseHA(),
            backsightVA = parser.parseVA(),
            backsightSD = parser.parseSD(),
            backsightDA = stationMeasurement.directAngTo(backsight),
            x = stationPoint.x,
            y = stationPoint.y
            
        )
        viewModelScope.launch {
            addedStation = stationRepository.addStation(station)
        }
        bluetoothMessage = ""
        selectedBacksight = null
        selectedStation = null
    }

    fun onStationAdded() {
        addedStation = Resource.Loading()
    }

    fun setBluetoothDataListener() {
        bluetooth.setOnDataReceivedListener { data, message ->
            Log.i("BluetoothTest", "StationsViewModel")
            bluetoothMessage = message
        }
    }

    fun onWaitMeasurement() {
        waitMeasurement = true
    }

    fun onStopWaitMeasurement() {
        waitMeasurement = false
    }

    fun onSetSelectedStation(station: Measurement) {
        selectedStation = station
    }

    fun onSetSelectedBacksight(backsight: Measurement) {
        selectedBacksight = backsight
    }
}
