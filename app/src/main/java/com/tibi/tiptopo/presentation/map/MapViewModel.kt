package com.tibi.tiptopo.presentation.map

import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tibi.tiptopo.MainDestinations.ProjectIdKey
import com.tibi.tiptopo.MainDestinations.StationIdKey
import com.tibi.tiptopo.data.Resource
import com.tibi.tiptopo.data.project.ProjectRepository
import com.tibi.tiptopo.data.station.StationRepository
import com.tibi.tiptopo.domain.Project
import com.tibi.tiptopo.domain.Station
import com.tibi.tiptopo.presentation.login.FirebaseUserLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val projectRepository: ProjectRepository,
    private val stationRepository: StationRepository,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {

    @Inject lateinit var authenticationState: LiveData<FirebaseUserLiveData.AuthenticationState>

    var currentProject: Resource<Project> by mutableStateOf(Resource.Loading())
        private set

    var currentStation: Resource<Station> by mutableStateOf(Resource.Loading())
        private set

    fun setCurrentProject() {
        viewModelScope.launch {
            val projectId = sharedPreferences.getString(ProjectIdKey, "") ?: ""
            currentProject = try {
                projectRepository
                    .getProject(projectId)
            } catch (e: Exception) {
                Resource.Failure(e)
            }
        }
    }

    fun setCurrentStation() {
        val stationId = sharedPreferences.getString(StationIdKey, "") ?: ""
        if (stationId.isEmpty()) return
        viewModelScope.launch {
            currentStation = try {
                stationRepository.getStation(stationId)
            } catch (e: Exception) {
                Resource.Failure(e)
            }
        }
    }

    fun setProjectIdToPath() {
        val projectId = sharedPreferences.getString(ProjectIdKey, "") ?: ""
        stationRepository.setProjectId(projectId)
    }

}
