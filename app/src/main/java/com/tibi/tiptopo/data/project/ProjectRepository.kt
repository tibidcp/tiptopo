package com.tibi.tiptopo.data.project

import com.tibi.tiptopo.domain.Project
import com.tibi.tiptopo.presentation.di.IoDispatcher
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class ProjectRepository @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val dataSource: ProjectDataSource
) {

    suspend fun addProject(project: Project) =
        withContext(ioDispatcher) { dataSource.addProject(project) }

    suspend fun getProject(projectId: String) =
        withContext(ioDispatcher) { dataSource.getProject(projectId) }

    suspend fun updateProject(project: Project) =
        withContext(ioDispatcher) { dataSource.updateProject(project) }

    suspend fun getAllProjects() = withContext(ioDispatcher) { dataSource.getAllProjects() }
}
