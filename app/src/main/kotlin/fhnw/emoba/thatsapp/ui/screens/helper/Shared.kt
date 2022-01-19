package fhnw.emoba.thatsapp.ui.screens.helper

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import fhnw.emoba.thatsapp.data.ChatUser
import fhnw.emoba.thatsapp.model.Screen
import fhnw.emoba.thatsapp.model.ThatsAppModel
import fhnw.emoba.thatsapp.ui.theme.typography

@Composable
fun GeneralTopBar(model: ThatsAppModel, title: String, backScreen: Screen) {
    with(model) {
        TopAppBar(
            title = { Text(text = title ) },
            navigationIcon = { IconButton(onClick = { currentScreen = backScreen }) {
                Icon(Icons.Filled.ArrowBack, "Back")
                }
            }
        )
    }
}

/* USERS */
@Composable
fun UserImage(user: ChatUser, size: Int) {
    Surface(
        modifier = Modifier.defaultMinSize(size.dp),
        shape = CircleShape,
        border = BorderStroke(1.5.dp, MaterialTheme.colors.primary)
    ) {
        if(user.userProfileImage != null){
            Image(
                bitmap = user.userProfileImage!!.asImageBitmap(),
                contentDescription = "User image",
                modifier = Modifier.size(size.dp)
            )
        }
        else {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "No User image",
                modifier = Modifier.size(size.dp),
                tint = MaterialTheme.colors.secondary.copy(alpha = 0.65f)
            )
        }
    }

}

@Composable
fun LastOnlineOrTyping(model: ThatsAppModel, user: ChatUser){
    if(user.isLive){
        Text("typing...", style = typography.body2)
    } else {
        Text("last online: " + model.getLocalDateTimeFromUTCtimestamp(user.lastOnline),
            style = typography.body2)
    }
}

/* NOTIFICATIONS */
@Composable
fun NotificationHost(state: SnackbarHostState) {
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
fun Notification(model: ThatsAppModel, scaffoldState: ScaffoldState) {
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

/* APP BEHAVIOUR */
@Composable
fun LoadingIndicator() {
    CircularProgressIndicator(modifier = Modifier.size(30.dp))
}

@Composable
fun OnScreenMessage(message: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.subtitle1,
            color = MaterialTheme.colors.secondary
        )
    }
}


/* STYLES */
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


