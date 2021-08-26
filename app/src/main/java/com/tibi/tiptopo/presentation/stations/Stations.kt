package com.tibi.tiptopo.presentation.stations

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tibi.tiptopo.R
import com.tibi.tiptopo.data.Resource
import com.tibi.tiptopo.domain.Measurement
import com.tibi.tiptopo.domain.PointType
import com.tibi.tiptopo.domain.Project
import com.tibi.tiptopo.domain.Station
import com.tibi.tiptopo.presentation.login.FirebaseUserLiveData
import com.tibi.tiptopo.presentation.projects.ProjectRow
import com.tibi.tiptopo.presentation.toast
import com.tibi.tiptopo.presentation.ui.ProgressCircular
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun Stations(
    stationsViewModel: StationsViewModel,
    onLogOut: () -> Unit,
    upPress: () -> Unit
) {
    stationsViewModel.setBluetoothDataListener()

    val authState: FirebaseUserLiveData.AuthenticationState by stationsViewModel.authenticationState
        .observeAsState(FirebaseUserLiveData.AuthenticationState.AUTHENTICATED)

    when (authState) {
        FirebaseUserLiveData.AuthenticationState.AUTHENTICATED -> {
            when (stationsViewModel.addedStation) {
                is Resource.Failure -> { LocalContext.current.toast(stringResource(R.string.error)) }
                is Resource.Loading -> { StationsScreen(stationsViewModel) }
                is Resource.Success -> {
                    stationsViewModel.onStationAdded()
                    upPress()
                }
            }
        }
        FirebaseUserLiveData.AuthenticationState.UNAUTHENTICATED -> onLogOut()
    }
}

@Composable
fun StationsScreen(
    stationsViewModel: StationsViewModel
) {
    val waitMeasurement = stationsViewModel.waitMeasurement
    val bluetoothMessage = stationsViewModel.bluetoothMessage
    val selectedStation = stationsViewModel.selectedStation

    if (bluetoothMessage.isNotBlank()) {
        stationsViewModel.onStopWaitMeasurement()
        stationsViewModel.addSelectedStation()
    }


    when (stationsViewModel.stations.observeAsState(Resource.Loading()).value) {
        is Resource.Loading -> {
            Button(
                onClick = {
                    stationsViewModel.addStation(Station(name = "S0"))
                },
                Modifier.padding(8.dp)
            ) {
                Text(text = stringResource(R.string.add_quick_station))
            }
        }
        is Resource.Failure -> {
            Text(text = stringResource(R.string.error))
        }
        is Resource.Success -> {
            val measurements = stationsViewModel.measurements
                .observeAsState(Resource.Loading()).value
            when (measurements) {
                is Resource.Loading -> {}
                is Resource.Failure -> {}
                is Resource.Success -> {
                    if (waitMeasurement) {
                        Column {
                            Text(text = stringResource(R.string.waiting_for_measurement))
                            ProgressCircular()
                        }
                    } else {
                        if (selectedStation == null) {
                            SelectStation(measurements.data, stationsViewModel)
                        } else {
                            SelectBacksight(measurements.data, stationsViewModel)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SelectStation(measurements: List<Measurement>, stationsViewModel: StationsViewModel) {
    Column {
        Text(text = stringResource(R.string.station))
        LazyColumn {
            items(measurements.filter { it.type == PointType.Station }
                .sortedBy { it.number }) { station ->
                StationRow(
                    station = station,
                    {
                        stationsViewModel.onSetSelectedStation(it)
                    },
                    Modifier.fillParentMaxWidth()
                )
            }
        }
    }
}

@Composable
fun SelectBacksight(measurements: List<Measurement>, stationsViewModel: StationsViewModel) {
    Column {
        Text(text = stringResource(R.string.backsight))
        LazyColumn {
            items(measurements.filter { it.type == PointType.Station }
                .sortedBy { it.number }) { station ->
                StationRow(
                    station = station,
                    {
                        stationsViewModel.onSetSelectedBacksight(it)
                        stationsViewModel.onWaitMeasurement()
                    },
                    Modifier.fillParentMaxWidth()
                )
            }
        }
    }
}

@Composable
fun StationRow(
    station: Measurement,
    onItemClicked: (Measurement) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier
            .clickable { onItemClicked(station) }
            .padding(start = 8.dp)) {
        Text(text = station.number.toString(), fontWeight = FontWeight.Bold)
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
            Text(stringResource(R.string.station), style = MaterialTheme.typography.body2)
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = station.date!!.time
            val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
            val date = formatter.format(calendar.time)
            Text(date, style = MaterialTheme.typography.body2)
        }
    }
}
