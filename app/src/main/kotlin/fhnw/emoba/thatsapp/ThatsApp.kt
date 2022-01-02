package fhnw.emoba.thatsapp

import androidx.activity.ComponentActivity
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import fhnw.emoba.EmobaApp
import fhnw.emoba.thatsapp.model.ThatsAppModel
import fhnw.emoba.thatsapp.ui.AppUI


object ThatsApp : EmobaApp {
    private lateinit var thatsAppModel: ThatsAppModel

    override fun initialize(activity: ComponentActivity) {
        thatsAppModel = ThatsAppModel(activity)
        thatsAppModel.connectAndSubscribe()
    }

    @ExperimentalMaterialApi
    @ExperimentalComposeUiApi
    @Composable
    override fun CreateUI() {
        AppUI(thatsAppModel)
    }

}

