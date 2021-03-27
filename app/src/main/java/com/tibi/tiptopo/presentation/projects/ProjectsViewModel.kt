package com.tibi.tiptopo.presentation.projects

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.tibi.tiptopo.data.Resource
import com.tibi.tiptopo.data.project.ProjectRepository
import com.tibi.tiptopo.domain.Project
import com.tibi.tiptopo.presentation.login.FirebaseUserLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@HiltViewModel
class ProjectsViewModel @Inject constructor(private val projectRepository: ProjectRepository) : ViewModel() {

    @Inject lateinit var authenticationState: LiveData<FirebaseUserLiveData.AuthenticationState>

    val projects = liveData<Resource<List<Project>>> {
        emit(Resource.Loading())
        try {
            projectRepository.getAllProjects().collect {
                emit(it)
            }
        } catch (e: Exception) {
            emit(Resource.Failure(e))
        }
    }

    fun addProject(name: String) {
        viewModelScope.launch {
            projectRepository.addProject(Project(name = name))
        }
    }
}
