package com.tibi.tiptopo.presentation.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tibi.tiptopo.R
import com.tibi.tiptopo.domain.TotalStation

@Composable
fun ProgressCircular() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
    }
}

@ExperimentalFoundationApi
@ExperimentalMaterialApi
@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@Composable
fun ItemEntryInput(
    label: String,
    initText: String = "",
    showTS: Boolean,
    selectedChip: TotalStation = TotalStation.Nikon,
    onChipSelected: (TotalStation) -> Unit = {},
    onItemComplete: (String) -> Unit
) {
    val (text, setText) = remember { mutableStateOf(initText) }
    val submit = {
        if (text.isNotBlank()) {
            onItemComplete(text)
        }
        setText("")
    }
    Column {
        Surface(color = MaterialTheme.colors.secondary) {
            ItemInput(
                text = text,
                onTextChange = setText,
                label = label,
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
        AnimatedVisibility(visible = showTS && text.isNotBlank()) {
            TotalStationChips(
                selectedChip = selectedChip,
                onChipSelected = onChipSelected,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@ExperimentalComposeUiApi
@Composable
fun ItemInput(
    text: String,
    onTextChange: (String) -> Unit,
    label: String,
    submit: () -> Unit,
    buttonSlot: @Composable () -> Unit
) {
    Column {
        Row(
            Modifier
                .padding(horizontal = 8.dp)
                .padding(top = 8.dp)
        ) {
            ItemInputText(
                text,
                onTextChange,
                Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                label,
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
fun ItemInputText(
    text: String,
    onTextChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String,
    onImeAction: () -> Unit = {}
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    OutlinedTextField(
        label = { Text(text = label) },
        value = text,
        onValueChange = onTextChange,
        colors = TextFieldDefaults.textFieldColors(backgroundColor = Color.Transparent),
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = {
            onImeAction()
            keyboardController?.hide()
        }),
        modifier = modifier
    )
}

@ExperimentalMaterialApi
@Composable
private fun TextChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = when {
            selected -> MaterialTheme.colors.primary
            else -> Color.Transparent
        },
        contentColor = when {
            selected -> MaterialTheme.colors.onPrimary
            else -> MaterialTheme.colors.onSecondary
        },
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = 1.dp,
            color = when {
                selected -> MaterialTheme.colors.primary
                else -> MaterialTheme.colors.onSecondary
            }
        ),
        onClick = onClick,
        modifier = modifier
    ) {
        Row(modifier = Modifier) {
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(top = 8.dp, start = 8.dp)
                        .size(20.dp)
                        .clip(CircleShape)
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.button,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@ExperimentalMaterialApi
@ExperimentalFoundationApi
@Composable
private fun TotalStationChips(
    selectedChip: TotalStation,
    onChipSelected: (TotalStation) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalStations = TotalStation.values()
    Surface(color = MaterialTheme.colors.secondary) {
        Row(
            modifier = modifier.padding(bottom = 16.dp)
        ) {
            totalStations.forEach { totalStation ->
                TextChip(
                    text = totalStation.name,
                    selected = totalStation == selectedChip,
                    onClick = { onChipSelected(totalStation) },
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 16.dp)
                )
            }
        }
    }
}

