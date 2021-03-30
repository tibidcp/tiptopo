package com.tibi.tiptopo.framework

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.tibi.tiptopo.data.Resource
import com.tibi.tiptopo.data.project.ProjectDataSource
import com.tibi.tiptopo.data.project.ProjectRepository
import com.tibi.tiptopo.domain.Project
import junit.framework.TestCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
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

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class FirestoreProjectDataSourceTest : TestCase() {

    private val testDispatcher = TestCoroutineDispatcher()
    private val dataSource: ProjectDataSource = FirestoreProjectDataSource()
    private val repository: ProjectRepository = ProjectRepository(testDispatcher, dataSource)

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
        val project = Project(name = "name")
        val expectedName = "name"
        var resultName = ""

        runBlocking {
            val result = repository.addProject(project)
            if (result is Resource.Success) {
                resultName = result.data.name
            }
            assertEquals(expectedName, resultName)
        }
    }

    @Test
    fun getProjectTest() {
        val project = Project(name = "name")
        val expectedName = "name"
        var resultName = ""

        runBlocking {
            repository.addProject(project)
            val result = repository.getProject(project.id)
            if (result is Resource.Success) {
                resultName = result.data.name
            }
            assertEquals(expectedName, resultName)
        }
    }

    @Test
    fun updateProjectTest() {
        val project = Project(name = "name")
        val expectedName = "name123"
        var resultName = ""

        runBlocking {
            val added = repository.addProject(project)
            if (added is Resource.Success) {
                added.data.name = expectedName
                val result = repository.updateProject(added.data)
                if (result is Resource.Success) {
                    resultName = result.data.name
                }
            }
            assertEquals(expectedName, resultName)
        }
    }

    @Test
    fun getAllProjectsTest() {
        val project1 = Project(name = "name")
        val project2 = Project(name = "name2")
        val expectedSize = 2
        var resultSize = 0

        runBlocking {
            repository.addProject(project1)
            repository.addProject(project2)
            val result = repository.getAllProjects().first()
            if (result is Resource.Success) {
                resultSize = result.data.size
            }
            assertEquals(expectedSize, resultSize)
        }
    }
}
