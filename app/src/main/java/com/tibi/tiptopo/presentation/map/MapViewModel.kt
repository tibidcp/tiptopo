package com.tibi.tiptopo.presentation.map

import android.graphics.Color
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.tibi.tiptopo.data.Resource
import com.tibi.tiptopo.data.measurement.MeasurementRepository
import com.tibi.tiptopo.data.project.ProjectRepository
import com.tibi.tiptopo.data.station.StationRepository
import com.tibi.tiptopo.domain.Measurement
import com.tibi.tiptopo.domain.PointType
import com.tibi.tiptopo.domain.Project
import com.tibi.tiptopo.domain.Station
import com.tibi.tiptopo.presentation.di.CurrentProjectId
import com.tibi.tiptopo.presentation.login.FirebaseUserLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val projectRepository: ProjectRepository,
    private val stationRepository: StationRepository,
    private val measurementRepository: MeasurementRepository,
    @CurrentProjectId private val projectId: String
) : ViewModel() {

    @Inject lateinit var authenticationState: LiveData<FirebaseUserLiveData.AuthenticationState>

    var setBounds by mutableStateOf(false)
        private set

    var drawLine by mutableStateOf(false)
        private set

    var currentPointObject by mutableStateOf(PointType.Point)
        private set

    var currentColor by mutableStateOf(Color.BLACK)
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

    fun addMeasurement(measurement: Measurement, stationId: String) {
        viewModelScope.launch {
            measurement.stationId = stationId
            measurementRepository.addMeasurement(measurement)
        }
    }

    fun onSetBounds() {
        setBounds = true
    }

    fun onSetBoundsComplete() {
        setBounds = false
    }

    fun onDrawLine() {
        drawLine = true
    }

    fun onDrawLineComplete() {
        drawLine = false
    }

    fun onSetCurrentPointObject(pointObject: PointType) {
        currentPointObject = pointObject
    }

    fun onSetCurrentColor(color: Int) {
        currentColor = color
    }
}
