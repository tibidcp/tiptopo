package com.tibi.tiptopo.presentation.projects

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import com.tibi.tiptopo.presentation.login.FirebaseUserLiveData
import com.tibi.tiptopo.presentation.login.FirebaseUserLiveData.AuthenticationState.AUTHENTICATED
import com.tibi.tiptopo.presentation.login.FirebaseUserLiveData.AuthenticationState.UNAUTHENTICATED

@Composable
fun Projects(projectsViewModel: ProjectsViewModel, selectProject: (String) -> Unit, onLogOut: () -> Unit) {

    val authState: FirebaseUserLiveData.AuthenticationState by
    projectsViewModel.authenticationState.observeAsState(AUTHENTICATED)

    when (authState) {
        AUTHENTICATED -> {
            Button(onClick = { selectProject("123") }) {
                Text("Map")
            }
        }
        UNAUTHENTICATED -> onLogOut()
    }
}
