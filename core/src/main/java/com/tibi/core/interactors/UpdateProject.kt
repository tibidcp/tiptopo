package com.tibi.core.interactors

import com.tibi.core.data.ProjectRepository
import com.tibi.core.domain.Project

class UpdateProject(private val projectRepository: ProjectRepository) {
    operator fun invoke(project: Project) = projectRepository.updateProject(project)
}
