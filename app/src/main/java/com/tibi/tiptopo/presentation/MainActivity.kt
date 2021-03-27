package com.tibi.tiptopo.presentation

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.ExperimentalComposeUiApi
import com.tibi.tiptopo.NavGraph
import com.tibi.tiptopo.presentation.login.LoginViewModel
import com.tibi.tiptopo.presentation.map.MapViewModel
import com.tibi.tiptopo.presentation.projects.ProjectsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val projectsViewModel: ProjectsViewModel by viewModels()
    private val loginViewModel: LoginViewModel by viewModels()
    private val mapViewModel: MapViewModel by viewModels()

    @ExperimentalComposeUiApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                NavGraph(
                    loginViewModel = loginViewModel,
                    projectsViewModel = projectsViewModel,
                    mapViewModel = mapViewModel
                )
            }
        }
    }
}
