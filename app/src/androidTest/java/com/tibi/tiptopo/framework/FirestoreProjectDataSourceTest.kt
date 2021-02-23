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
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Retrofit
import retrofit2.http.DELETE

interface RetrofitApiService {
    @DELETE("documents")
    suspend fun clearDb()
}

@RunWith(AndroidJUnit4::class)
class FirestoreProjectDataSourceTest : TestCase() {

    private val dataSource: ProjectDataSource = FirestoreProjectDataSource()
    private val repository: ProjectRepository = ProjectRepository(dataSource)

    private val retrofit = Retrofit.Builder()
        .baseUrl("http://10.0.2.2:8080/emulator/v1/projects/tiptopo/databases/(default)/")
        .build()
        .create(RetrofitApiService::class.java)

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

    @After
    fun after() {
        runBlocking {
            retrofit.clearDb()
        }
    }

    @Test
    fun addProjectTest() {
        val project = Project(name = "name", date = 111L)
        val expected = Resource.Success(project)

        runBlocking {
            val result = repository.addProject(project)
            if (result is Resource.Success) {
                project.id = result.data.id
            }
            assertEquals(expected, result)
        }
    }

    @Test
    fun getProjectTest() {
        val project = Project(name = "name", date = 111L)
        val expected = Resource.Success(project)

        runBlocking {
            val added = repository.addProject(project)
            if (added is Resource.Success) {
                project.id = added.data.id
            }
            val result = repository.getProject(project)
            assertEquals(expected, result)
        }
    }

    @Test
    fun updateProjectTest() {
        val project = Project(name = "name", date = 111L)
        val projectExpected = Project(name = "name", date = 555L)
        val expected = Resource.Success(projectExpected)

        runBlocking {
            val added = repository.addProject(project)
            if (added is Resource.Success) {
                projectExpected.id = added.data.id
            }
            val result = repository.updateProject(projectExpected)
            assertEquals(expected, result)
        }
    }

    @Test
    fun getAllProjectsTest() {
        val project1 = Project(name = "name", date = 111L)
        val project2 = Project(name = "name2", date = 555L)
        val expected = Resource.Success(mutableListOf(project1, project2))

        runBlocking {
            val added1 = repository.addProject(project1)
            if (added1 is Resource.Success) {
                project1.id = added1.data.id
            }
            val added2 = repository.addProject(project2)
            if (added2 is Resource.Success) {
                project2.id = added2.data.id
            }
            val result = repository.getAllProjects()
            assertEquals(result, expected)
        }
    }
}
