package com.tibi.core.interactors

import com.tibi.core.data.ProjectRepository

class GetAllProjects(private val projectRepository: ProjectRepository) {
    operator fun invoke() = projectRepository.getAllProjects()
}
