package com.tibi.core.data

import com.tibi.core.domain.Project

class ProjectRepository(private val dataSource: ProjectDataSource) {
    suspend fun addProject(project: Project) = dataSource.addProject(project)
    fun getProject(name: String) = dataSource.getProject(name)
    fun updateProject(project: Project) = dataSource.updateProject(project)
    fun removeProject(name: String) = dataSource.removeProject(name)
    fun getAllProjects() = dataSource.getAllProjects()
}
