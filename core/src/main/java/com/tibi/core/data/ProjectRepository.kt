package com.tibi.core.data

import com.tibi.core.domain.Project
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class ProjectRepository(
    private val ioDispatcher: CoroutineDispatcher,
    private val dataSource: ProjectDataSource
    ) {
    suspend fun addProject(project: Project) =
        withContext(ioDispatcher) { dataSource.addProject(project) }

    suspend fun getProject(project: Project) =
        withContext(ioDispatcher) { dataSource.getProject(project) }

    suspend fun updateProject(project: Project) =
        withContext(ioDispatcher) { dataSource.updateProject(project) }

    suspend fun getAllProjects() = withContext(ioDispatcher) { dataSource.getAllProjects() }
}
