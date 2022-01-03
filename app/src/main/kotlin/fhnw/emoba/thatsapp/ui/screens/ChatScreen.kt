package fhnw.emoba.thatsapp.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import fhnw.emoba.freezerapp.ui.theme.gray300
import fhnw.emoba.thatsapp.data.ChatMessage
import fhnw.emoba.thatsapp.model.Screen
import fhnw.emoba.thatsapp.model.ThatsAppModel
import fhnw.emoba.thatsapp.ui.UserInput
import kotlinx.coroutines.launch


@ExperimentalComposeUiApi
@Composable
fun ChatScreen(model: ThatsAppModel) {
    val scaffoldState = rememberScaffoldState()
    Scaffold(scaffoldState = scaffoldState,
        topBar = { ChatTopBar(model, Screen.CHAT.title, Screen.MAIN, scaffoldState) }, // TODO getUserName from Model
        snackbarHost = { NotificationHost(it) },
        content = { Body(model) }
    )
    Notification(model, scaffoldState)
}

@Composable
fun ChatTopBar(model: ThatsAppModel, title: String, screen: Screen, scaffoldState: ScaffoldState) {
    val scope = rememberCoroutineScope()
    with(model) {
        TopAppBar(
            title = { Heading3(text = title) },
            navigationIcon = { IconButton(onClick = { currentScreen = screen }) {
                Icon(Icons.Filled.ArrowBack, "Back")
            } }
        )
    }
}


/* CONTENT */
@ExperimentalComposeUiApi
@Composable
private fun Body(model: ThatsAppModel) {
    with(model) {
        ConstraintLayout(modifier = Modifier.fillMaxSize()) {
            val (brokerInfo, topicInfo, allThatsPanel, messageField) = createRefs()
            // not needed
            /*
            Info(mqttBroker, Modifier.constrainAs(brokerInfo) {
                top.linkTo(parent.top, 10.dp)
                start.linkTo(parent.start, 10.dp)
            })
            Info(mainTopic, Modifier.constrainAs(topicInfo) {
                top.linkTo(brokerInfo.bottom, 10.dp)
                start.linkTo(parent.start, 10.dp)
            })
            */

            AllMessagesList(allMessages, Modifier.constrainAs(allThatsPanel) {
                width  = Dimension.fillToConstraints
                height = Dimension.fillToConstraints
                top.linkTo(parent.top, 0.dp)
                start.linkTo(parent.start, 10.dp)
                //bottom.linkTo(messageField.top, 0.dp)
                end.linkTo(parent.end, 10.dp)
            })

            MessageArea(model, Modifier.constrainAs(messageField) {
                width = Dimension.fillToConstraints
                start.linkTo(parent.start, 10.dp)
                bottom.linkTo(parent.bottom, 10.dp)
                end.linkTo(parent.end, 10.dp)
                /*
                top.linkTo(allFlapsPanel.bottom, 15.dp)
                start.linkTo(parent.start, 10.dp)
                bottom.linkTo(publishButton.top, 15.dp)
                end.linkTo(parent.end, 10.dp)
                 */
            })
            /*
            PublishButton(model, Modifier.constrainAs(publishButton) {
                width = Dimension.fillToConstraints
                start.linkTo(parent.start, 10.dp)
                end.linkTo(parent.end, 10.dp)
                bottom.linkTo(parent.bottom, 15.dp)
            })
             */
        }
    }
}


// not needed
@Composable
private fun Info(text: String, modifier: Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.h6,
        modifier = modifier
    )
}


@Composable
private fun AllMessagesList(chatMessages: List<ChatMessage>, modifier: Modifier) {
    Box(
        modifier.border(
            width = 1.dp,
            brush = SolidColor(gray300),
            shape = RectangleShape
        )
    ) {
        if (chatMessages.isEmpty()) {
            Text(
                text = "No messages yet.",
                style = MaterialTheme.typography.h4,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            val scrollState = rememberLazyListState()
            LazyColumn(state = scrollState) {
                items(chatMessages) { MessageRow(it) }
            }

            val scope = rememberCoroutineScope()
            SideEffect {
                scope.launch { scrollState.animateScrollToItem(chatMessages.size - 1) }
            }
        }
    }
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun MessageRow(chatMessage: ChatMessage) {
    with(chatMessage) {
        // TODO: Attributes needed: Name, last message, lastTime
        ListItem(text = { Text(messageID) },
            overlineText = { Text(senderID) }
        )
        Divider()
    }
}

@ExperimentalComposeUiApi
@Composable
private fun MessageArea(model: ThatsAppModel, modifier: Modifier) {
    UserInput(model = model, onMessageSent = { model.message }, modifier = modifier)
}


