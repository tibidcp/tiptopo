package com.tibi.tiptopo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navArgument
import androidx.navigation.compose.navigate
import androidx.navigation.compose.rememberNavController
import com.tibi.tiptopo.MainDestinations.ProjectIdKey
import com.tibi.tiptopo.presentation.login.Login
import com.tibi.tiptopo.presentation.login.LoginViewModel
import com.tibi.tiptopo.presentation.map.Map
import com.tibi.tiptopo.presentation.map.MapViewModel
import com.tibi.tiptopo.presentation.projects.Projects
import com.tibi.tiptopo.presentation.projects.ProjectsViewModel

object MainDestinations {
    const val LoginRoute = "login"
    const val ProjectsRoute = "projects"
    const val MapRoute = "map"
    const val ProjectIdKey = "projectId"
}

@ExperimentalComposeUiApi
@Composable
fun NavGraph(
    loginViewModel: LoginViewModel,
    projectsViewModel: ProjectsViewModel,
    mapViewModel: MapViewModel,
    startDestination: String = MainDestinations.LoginRoute
) {
    val navController = rememberNavController()

    val actions = remember(navController) { MainActions(navController) }
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(MainDestinations.LoginRoute) {
            Login(loginViewModel, onLoginComplete = actions.onLoginComplete)
        }
        composable(MainDestinations.ProjectsRoute) {
            Projects(
                projectsViewModel = projectsViewModel,
                selectProject = actions.selectProject,
                onLogOut = actions.onLogOut
            )
        }
        composable(
            "${MainDestinations.MapRoute}/{$ProjectIdKey}",
            arguments = listOf(navArgument(ProjectIdKey) { type = NavType.StringType })
            ) { backStackEntry ->
            val arguments = requireNotNull(backStackEntry.arguments)
            Map(
                mapViewModel = mapViewModel,
                projectId = arguments.getString(ProjectIdKey, ""),
                onLogOut = actions.onLogOut
            )
        }
    }
}

class MainActions(navController: NavHostController) {
    val onLoginComplete: () -> Unit = {
        navController.navigate(MainDestinations.ProjectsRoute) {
            launchSingleTop = true
            popUpTo(0) { inclusive = true }
        }
    }
    val selectProject: (String) -> Unit = { projectId: String ->
        navController.navigate("${MainDestinations.MapRoute}/$projectId")
    }
    val onLogOut: () -> Unit = {
        navController.navigate(MainDestinations.LoginRoute) {
            launchSingleTop = true
            popUpTo(0) { inclusive = true }
        }
    }
}
