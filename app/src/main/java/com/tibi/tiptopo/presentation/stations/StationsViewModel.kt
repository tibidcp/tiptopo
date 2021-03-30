package com.tibi.tiptopo.presentation.stations

import androidx.lifecycle.ViewModel
import com.tibi.tiptopo.data.station.StationRepository
import javax.inject.Inject

class StationsViewModel @Inject constructor(
    private val stationRepository: StationRepository
) : ViewModel() {

    fun setProjectId(projectId: String) {
        stationRepository.setProjectId(projectId)
    }
}