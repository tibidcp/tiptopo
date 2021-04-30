package com.tibi.tiptopo.presentation.di

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.tibi.tiptopo.MainDestinations.ProjectIdKey
import com.tibi.tiptopo.data.line.LineDataSource
import com.tibi.tiptopo.data.measurement.MeasurementDataSource
import com.tibi.tiptopo.data.project.ProjectDataSource
import com.tibi.tiptopo.data.station.StationDataSource
import com.tibi.tiptopo.framework.FirestoreLineDataSource
import com.tibi.tiptopo.framework.FirestoreMeasurementDataSource
import com.tibi.tiptopo.framework.FirestoreProjectDataSource
import com.tibi.tiptopo.framework.FirestoreStationDataSource
import com.tibi.tiptopo.presentation.login.FirebaseUserLiveData
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier

@InstallIn(ViewModelComponent::class)
@Module
abstract class ProjectDataSourceModule {
    @ViewModelScoped
    @Binds
    abstract fun bindProjectDataSource(impl: FirestoreProjectDataSource):
            ProjectDataSource
}

@InstallIn(ViewModelComponent::class)
@Module
abstract class StationDataSourceModule {
    @ViewModelScoped
    @Binds
    abstract fun bindStationDataSource(impl: FirestoreStationDataSource):
            StationDataSource
}

@InstallIn(ViewModelComponent::class)
@Module
abstract class MeasurementDataSourceModule {
    @ViewModelScoped
    @Binds
    abstract fun bindMeasurementDataSource(impl: FirestoreMeasurementDataSource):
            MeasurementDataSource
}

@InstallIn(ViewModelComponent::class)
@Module
abstract class LineDataSourceModule {
    @ViewModelScoped
    @Binds
    abstract fun bindLineDataSource(impl: FirestoreLineDataSource):
            LineDataSource
}

@Module
@InstallIn(ViewModelComponent::class)
object DispatcherModule {
    @IoDispatcher
    @Provides
    @ViewModelScoped
    fun providesIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
}

@Module
@InstallIn(ViewModelComponent::class)
object FirebaseUserLivedataModule {
    @Provides
    @ViewModelScoped
    fun providesFirebaseUserLivedata(): LiveData<FirebaseUserLiveData.AuthenticationState> =
        FirebaseUserLiveData().map { user ->
            if (user != null) {
                FirebaseUserLiveData.AuthenticationState.AUTHENTICATED
            } else {
                FirebaseUserLiveData.AuthenticationState.UNAUTHENTICATED
            }
        }
}

@Module
@InstallIn(SingletonComponent::class)
object ProjectIdModule {
    @Provides
    @CurrentProjectId
    fun providesProjectId(@ApplicationContext context: Context) =
        context
            .getSharedPreferences("com.tibi.tiptopo", Context.MODE_PRIVATE)
            .getString(ProjectIdKey, "") ?: ""
}

@Module
@InstallIn(SingletonComponent::class)
object SharedPreferencesModule {
    @Provides
    fun providesSharedPreferences(@ApplicationContext context: Context) =
        context
            .getSharedPreferences("com.tibi.tiptopo", Context.MODE_PRIVATE)
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class CurrentProjectId
