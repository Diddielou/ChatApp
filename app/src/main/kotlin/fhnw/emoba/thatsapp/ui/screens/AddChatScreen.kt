package fhnw.emoba.thatsapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import fhnw.emoba.thatsapp.data.ChatUser
import fhnw.emoba.thatsapp.model.Screen
import fhnw.emoba.thatsapp.model.ThatsAppModel
import kotlinx.coroutines.launch

/*
Displays all users available
 */
@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@Composable
fun AddChatScreen(model: ThatsAppModel) {
    val scaffoldState = rememberScaffoldState()
    Scaffold(scaffoldState = scaffoldState,
        topBar = { GeneralTopBar(model, Screen.ADDCHAT.title, Screen.MAIN, scaffoldState) },
        snackbarHost = { NotificationHost(it) },
        content = { Body(model) }
    )
    Notification(model, scaffoldState)
}


/* CONTENT */
@ExperimentalMaterialApi
@Composable
private fun Body(model: ThatsAppModel) {
    with(model) {
        UserList(model = model)
    }
}

@ExperimentalMaterialApi
@Composable
private fun UserList(model: ThatsAppModel) {
    val state = rememberLazyListState()
    with(model) {
        if (isLoading) {
            LoadingIndicator()
        } else if(model.allUsers.isEmpty()) {
            OnScreenMessage("No users found.")
        } else {
            LazyColumn(state = state) {
                items(allUsers) { UserRow(user = it, model = model) }
            }
            val scope = rememberCoroutineScope()
            SideEffect {
                scope.launch { state.animateScrollToItem(allUsers.size - 1) }
            }
        }
    }
}

@ExperimentalMaterialApi
@Composable
private fun UserRow(user: ChatUser, model: ThatsAppModel) {
    with(user) {
        ListItem(
            modifier = Modifier.clickable(onClick = {
                //model.filterMessagesPerConversation(user)
                model.currentChatPartner = user
                model.currentScreen = Screen.CHAT
            }),
            text = { Text(nickname) },
            secondaryText = { Text("last online: " + model.getLocalDateTimeFromUTCtimestamp(user.lastOnline)) },
            trailing = { ProfileImage(user, 50) }
        )
        Divider()
    }
}

/*
@Composable
fun SmallProfileImage(model: ThatsAppModel, user: ChatUser) {
    if(user.userProfileImage != null){
        Image(
            bitmap = user.userProfileImage!!.asImageBitmap(),
            contentDescription = "Profile image",
            modifier = Modifier.size(50.dp)
        )
    }
    else {
        Icon(Icons.Filled.AccountCircle, "No profile picture")
    }
}

 */