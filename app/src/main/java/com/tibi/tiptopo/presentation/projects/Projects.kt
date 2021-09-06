package com.tibi.tiptopo.presentation.projects

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.firebase.ui.auth.AuthUI
import com.tibi.tiptopo.R
import com.tibi.tiptopo.data.Resource
import com.tibi.tiptopo.domain.Project
import com.tibi.tiptopo.presentation.login.FirebaseUserLiveData
import com.tibi.tiptopo.presentation.login.FirebaseUserLiveData.AuthenticationState.AUTHENTICATED
import com.tibi.tiptopo.presentation.login.FirebaseUserLiveData.AuthenticationState.UNAUTHENTICATED
import com.tibi.tiptopo.presentation.ui.ProgressCircular
import com.tibi.tiptopo.presentation.ui.ItemEntryInput
import java.text.SimpleDateFormat
import java.util.*

@ExperimentalFoundationApi
@ExperimentalMaterialApi
@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@Composable
fun Projects(
    projectsViewModel: ProjectsViewModel,
    onProjectSelected: () -> Unit,
    onLogOut: () -> Unit
) {
    val authState: FirebaseUserLiveData.AuthenticationState by
    projectsViewModel.authenticationState.observeAsState(AUTHENTICATED)

    when (authState) {
        AUTHENTICATED -> {
            ProjectScreen(
                projectsViewModel = projectsViewModel,
                onProjectSelected = onProjectSelected
            )
        }
        UNAUTHENTICATED -> onLogOut()
    }
}

@ExperimentalMaterialApi
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@Composable
fun ProjectScreen(
    projectsViewModel: ProjectsViewModel,
    onProjectSelected: () -> Unit
) {
    val projectItemsState = projectsViewModel.projects.observeAsState(Resource.Loading())
    val selectedTS = projectsViewModel.selectedTS
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.projects)) },
                actions = {
                    val context = LocalContext.current
                    IconButton(onClick = { AuthUI.getInstance().signOut(context) }) {
                        Icon(Icons.Default.Logout, stringResource(R.string.logout_description))
                    }
                }
            )
        }
    ) {
        Column {
            ItemEntryInput(
                label = stringResource(R.string.project_name),
                showTS = true,
                selectedChip = selectedTS,
                onChipSelected = projectsViewModel::onSelectTS,
                onItemComplete = projectsViewModel::addProject
            )
            when (val projectItems = projectItemsState.value) {
                is Resource.Success -> {
                    LazyColumn {
                        items(projectItems.data.sortedByDescending { it.date }) { project ->
                            ProjectRow(
                                project = project,
                                {
                                    projectsViewModel.saveSharedPrefData(
                                        project.id,
                                        project.totalStation
                                    )
                                    onProjectSelected()
                                },
                                Modifier.fillParentMaxWidth()
                            )
                        }
                    }
                }
                is Resource.Loading -> { ProgressCircular() }
                is Resource.Failure -> { Text(text = stringResource(R.string.error)) }
            }
        }
    }
}

@Composable
fun ProjectRow(
    project: Project,
    onItemClicked: (Project) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier
            .clickable { onItemClicked(project) }
            .padding(start = 8.dp)) {
        Text(text = project.name, fontWeight = FontWeight.Bold)
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
            Text(project.totalStation.name, style = MaterialTheme.typography.body2)
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = project.date?.time ?: 0
            val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
            val date = formatter.format(calendar.time)
            Text(date, style = MaterialTheme.typography.body2)
        }
    }
}
