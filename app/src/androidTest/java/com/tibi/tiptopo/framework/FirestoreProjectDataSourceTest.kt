package com.tibi.tiptopo.framework

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.tibi.core.data.ProjectDataSource
import com.tibi.core.data.ProjectRepository
import com.tibi.core.domain.Project
import junit.framework.TestCase
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FirestoreProjectDataSourceTest : TestCase() {

    private lateinit var firestore: FirebaseFirestore
    private val dataSource: ProjectDataSource = FirestoreProjectDataSource()
    private val repository: ProjectRepository = ProjectRepository(dataSource)
    private val path = "users/${Firebase.auth.currentUser?.uid!!}/projects"

    @Before
    fun beforeTest() {
        firestore = FirebaseFirestore.getInstance()
        firestore.useEmulator("10.0.2.2", 8080)

        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(false)
            .build()
        firestore.firestoreSettings = settings
    }

    @Test
    fun addProjectTest() {
        val name = "name"
        val expected = Project(name, 111L)

        runBlocking {
            repository.addProject(expected)
            var result: Project? = null
            firestore.collection(path).document(name).get()
                .addOnSuccessListener { documentSnapshot ->
                    result = documentSnapshot.toObject<Project>()
                }.await()
            Assert.assertEquals(expected, result)
        }
    }
}
