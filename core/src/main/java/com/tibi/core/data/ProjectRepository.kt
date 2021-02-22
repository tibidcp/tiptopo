package com.tibi.core.data

import com.tibi.core.domain.Project

class ProjectRepository(private val dataSource: ProjectDataSource) {
    suspend fun addProject(project: Project) = dataSource.addProject(project)
    suspend fun getProject(name: String) = dataSource.getProject(name)
    suspend fun updateProject(project: Project) = dataSource.updateProject(project)
    suspend fun getAllProjects() = dataSource.getAllProjects()
}
