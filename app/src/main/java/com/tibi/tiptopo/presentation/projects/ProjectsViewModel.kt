package com.tibi.tiptopo.presentation.projects

import android.content.SharedPreferences
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import androidx.lifecycle.*
import com.tibi.tiptopo.MainDestinations.ProjectIdKey
import com.tibi.tiptopo.MainDestinations.TotalStationKey
import com.tibi.tiptopo.data.Resource
import com.tibi.tiptopo.data.project.ProjectRepository
import com.tibi.tiptopo.domain.Project
import com.tibi.tiptopo.domain.TotalStation
import com.tibi.tiptopo.presentation.login.FirebaseUserLiveData
import com.tibi.tiptopo.presentation.map.MapState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProjectsViewModel @Inject constructor(
    private val projectRepository: ProjectRepository,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {

    @Inject lateinit var authenticationState: LiveData<FirebaseUserLiveData.AuthenticationState>

    private val refreshTrigger = MutableLiveData(Unit)

    var selectedTS by mutableStateOf(TotalStation.Nikon)
        private set

    val projects = refreshTrigger.switchMap {
        liveData {
            emit(Resource.Loading())
            projectRepository.getAllProjects().collect { emit(it) }
        }
    }

    fun onSelectTS(totalStation: TotalStation) {
        selectedTS = totalStation
    }

    fun addProject(name: String) {
        viewModelScope.launch {
            projectRepository.addProject(Project(name = name, totalStation = selectedTS))
            refresh()
        }
    }

    fun saveSharedPrefData(projectId: String, totalStation: TotalStation) {
        sharedPreferences.edit(commit = true) { putString(ProjectIdKey, projectId) }
        sharedPreferences.edit(commit = true) { putString(TotalStationKey, totalStation.name) }
    }

    private fun refresh() {
        refreshTrigger.value = Unit
    }
}
