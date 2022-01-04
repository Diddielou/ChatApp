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
import fhnw.emoba.thatsapp.data.ChatUser
import fhnw.emoba.thatsapp.model.Screen
import fhnw.emoba.thatsapp.model.ThatsAppModel
import fhnw.emoba.thatsapp.ui.UserInput
import kotlinx.coroutines.launch


@ExperimentalComposeUiApi
@Composable
fun ChatScreen(model: ThatsAppModel) {
    val scaffoldState = rememberScaffoldState()
    val title = Screen.CHAT.title + (model.currentChatPartner!!.nickname)

    Scaffold(scaffoldState = scaffoldState,
        topBar = { ChatTopBar(model, title, Screen.MAIN, scaffoldState) },
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
            navigationIcon = {
                IconButton(onClick = { currentScreen = screen }) {
                    Icon(Icons.Filled.ArrowBack, "Back")
                }
            }
        )
    }
}


/* CONTENT */
@ExperimentalComposeUiApi
@Composable
private fun Body(model: ThatsAppModel) {
    with(model) {
        ConstraintLayout(modifier = Modifier.fillMaxSize()) {
            val (allThatsPanel, messageField) = createRefs()
            AllMessagesList(model, Modifier.constrainAs(allThatsPanel) {
                width = Dimension.fillToConstraints
                height = Dimension.fillToConstraints
                top.linkTo(parent.top, 0.dp)
                start.linkTo(parent.start, 10.dp)
                end.linkTo(parent.end, 10.dp)
            })

            MessageArea(model, Modifier.constrainAs(messageField) {
                width = Dimension.fillToConstraints
                start.linkTo(parent.start, 10.dp)
                bottom.linkTo(parent.bottom, 10.dp)
                end.linkTo(parent.end, 10.dp)
            })
        }
    }
}

@Composable
private fun AllMessagesList(model: ThatsAppModel, modifier: Modifier) {
    var messagesForThisChat =
        model.currentChatPartner?.let { model.filterMessagesPerConversation(it) }

    Box(
        modifier.border(
            width = 1.dp,
            brush = SolidColor(gray300),
            shape = RectangleShape
        )
    ) {
        if (messagesForThisChat!!.isEmpty()) {
            Text(
                text = "No messages yet.",
                style = MaterialTheme.typography.h4,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            val scrollState = rememberLazyListState()
            LazyColumn(state = scrollState) {
                items(messagesForThisChat) {
                    model.currentChatPartner?.let { user ->
                        MessageRowForeign(
                            it,
                            user
                        )
                    }

                }
            }

            val scope = rememberCoroutineScope()
            SideEffect {
                scope.launch { scrollState.animateScrollToItem(messagesForThisChat.size - 1) }
            }
        }
    }
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun MessageRowForeign(chatMessage: ChatMessage, user: ChatUser) {
    with(chatMessage) {
        // TODO: Attributes needed: Name, last message, lastTime
        ListItem(text = { Text(chatMessage.receiverID) },
            overlineText = { Text(senderID) }
        )
        Divider()
    }
}

// TODO cannot receive or deliver messages > ChatScreen

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun MessageRowSelf(model: ThatsAppModel, chatMessage: ChatMessage) {

    with(chatMessage) {
        // TODO: Attributes needed: Name, last message, lastTime
        ListItem(text = { Text(chatMessage.senderID) },
            overlineText = { Text(model.profileName) }
        )
        Divider()
    }
}

@ExperimentalComposeUiApi
@Composable
private fun MessageArea(model: ThatsAppModel, modifier: Modifier) {
    UserInput(model = model, onMessageSent = { model.message }, modifier = modifier)
}


