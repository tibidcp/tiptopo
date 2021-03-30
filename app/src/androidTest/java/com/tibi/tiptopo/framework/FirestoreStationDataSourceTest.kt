package com.tibi.tiptopo.framework

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.tibi.tiptopo.data.Resource
import com.tibi.tiptopo.data.MapDataSource
import com.tibi.tiptopo.data.MapRepository
import com.tibi.tiptopo.domain.Station
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

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class FirestoreStationDataSourceTest : TestCase() {

    private val testDispatcher = TestCoroutineDispatcher()
    private val dataSource: MapDataSource<Station> = FirestoreStationDataSource()
    private val repository: MapRepository<Station> = MapRepository(testDispatcher, dataSource)

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
    fun addStationTest() {
        val station = Station(name = "name")
        val expectedName = "name"
        var resultName = ""

        runBlocking {
            val result = repository.add(station)
            if (result is Resource.Success) {
                resultName = result.data.name
            }
            assertEquals(expectedName, resultName)
        }
    }

    @Test
    fun getStationTest() {
        val station = Station(name = "name")
        val expectedName = "name"
        var resultName = ""

        runBlocking {
            repository.add(station)
            val result = repository.get(station.id)
            if (result is Resource.Success) {
                resultName = result.data.name
            }
            assertEquals(expectedName, resultName)
        }
    }

    @Test
    fun updateStationTest() {
        val station = Station(name = "name")
        val expectedName = "name123"
        var resultName = ""

        runBlocking {
            val added = repository.add(station)
            if (added is Resource.Success) {
                added.data.name = expectedName
                val result = repository.update(added.data)
                if (result is Resource.Success) {
                    resultName = result.data.name
                }
            }
            assertEquals(expectedName, resultName)
        }
    }

    @Test
    fun getAllStationsTest() {
        val station1 = Station(name = "name")
        val station2 = Station(name = "name2")
        val expectedSize = 2
        var resultSize = 0

        runBlocking {
            repository.add(station1)
            repository.add(station2)
            val result = repository.getAll().first()
            if (result is Resource.Success) {
                resultSize = result.data.size
            }
            assertEquals(expectedSize, resultSize)
        }
    }
}
