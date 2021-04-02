package com.tibi.tiptopo.presentation.stations

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tibi.tiptopo.R
import com.tibi.tiptopo.data.Resource
import com.tibi.tiptopo.domain.Station
import com.tibi.tiptopo.presentation.login.FirebaseUserLiveData
import com.tibi.tiptopo.presentation.toast

@Composable
fun Stations(
    stationsViewModel: StationsViewModel,
    onLogOut: () -> Unit,
    upPress: () -> Unit
) {
    val authState: FirebaseUserLiveData.AuthenticationState by stationsViewModel.authenticationState
        .observeAsState(FirebaseUserLiveData.AuthenticationState.AUTHENTICATED)

    when (authState) {
        FirebaseUserLiveData.AuthenticationState.AUTHENTICATED -> {
            StationsScreen(stationsViewModel)
        }
        FirebaseUserLiveData.AuthenticationState.UNAUTHENTICATED -> onLogOut()
    }

    when (stationsViewModel.addedStation) {
        is Resource.Failure -> { LocalContext.current.toast(stringResource(R.string.error)) }
        is Resource.Loading -> { StationsScreen(stationsViewModel) }
        is Resource.Success -> {
            stationsViewModel.onStationAdded()
            upPress()
        }
    }
}

@Composable
fun StationsScreen(stationsViewModel: StationsViewModel) {
    val stationsState = stationsViewModel.stations.observeAsState(Resource.Loading())
    when (stationsState.value) {
        is Resource.Loading -> {
            Button(
                onClick = {
                    stationsViewModel.addStation(Station(name = "S1"))
                },
                Modifier.padding(8.dp)
            ) {
                Text(text = "Add quick station")
            }
        }
    }
}
