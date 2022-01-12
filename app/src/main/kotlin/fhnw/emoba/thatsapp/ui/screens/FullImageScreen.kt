package fhnw.emoba.thatsapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import fhnw.emoba.thatsapp.model.ThatsAppModel

@Composable
fun FullImageScreen(model: ThatsAppModel) {
    ImageFullSize(model)
}

@Composable
fun ImageFullSize(model: ThatsAppModel){
    Image(
        bitmap = model.showImageBig.asImageBitmap(),
        contentDescription = "Image shown full size",
        modifier = Modifier
            .fillMaxSize()
            .clickable { model.currentScreen = model.backScreen }
    )
}