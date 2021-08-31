package com.tibi.tiptopo.presentation.projects

import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.*
import com.tibi.tiptopo.MainDestinations.ProjectIdKey
import com.tibi.tiptopo.data.Resource
import com.tibi.tiptopo.data.project.ProjectRepository
import com.tibi.tiptopo.domain.Project
import com.tibi.tiptopo.presentation.login.FirebaseUserLiveData
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

    val projects = refreshTrigger.switchMap {
        liveData {
            emit(Resource.Loading())
            projectRepository.getAllProjects().collect { emit(it) }
        }
    }


    fun addProject(name: String) {
        viewModelScope.launch {
            projectRepository.addProject(Project(name = name))
            refresh()
        }
    }

    fun saveProjectId(projectId: String) {
        sharedPreferences.edit(commit = true) { putString(ProjectIdKey, projectId) }
    }

    private fun refresh() {
        refreshTrigger.value = Unit
    }
}
