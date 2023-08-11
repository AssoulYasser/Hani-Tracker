package my.hanitracker

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import my.hanitracker.location.LocationService
import my.hanitracker.location.UserPlaceNameLocationDataClass
import my.hanitracker.manager.PermissionManager
import my.hanitracker.map.MapBusinessLogic
import my.hanitracker.ui.screens.MapScreen
import my.hanitracker.ui.theme.CircularProgress
import my.hanitracker.ui.theme.HaniTrackerTheme

class MapActivity : ComponentActivity() {
    private lateinit var mapBusinessLogic: MapBusinessLogic
    private lateinit var permissionManager: PermissionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionManager = PermissionManager(this)
        checkNotificationPermission()

        setContent {
            val isLoading = remember { mutableStateOf(false) }
            val isTracking = remember { mutableStateOf(false) }

            fun startLoading() {
                isLoading.value = true
            }

            fun stopLoading() {
                isLoading.value = false
            }

            CircularProgress(isLoading = isLoading)
            HaniTrackerTheme {
                startLoading()
                MapScreen(
                    startTracking = { startTracking() },
                    stopTracking = { stopTracking() },
                    onCenterCameraPosition = { centerCameraPosition() },
                    onListingOnlinePeople = { listOnlinePeople() },
                ) { mapView ->
                    Log.d("DEBUGGING : ", "onCreate: BEFORE HAVING BITMAP EXCEPTION")
                    mapBusinessLogic = MapBusinessLogic(this, mapView)
                    isLoading.value = false
                }
            }
        }
    }

    private fun listOnlinePeople() : MutableList<UserPlaceNameLocationDataClass> {
        return mapBusinessLogic.getActiveUsersPlaces()
    }

    private fun centerCameraPosition() {
        mapBusinessLogic.adjustCameraPosition()
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
            return

        val notificationPermission = Manifest.permission.POST_NOTIFICATIONS
        if (permissionManager.isPermissionGranted(notificationPermission))
            return

        permissionManager.requestPermission(notificationPermission)
    }

    private fun isLocationPermissionGranted() : Boolean {
        if (!permissionManager.isPermissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION))
            return false
        if (!permissionManager.isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION))
            return false
        return true
    }

    private fun startTracking() : Boolean {
        if (!isLocationPermissionGranted()) {
            permissionManager.requestPermission(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION))
            return false
        }

        Intent(applicationContext, LocationService::class.java).apply {
            action = LocationService.START
            startService(this)
        }
        mapBusinessLogic.startTracking()

        return true

    }

    private fun stopTracking() {
        Intent(applicationContext, LocationService::class.java).apply {
            action = LocationService.STOP
            startService(this)
        }
        mapBusinessLogic.stopTracking()
    }

    @Deprecated("Deprecated in Java", ReplaceWith(
        "super.onRequestPermissionsResult(requestCode, permissions, grantResults)",
        "androidx.activity.ComponentActivity"
        )
    )
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionManager.onPermissionResult(requestCode, permissions, grantResults)
    }

}