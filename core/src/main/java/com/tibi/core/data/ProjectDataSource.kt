package com.tibi.core.data

import com.tibi.core.domain.Project
import kotlinx.coroutines.flow.Flow

interface ProjectDataSource {
    suspend fun addProject(project: Project)
    fun getProject(name: String): Flow<Project?>
    fun updateProject(project: Project)
    fun removeProject(name: String)
    fun getAllProjects(): Flow<Project?>
}
