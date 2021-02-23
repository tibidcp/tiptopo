package com.tibi.core.data

import com.tibi.core.domain.Project

interface ProjectDataSource {
    suspend fun addProject(project: Project): Resource<Project>
    suspend fun getProject(project: Project): Resource<Project>
    suspend fun updateProject(project: Project): Resource<Project>
    suspend fun getAllProjects(): Resource<List<Project>>
}
