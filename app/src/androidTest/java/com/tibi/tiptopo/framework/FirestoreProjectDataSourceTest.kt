package com.tibi.tiptopo.framework

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.tibi.core.data.ProjectDataSource
import com.tibi.core.data.ProjectRepository
import com.tibi.core.data.Resource
import com.tibi.core.domain.Project
import junit.framework.TestCase
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FirestoreProjectDataSourceTest : TestCase() {

    private val dataSource: ProjectDataSource = FirestoreProjectDataSource()
    private val repository: ProjectRepository = ProjectRepository(dataSource)

    companion object {
        var setUpIsDone = false
    }

    @Before
    fun setup() {
        if (setUpIsDone) {
            return
        }

        val firestore = FirebaseFirestore.getInstance()
        firestore.useEmulator("10.0.2.2", 8080)

        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(false)
            .build()
        firestore.firestoreSettings = settings

        setUpIsDone = true
    }

    @Test
    fun addProjectTest() {
        val name = "name"
        val project = Project(name, 111L)
        val expected = Resource.Success(project)

        runBlocking {
            val result = repository.addProject(project)
            assertEquals(expected, result)
        }
    }

    @Test
    fun getProjectTest() {
        val name = "name"
        val project = Project(name, 111L)
        val expected = Resource.Success(project)

        runBlocking {
            repository.addProject(project)
            val result = repository.getProject(name)
            assertEquals(expected, result)
        }
    }

    @Test
    fun updateProjectTest() {
        val name = "name"
        val project = Project(name, 111L)
        val projectExpected = Project(name, 555L)
        val expected = Resource.Success(projectExpected)

        runBlocking {
            repository.addProject(project)
            val result = repository.updateProject(projectExpected)
            assertEquals(expected, result)
        }
    }

    @Test
    fun getAllProjectsTest() {
        val project1 = Project("name", 111L)
        val project2 = Project("name2", 555L)
        val expected = Resource.Success(mutableListOf(project1, project2))

        runBlocking {
            repository.addProject(project1)
            repository.addProject(project2)
            val result = repository.getAllProjects()
            assertEquals(result, expected)
        }
    }
}
