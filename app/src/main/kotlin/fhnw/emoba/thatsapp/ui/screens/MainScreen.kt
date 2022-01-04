package fhnw.emoba.thatsapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fhnw.emoba.thatsapp.model.Screen
import fhnw.emoba.thatsapp.model.ThatsAppModel
import kotlinx.coroutines.launch

/*
Displays all opened chats
 */
@ExperimentalComposeUiApi
@Composable
fun MainScreen(model: ThatsAppModel) {
    val scaffoldState = rememberScaffoldState()
    Scaffold(scaffoldState = scaffoldState,
        topBar = { TopBar(model, scaffoldState) },
        drawerContent = { Drawer(model) },
        snackbarHost = { NotificationHost(it) },
        content = { Body(model) },
        floatingActionButton = { AddUserButton(model) }
    )
    Notification(model, scaffoldState)
}

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
@Composable
private fun Body(model: ThatsAppModel) {
    Text("Here comes the list with opened Chats.")
    // get list with chatMessages, filtered by userID (mine or one other)
    // filter List with chatMessages down to one userId

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
