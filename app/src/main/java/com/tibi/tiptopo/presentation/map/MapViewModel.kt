package com.tibi.tiptopo.presentation.map

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tibi.tiptopo.data.Resource
import com.tibi.tiptopo.data.project.ProjectRepository
import com.tibi.tiptopo.domain.Project
import com.tibi.tiptopo.presentation.login.FirebaseUserLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class MapViewModel @Inject constructor(private val projectRepository: ProjectRepository) : ViewModel() {

    var currentProject: Resource<Project> by mutableStateOf(Resource.Loading())
        private set

    fun setCurrentProject(projectId: String) {
        viewModelScope.launch {
            currentProject = try {
                projectRepository.getProject(projectId)
            } catch (e: Exception) {
                Resource.Failure(e)
            }
        }
    }

    @Inject lateinit var authenticationState: LiveData<FirebaseUserLiveData.AuthenticationState>
}
