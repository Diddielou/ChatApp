package fhnw.emoba.thatsapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import fhnw.emoba.freezerapp.ui.theme.gray300
import fhnw.emoba.thatsapp.data.ChatMessage
import fhnw.emoba.thatsapp.model.*
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
            val (allMessagesPanel, messageField) = createRefs()
            AllMessagesList(model, Modifier.constrainAs(allMessagesPanel) {
                width = Dimension.fillToConstraints
                height = Dimension.fillToConstraints
                top.linkTo(parent.top, 5.dp)
                start.linkTo(parent.start, 10.dp)
                end.linkTo(parent.end, 10.dp)
                bottom.linkTo(messageField.top)
            })

            MessageArea(model, Modifier.constrainAs(messageField) {
                width = Dimension.fillToConstraints
                top.linkTo(allMessagesPanel.bottom)
                start.linkTo(parent.start, 10.dp)
                end.linkTo(parent.end, 10.dp)
                bottom.linkTo(parent.bottom, 10.dp)
            })
        }
    }
}

@Composable
private fun AllMessagesList(model: ThatsAppModel, modifier: Modifier) {
    // TODO cannot receive or deliver messages > ChatScreen
    val messagesInThisChat =
        model.currentChatPartner?.let { model.filterMessagesPerConversation(it) }

    Box(
        modifier.border(
            width = 1.dp,
            brush = SolidColor(gray300),
            shape = RectangleShape
        )
    ) {
        if (messagesInThisChat!!.isEmpty()) {
            OnScreenMessage("No messages yet.")
        } else {
            val scrollState = rememberLazyListState()
            LazyColumn(state = scrollState) {
                items(messagesInThisChat) {
                   MessageRow(it, model)
                }
            }
            val scope = rememberCoroutineScope()
            SideEffect {
                scope.launch { scrollState.animateScrollToItem(messagesInThisChat.size - 1) }
            }
        }
    }
}

// TODO complete for all options
@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun MessageRow(chatMessage: ChatMessage, model: ThatsAppModel) {
    val modifierMyMessage = Modifier.background(
        color = MaterialTheme.colors.secondary.copy(alpha = 0.15f),
        shape = RoundedCornerShape(15.dp)
    ).padding(0.dp, 5.dp, 0.dp, 5.dp).border(5.dp, Color.White)
    val modifierForeignMessage = Modifier.background(
        color = MaterialTheme.colors.primary.copy(alpha = 0.15f),
        shape = RoundedCornerShape(15.dp)
    ).padding(0.dp, 5.dp, 0.dp, 5.dp)

    with(chatMessage) {
        when (messageType){
            ChatPayloadContents.TEXT.name -> {
                val chatText = ChatText(chatMessage.payload)

                if(chatMessage.senderID == model.profileId){ // message from profile
                    ListItem(text = { Text(chatText.body, fontWeight = FontWeight.Normal) }, modifier = modifierMyMessage) // textAlign = TextAlign.End,
                }
                else { // message from currentChatPartner
                    ListItem(text = { Text(chatText.body) }, modifier = modifierForeignMessage)
                }
            }
            ChatPayloadContents.LOCATION.name -> {  }
            ChatPayloadContents.IMAGE.name -> {  } // show bitmap
            ChatPayloadContents.INFO.name -> {  }
            ChatPayloadContents.LIVE.name -> { }
        }

    }
}


@ExperimentalComposeUiApi
@Composable
private fun MessageArea(model: ThatsAppModel, modifier: Modifier) {
    UserInput(model = model,  modifier = modifier) // onMessageSent = { model.messageToSend },
}


