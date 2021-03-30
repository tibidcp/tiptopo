package com.tibi.tiptopo.presentation.di

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.tibi.tiptopo.data.MapDataSource
import com.tibi.tiptopo.domain.Project
import com.tibi.tiptopo.domain.Station
import com.tibi.tiptopo.framework.FirestoreProjectDataSource
import com.tibi.tiptopo.framework.FirestoreStationDataSource
import com.tibi.tiptopo.presentation.login.FirebaseUserLiveData
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Qualifier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

@InstallIn(ViewModelComponent::class)
@Module
abstract class ProjectDataSourceModule {
    @ViewModelScoped
    @Binds
    abstract fun bindProjectDataSource(impl: FirestoreProjectDataSource):
            MapDataSource<Project>
}

@InstallIn(ViewModelComponent::class)
@Module
abstract class StationDataSourceModule {
    @ViewModelScoped
    @Binds
    abstract fun bindStationDataSource(impl: FirestoreStationDataSource):
            MapDataSource<Station>
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

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher
