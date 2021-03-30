package com.tibi.tiptopo.presentation.map

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tibi.tiptopo.data.Resource
import com.tibi.tiptopo.data.MapRepository
import com.tibi.tiptopo.domain.Project
import com.tibi.tiptopo.domain.Station
import com.tibi.tiptopo.presentation.login.FirebaseUserLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class MapViewModel @Inject constructor(
    private val projectRepository: MapRepository<Project>,
    private val stationRepository: MapRepository<Station>
) : ViewModel() {

    @Inject lateinit var authenticationState: LiveData<FirebaseUserLiveData.AuthenticationState>

    var currentProject: Resource<Project> by mutableStateOf(Resource.Loading())
        private set

    var currentStation: Resource<Station> by mutableStateOf(Resource.Loading())
        private set

    fun setCurrentProject(projectId: String) {
        viewModelScope.launch {
            currentProject = try {
                projectRepository.get(projectId)
            } catch (e: Exception) {
                Resource.Failure(e)
            }
        }
    }

    fun setCurrentStation(station: Station) {
        viewModelScope.launch {
            currentStation = try {
                stationRepository.add(station)
            } catch (e: Exception) {
                Resource.Failure(e)
            }
        }
    }
}
