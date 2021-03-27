package com.tibi.tiptopo.data.project

import com.tibi.tiptopo.data.Resource
import com.tibi.tiptopo.domain.Project
import kotlinx.coroutines.flow.Flow

interface ProjectDataSource {
    suspend fun addProject(project: Project): Resource<Project>
    suspend fun getProject(projectId: String): Resource<Project>
    suspend fun updateProject(project: Project): Resource<Project>
    suspend fun getAllProjects(): Flow<Resource<List<Project>>>
}
