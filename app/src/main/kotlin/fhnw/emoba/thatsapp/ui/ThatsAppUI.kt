package fhnw.emoba.thatsapp.ui


import androidx.compose.animation.Crossfade
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import fhnw.emoba.thatsapp.ui.theme.ThatsAppTheme
import fhnw.emoba.thatsapp.model.Screen
import fhnw.emoba.thatsapp.model.ThatsAppModel
import fhnw.emoba.thatsapp.ui.screens.*


@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@Composable
fun AppUI(model : ThatsAppModel){
    with(model){
        ThatsAppTheme(model.darkTheme){
            Crossfade(targetState = currentScreen) { screen ->
                when (screen) {
                    Screen.MAIN   -> { MainScreen(model) }
                    Screen.ADDCHAT -> { AddChatScreen(model) }
                    Screen.CHAT -> { ChatScreen(model) }
                    Screen.PROFILE -> {  ProfileScreen(model) }
                    Screen.FULL_IMAGE -> {  FullImageScreen(model) }
                }
            }
        }
    }
}
