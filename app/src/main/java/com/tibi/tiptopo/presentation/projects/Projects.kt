package com.tibi.tiptopo.presentation.projects

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.firebase.ui.auth.AuthUI
import com.tibi.tiptopo.R
import com.tibi.tiptopo.data.Resource
import com.tibi.tiptopo.domain.Project
import com.tibi.tiptopo.presentation.login.FirebaseUserLiveData
import com.tibi.tiptopo.presentation.login.FirebaseUserLiveData.AuthenticationState.AUTHENTICATED
import com.tibi.tiptopo.presentation.login.FirebaseUserLiveData.AuthenticationState.UNAUTHENTICATED
import com.tibi.tiptopo.presentation.ui.ProgressCircular
import java.text.SimpleDateFormat
import java.util.*

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

@ExperimentalComposeUiApi
@Composable
fun ProjectScreen(
    projectsViewModel: ProjectsViewModel,
    onProjectSelected: () -> Unit
) {
    val projectItemsState = projectsViewModel.projects.observeAsState(Resource.Loading())
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
            Surface(color = MaterialTheme.colors.onSurface.copy(alpha = 0.05f)) {
                ProjectEntryInput(projectsViewModel::addProject)
            }
            when (val projectItems = projectItemsState.value) {
                is Resource.Success -> {
                    LazyColumn {
                        items(projectItems.data.sortedByDescending { it.date }) { project ->
                            ProjectRow(
                                project = project,
                                {
                                    projectsViewModel.saveProjectId(project.id)
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

@ExperimentalComposeUiApi
@Composable
fun ProjectEntryInput(onProjectComplete: (String) -> Unit) {
    val (text, setText) = remember { mutableStateOf("") }
    val submit = {
        onProjectComplete(text)
        setText("")
    }
    ProjectInput(
        text = text,
        onTextChange = setText,
        submit = submit
    ) {
        TextButton(
            onClick = submit,
            shape = CircleShape,
            enabled = text.isNotBlank()
        ) {
            Text(stringResource(R.string.add))
        }
    }
}

@ExperimentalComposeUiApi
@Composable
fun ProjectInput(
    text: String,
    onTextChange: (String) -> Unit,
    submit: () -> Unit,
    buttonSlot: @Composable () -> Unit
) {
    Column {
        Row(
            Modifier
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp)
        ) {
            ProjectInputText(
                text,
                onTextChange,
                Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                submit
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(Modifier.align(Alignment.CenterVertically)) { buttonSlot() }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@ExperimentalComposeUiApi
@Composable
fun ProjectInputText(
    text: String,
    onTextChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    onImeAction: () -> Unit = {}
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    OutlinedTextField(
        label = { Text(text = stringResource(R.string.project_name)) },
        value = text,
        onValueChange = onTextChange,
        colors = TextFieldDefaults.textFieldColors(backgroundColor = Color.Transparent),
        maxLines = 1,
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = {
            onImeAction()
            keyboardController?.hideSoftwareKeyboard()
        }),
        modifier = modifier
    )
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
            calendar.timeInMillis = project.date!!.time
            val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
            val date = formatter.format(calendar.time)
            Text(date, style = MaterialTheme.typography.body2)
        }
    }
}
