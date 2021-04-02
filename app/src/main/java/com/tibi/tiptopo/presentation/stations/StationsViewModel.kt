package com.tibi.tiptopo.presentation.stations

import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.tibi.tiptopo.data.Resource
import com.tibi.tiptopo.data.station.StationRepository
import com.tibi.tiptopo.domain.Station
import com.tibi.tiptopo.presentation.login.FirebaseUserLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StationsViewModel @Inject constructor(
    private val stationRepository: StationRepository,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {

    @Inject
    lateinit var authenticationState: LiveData<FirebaseUserLiveData.AuthenticationState>

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

    var addedStation: Resource<Station> by mutableStateOf(Resource.Loading())
        private set

    fun addStation(station: Station) {
        viewModelScope.launch {
            addedStation = stationRepository.addStation(station)
        }
    }

    fun onStationAdded() {
        addedStation = Resource.Loading()
    }
}
