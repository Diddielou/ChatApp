package fhnw.emoba.thatsapp.ui.screens

import androidx.compose.foundation.layout.Row
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import fhnw.emoba.thatsapp.model.Screen
import fhnw.emoba.thatsapp.model.ThatsAppModel

@Composable
fun ProfileScreen(model: ThatsAppModel) {
    val scaffoldState = rememberScaffoldState()
    Scaffold(scaffoldState = scaffoldState,
        topBar = { GeneralTopBar(model, Screen.PROFILE.title, Screen.MAIN, scaffoldState) },
        snackbarHost = { NotificationHost(it) },
        content = { Body(model) }
    )
    Notification(model, scaffoldState)
}

@Composable
private fun Body(model: ThatsAppModel) {
    with(model) {

    }
}

@Composable
private fun ProfilePicture(model: ThatsAppModel) {
    with(model) {

    }
}

@Composable
private fun ProfileInformation(model: ThatsAppModel) {
    with(model) {
        Row(){
            Icon(Icons.Filled.Person, contentDescription = "Enter your name")
        }

        // Icon
        // Textfield Name
        //OutlinedTextField(value = , onValueChange = )
        /*
        TextField(
            value = userName, // User name
            onValueChange = { userName = it },
            placeholder = { Text("Enter your name") },
            trailingIcon = {
                IconButton(onClick = {
                    keyboard?.hide()
                    searchFilter = ""
                    when (selectedTab) {
                        Tab.TRACKS -> searchTrack(searchFilter)
                        Tab.ALBUMS -> searchAlbum(searchFilter)
                        Tab.RADIO -> loadRadios()
                        Tab.FAVORITES -> getFavoritesList()
                    }
                })
                { Icon(Icons.Filled.Clear, "Delete") }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done,
                autoCorrect  = false,
                keyboardType = KeyboardType.Ascii),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboard?.hide()
                    when (selectedTab) {
                        Tab.TRACKS -> searchTrack(searchFilter)
                        Tab.ALBUMS -> searchAlbum(searchFilter)
                        Tab.RADIO -> loadRadios()
                        Tab.FAVORITES -> getFavoritesList()
                    }
            }),
            modifier = Modifier
                .fillMaxWidth()
                .onKeyEvent {
                    if (it.nativeKeyEvent.keyCode == Key.Enter.nativeKeyCode) {
                        keyboard?.hide()
                        when (selectedTab) {
                            Tab.TRACKS -> searchTrack(searchFilter)
                            Tab.ALBUMS -> searchAlbum(searchFilter)
                            Tab.RADIO -> loadRadios()
                            Tab.FAVORITES -> getFavoritesList()
                        }
                    }
                    return@onKeyEvent true
                }
            )
    }
         */

    }
}


