package fhnw.emoba.thatsapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Place
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import fhnw.emoba.thatsapp.data.ChatMessage
import fhnw.emoba.thatsapp.data.GeoPosition
import fhnw.emoba.thatsapp.model.*
import fhnw.emoba.thatsapp.ui.UserInput
import kotlinx.coroutines.launch

@ExperimentalComposeUiApi
@Composable
fun ChatScreen(model: ThatsAppModel) {
    val scaffoldState = rememberScaffoldState()
    val title = Screen.CHAT.title + (model.currentChatPartner!!.nickname)

    Scaffold(scaffoldState = scaffoldState,
        topBar = { ChatTopBar(model, title, Screen.MAIN) },
        snackbarHost = { NotificationHost(it) },
        content = { Body(model) }
    )
    Notification(model, scaffoldState)
}

@Composable
fun ChatTopBar(model: ThatsAppModel, title: String, screen: Screen) {
    with(model) {
        TopAppBar(
            title = {
                Row(Modifier.width(305.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Column() {
                        Heading3(title)
                        LastOnlineOrTyping(model, currentChatPartner!!)
                    }
                    ProfileImage(currentChatPartner!!, 45)
                }
            },
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
                top.linkTo(parent.top, 10.dp)
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
    val messagesInThisChat =
        model.currentChatPartner?.let { model.filterMessagesPerConversation(it) }

    Box(
        modifier.border(
            width = 1.dp,
            brush = SolidColor(Color.Transparent),
            shape = RectangleShape
        )
    ) {
        if (messagesInThisChat!!.isEmpty()) {
            OnScreenMessage("No messages yet.")
        } else {
            val scrollState = rememberLazyListState()
            LazyColumn(state = scrollState, verticalArrangement = Arrangement.spacedBy(10.dp)) {
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

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun MessageRow(chatMessage: ChatMessage, model: ThatsAppModel) {

    val modifierMyMessage = Modifier
        .background(
            color = MaterialTheme.colors.secondary.copy(alpha = 0.15f),
            shape = RoundedCornerShape(10.dp)
        )
        .padding(10.dp, 5.dp, 5.dp, 10.dp)

    val modifierForeignMessage = Modifier
        .background(
            color = MaterialTheme.colors.primary.copy(alpha = 0.15f),
            shape = RoundedCornerShape(10.dp)
        )
        .padding(10.dp, 5.dp, 5.dp, 10.dp)

    with(chatMessage) {
        when (messageType){
            ChatPayloadContents.TEXT.name -> {
                val chatText = ChatText(payload)
                MessageContent(chatMessage = chatMessage, model = model,
                    modifierSelf = modifierMyMessage, modifierForeign = modifierForeignMessage,
                    rowContent = { Text(text = chatText.body, fontWeight = FontWeight.Normal) }
                )
            }
            ChatPayloadContents.IMAGE.name -> {
                MessageContent(chatMessage = chatMessage, model = model,
                    modifierSelf = modifierMyMessage, modifierForeign = modifierForeignMessage,
                    rowContent = { Image(
                        bitmap = chatMessage.bitmap!!.asImageBitmap(),
                        contentDescription = "Sent Image",
                        modifier = Modifier.height(200.dp)) }
                )
            }
            ChatPayloadContents.LOCATION.name -> {
                MessageContent(chatMessage = chatMessage, model = model,
                    modifierSelf = modifierMyMessage, modifierForeign = modifierForeignMessage,
                    rowContent = { LocationContent(position = chatMessage.position!!) }
                )
            }
        }
    }
}

@Composable
fun MessageContent(chatMessage: ChatMessage, model: ThatsAppModel, modifierSelf: Modifier,
                   modifierForeign: Modifier, rowContent: @Composable() () -> Unit) {

    if(model.messageSent && chatMessage.senderID == model.profileId) { // message from profile
        Column(
            modifier = Modifier
                .padding(start = 25.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Top
        ) {
            Row(modifierSelf,
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.Start) {
                rowContent()
            }
        }
    }
    else { // message from currentChatPartner
            Column(
                modifier = Modifier
                    .padding(end = 25.dp)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Top
            ) {
                Row(
                    modifierForeign,
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.Start
                ) {
                    rowContent()
                }
            }
    }
}

@Composable
fun LocationContent(position: GeoPosition) {
    val uriHandler = LocalUriHandler.current
    Row(Modifier
        .clickable  { uriHandler.openUri(position.asGoogleMapsURL()) },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ){
        Icon(imageVector = Icons.Filled.Place, contentDescription = "Location", tint = MaterialTheme.colors.secondary)
        Text(text = position.dms(), color = MaterialTheme.colors.secondary)
    }
}

@ExperimentalComposeUiApi
@Composable
private fun MessageArea(model: ThatsAppModel, modifier: Modifier) {
    UserInput(model = model,  modifier = modifier)
}


