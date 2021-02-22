package com.tibi.core.interactors

import com.tibi.core.data.ProjectRepository
import com.tibi.core.domain.Project

class AddProject(private val projectRepository: ProjectRepository) {
    suspend operator fun invoke(project: Project) = projectRepository.addProject(project)
}
