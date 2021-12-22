package fhnw.emoba.thatsapp.ui


import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import fhnw.emoba.thatsapp.model.ThatsAppModel


@Composable
fun AppUI(model : ThatsAppModel){
    with(model){
        Text(text = title, style = TextStyle(fontSize = 28.sp))
    }
}
