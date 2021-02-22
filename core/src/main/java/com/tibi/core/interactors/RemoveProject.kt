package com.tibi.core.interactors

import com.tibi.core.data.ProjectRepository

class RemoveProject(private val projectRepository: ProjectRepository) {
    operator fun invoke(name: String) = projectRepository.removeProject(name)
}
