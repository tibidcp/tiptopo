package com.tibi.core.interactors

import com.tibi.core.data.ProjectRepository

class GetProject(private val projectRepository: ProjectRepository) {
    suspend operator fun invoke(name: String) = projectRepository.getProject(name)
}
