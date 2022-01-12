package fhnw.emoba.thatsapp.data.connectors

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import fhnw.emoba.thatsapp.data.GeoPosition

class GPSConnector(val activity: Activity) {
    private val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
                                      Manifest.permission.ACCESS_COARSE_LOCATION)

    private val locationProvider by lazy { LocationServices.getFusedLocationProviderClient(activity) }

    init {
        ActivityCompat.requestPermissions(activity, permissions, 10)
    }

    @SuppressLint("MissingPermission")
    fun getLocation(onSuccess:          (geoPosition: GeoPosition) -> Unit, // Je nach Situation
                    onFailure:          (exception: Exception) -> Unit,
                    onPermissionDenied: () -> Unit)  {
        if (permissions.oneOfGranted()) {
            locationProvider.lastLocation // Zugriff auf Sensor
                .addOnSuccessListener(activity) {
                    onSuccess.invoke(GeoPosition(it.longitude, it.latitude, it.altitude))
                }
                .addOnFailureListener(activity) {
                    onFailure.invoke(it)
                }
        }
        else {
            onPermissionDenied.invoke()
        }
    }

    private fun Array<String>.oneOfGranted() : Boolean {
        var any = false
        forEach { any = any || it.granted() }

        return any
    }

    private fun String.granted(): Boolean = ActivityCompat.checkSelfPermission(activity, this) == PackageManager.PERMISSION_GRANTED
}

