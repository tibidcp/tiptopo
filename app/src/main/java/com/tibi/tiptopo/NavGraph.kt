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
import com.tibi.tiptopo.presentation.stations.Stations
import com.tibi.tiptopo.presentation.stations.StationsViewModel

object MainDestinations {
    const val LoginRoute = "login"
    const val ProjectsRoute = "projects"
    const val MapRoute = "map"
    const val StationsRoute = "stations"
    const val ProjectIdKey = "projectId"
    const val StationIdKey = "stationId"
}

@ExperimentalComposeUiApi
@Composable
fun NavGraph(
    loginViewModel: LoginViewModel,
    projectsViewModel: ProjectsViewModel,
    mapViewModel: MapViewModel,
    stationsViewModel: StationsViewModel,
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
                onProjectSelected = actions.onProjectSelected,
                onLogOut = actions.onLogOut
            )
        }
        composable(MainDestinations.MapRoute) {
            Map(
                mapViewModel = mapViewModel,
                onSetStation = actions.onSetStation,
                onLogOut = actions.onLogOut
            )
        }
        composable(MainDestinations.StationsRoute) {
            Stations(
                stationsViewModel = stationsViewModel,
                onLogOut = actions.onLogOut,
                upPress = actions.upPress
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
    val onProjectSelected: () -> Unit = {
        navController.navigate(MainDestinations.MapRoute)
    }
    val onSetStation: () -> Unit = {
        navController.navigate(MainDestinations.StationsRoute)
    }
    val onLogOut: () -> Unit = {
        navController.navigate(MainDestinations.LoginRoute) {
            launchSingleTop = true
            popUpTo(0) { inclusive = true }
        }
    }
    val upPress: () -> Unit = {
        navController.navigateUp()
    }
}
