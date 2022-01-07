package fhnw.emoba.thatsapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import fhnw.emoba.thatsapp.model.Screen
import fhnw.emoba.thatsapp.model.ThatsAppModel

/* To go back */
@Composable
fun GeneralTopBar(model: ThatsAppModel, title: String, screen: Screen, scaffoldState: ScaffoldState) {
    val scope = rememberCoroutineScope()
    with(model) {
        TopAppBar(
            title = { Text(text = title ) },
            navigationIcon = { IconButton(onClick = { currentScreen = screen }) {
                Icon(Icons.Filled.ArrowBack, "Back")
                }
            }
        )
    }
}

/* MESSAGES */
@Composable
public fun NotificationHost(state: SnackbarHostState) {
    SnackbarHost(state) { data ->
        Box(
            modifier = Modifier.fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Snackbar(snackbarData = data)
        }
    }
}

@Composable
public fun Notification(model: ThatsAppModel, scaffoldState: ScaffoldState) {
    with(model) {
        if (notificationMessage.isNotBlank()) {
            LaunchedEffect(scaffoldState.snackbarHostState) {
                scaffoldState.snackbarHostState.showSnackbar(
                    message = notificationMessage,
                    actionLabel = "OK"
                )
                notificationMessage = ""
            }
        }
    }
}

@Composable
fun LoadingIndicator(isLoading: Boolean = true) {
    CircularProgressIndicator(modifier = Modifier.size(30.dp))
}

@Composable
fun OnScreenMessage(message: String){
    Column(
        modifier = Modifier.fillMaxWidth().fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = message,
            style = MaterialTheme.typography.subtitle1,
            color = MaterialTheme.colors.secondary)
    }
}

@Composable
fun Heading1(text: String){
    Text(text = text,
        modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 10.dp),
        style = MaterialTheme.typography.h4,
        overflow = TextOverflow.Ellipsis,
        maxLines = 2,
        color = MaterialTheme.colors.secondaryVariant)
}

@Composable
fun Heading2(text: String){
    Text(text = text,
        modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 5.dp),
        style = MaterialTheme.typography.h5,
        overflow = TextOverflow.Ellipsis,
        maxLines = 1)
}

@Composable
fun Heading3(text: String){
    Text(text = text,
        modifier = Modifier.padding(0.dp),
        style = MaterialTheme.typography.h6,
        overflow = TextOverflow.Ellipsis,
        maxLines = 1)
}

