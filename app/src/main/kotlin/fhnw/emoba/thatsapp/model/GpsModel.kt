package fhnw.emoba.thatsapp.model

import android.graphics.BitmapFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import androidx.activity.ComponentActivity
import androidx.annotation.DrawableRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import fhnw.emoba.thatsapp.data.connectors.GPSConnector
import fhnw.emoba.thatsapp.data.GeoPosition

class GpsModel(private val activity: ComponentActivity,
               private val locator: GPSConnector
) {

    private val modelScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    //val theseusImage = loadImage(R.drawable.theseus)

    val waypoints = mutableStateListOf<GeoPosition>()
    var notificationMessage by mutableStateOf("")

    // asynchroner Aufruf
    fun rememberCurrentPosition(){
        //modelScope.launch { // Code wird jetzt im UI /main Thread ausgeführt
            locator.getLocation(onSuccess          = { waypoints.add(it)
                                                       notificationMessage = "neuer Wegpunkt"
                                                     },
                                onFailure          = {}, //todo: was machen wir?
                                onPermissionDenied = { notificationMessage = "Keine Berechtigung." }  //todo: was machen wir?
            )
        //}
    }

    fun removeWaypoint(position: GeoPosition) {
        waypoints.remove(position)
    }

    private fun loadImage(@DrawableRes id: Int) : ImageBitmap {
        return BitmapFactory.decodeResource(activity.resources, id).asImageBitmap()
    }
}

