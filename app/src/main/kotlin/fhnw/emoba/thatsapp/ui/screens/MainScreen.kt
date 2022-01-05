package fhnw.emoba.thatsapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import fhnw.emoba.thatsapp.data.ChatUser
import fhnw.emoba.thatsapp.model.Screen
import fhnw.emoba.thatsapp.model.ThatsAppModel
import kotlinx.coroutines.launch

@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@Composable
fun MainScreen(model: ThatsAppModel) {
    val scaffoldState = rememberScaffoldState()
    Scaffold(scaffoldState = scaffoldState,
        topBar = { TopBar(model, scaffoldState) },
        drawerContent = { Drawer(model) },
        snackbarHost = { NotificationHost(it) },
        content = { Body(model) }
        //, floatingActionButton = { AddUserButton(model) }
    )
    Notification(model, scaffoldState)
}

/*
@Composable
fun AddUserButton(model: ThatsAppModel) {
    with(model) {
        FloatingActionButton(
            content = { Icon(Icons.Filled.PersonAdd, "Add chat") },
            onClick = {
                currentScreen = Screen.ADDCHAT
                loadUserListImages()
            })
    }
}
 */

@Composable
private fun TopBar(model: ThatsAppModel, scaffoldState: ScaffoldState) {
    with(model) {
        Column {
            TopAppBar(
                title = { Text(text = Screen.MAIN.title) },
                navigationIcon = { DrawerIcon(scaffoldState = scaffoldState) },
            )
        }
    }
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
            trailing = { SmallProfileImage(model = model, user = user) }
        )
        Divider()
    }
}


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



/* Drawer */
@Composable
private fun DrawerIcon(scaffoldState: ScaffoldState) {
    val scope = rememberCoroutineScope()

    IconButton(onClick = { scope.launch { scaffoldState.drawerState.open() } }) {
        Icon(Icons.Filled.Menu, "Open drawer")
    }
}

@Composable
private fun Drawer(model: ThatsAppModel) {
    with(model) {
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 25.dp, bottom = 25.dp)
        )
        {
            Text(
                model.drawerTitle,
                style = MaterialTheme.typography.h4,
                color = MaterialTheme.colors.secondary
            )
        }
        Divider()
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .clickable(onClick = { toggleTheme() })
        ) {
            Switch(checked = darkTheme,
                onCheckedChange = { toggleTheme() })
            Text(text = toggleThemeText())
        }
        Divider()
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
            .clickable(onClick = { currentScreen = Screen.PROFILE })
        ){
            Text("Edit Profile")
        }
        Divider()
    }
}
