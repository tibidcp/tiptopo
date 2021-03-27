package com.tibi.tiptopo.presentation.di

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.tibi.tiptopo.data.project.ProjectDataSource
import com.tibi.tiptopo.framework.FirestoreProjectDataSource
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

@Module
@InstallIn(ViewModelComponent::class)
abstract class ProjectDataSource {
    @Binds
    @ViewModelScoped
    abstract fun bindProjectDataSource(impl: FirestoreProjectDataSource):
            ProjectDataSource
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
