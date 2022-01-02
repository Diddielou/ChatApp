package fhnw.emoba.thatsapp.model

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import fhnw.emoba.R
import fhnw.emoba.thatsapp.data.downloadBitmapFromFileIO
import fhnw.emoba.thatsapp.data.uploadBitmapToFileIO

class FileIOModel(val activity: Activity) {
    private val modelScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    //var currentTab by mutableStateOf(HomeScreenTab.UPLOAD)

    // ausnahmsweise mal nicht asynchron
    // TODO get picture chosen
    val originalCrew by lazy {  }

    var fileioURL          by mutableStateOf<String?>(null)
    var uploadInProgress   by mutableStateOf(false)

    var downloadedCrew     by mutableStateOf<Bitmap?>(null)
    var downloadInProgress by mutableStateOf(false)
    var downloadMessage    by mutableStateOf("")

    // TODO
    /*
    fun uploadToFileIO() {
        uploadInProgress = true
        fileioURL = null
        modelScope.launch {
            uploadBitmapToFileIO(bitmap    = originalCrew,
                                 onSuccess = { fileioURL = it},
                                 onError   = {_, _ -> })  //todo: was machen wir denn nun?
            uploadInProgress = false
        }
    }
     */

    fun downloadFromFileIO(){
        if(fileioURL != null){
            downloadedCrew = null
            downloadInProgress = true
            modelScope.launch {
                downloadBitmapFromFileIO(url       = fileioURL!!,
                                         onSuccess = { downloadedCrew = it},
                                         onDeleted = { downloadMessage = "File is deleted"},
                                         onError   = { downloadMessage = "Connection failed"})
                downloadInProgress = false
            }
        }
    }
}

