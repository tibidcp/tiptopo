package com.tibi.tiptopo.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.tibi.tiptopo.R

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

@ExperimentalComposeUiApi
@Composable
fun ItemEntryInput(label: String, initText: String = "", onItemComplete: (String) -> Unit) {
    val (text, setText) = remember { mutableStateOf(initText) }
    val submit = {
        if (text.isNotBlank()) {
            onItemComplete(text)
        }
        setText("")
    }
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

