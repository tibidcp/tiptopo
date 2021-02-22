package com.tibi.core.data

import com.tibi.core.domain.Project

interface ProjectDataSource {
    suspend fun addProject(project: Project): Resource<Project>
    suspend fun getProject(name: String): Resource<Project>
    suspend fun updateProject(project: Project): Resource<Project>
    suspend fun getAllProjects(): Resource<MutableList<Project>>
}
