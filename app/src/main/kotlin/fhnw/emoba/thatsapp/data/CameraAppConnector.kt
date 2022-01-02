package fhnw.emoba.thatsapp.data

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider

class CameraAppConnector(val activity: ComponentActivity) {

    private val photoFile          by lazy { getFile("photo.jpg") }  //alle Bilder werden in diesen File abgespeichert
    private val imageCaptureIntent by lazy { createImageCaptureIntent() }

    private var lastBitmap: Bitmap? = null

    private lateinit var onSuccess : (Bitmap) -> Unit
    private lateinit var onCanceled: () -> Unit

    private val cameraLauncher = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val options = BitmapFactory.Options().apply {
                inMutable = true
                if(lastBitmap != null){
                    inBitmap = lastBitmap
                }
            }
            //wenn die CameraApp "zurueckkommt", dann wird der photofile eingelesen und
            //die Bitmap als Resultat weitergereicht
            val lastBitmap = BitmapFactory.decodeFile(photoFile.absolutePath, options)
            onSuccess.invoke(lastBitmap)
        }
        else if(result.resultCode == Activity.RESULT_CANCELED){
            onCanceled.invoke()
        }
    }

    fun getBitmap(onSuccess:  (Bitmap) -> Unit,
                  onCanceled: () -> Unit) {
        this.onSuccess = onSuccess
        this.onCanceled = onCanceled
        cameraLauncher.launch(imageCaptureIntent)
    }

    private fun createImageCaptureIntent(): Intent =
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            val providerFile = FileProvider.getUriForFile(
                activity,
                "fhnw.emoba.fileprovider",
                photoFile
            )
            putExtra(MediaStore.EXTRA_OUTPUT, providerFile)  //die CameraApp speichert in den 'photofile'
        }

    @SuppressLint("SetWorldWritable")
    private fun getFile(fileName: String): File =
        File(activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileName).apply {
            createNewFile()
            setWritable(true, false)
        }
}