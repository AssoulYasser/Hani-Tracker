package my.hanitracker

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import my.hanitracker.manager.PermissionManager.PERMISSION_REQUEST_CODE
import my.hanitracker.manager.PermissionManager.isPermissionGranted
import my.hanitracker.manager.PermissionManager.requestPermissions
import my.hanitracker.location.LocationService
import my.hanitracker.map.MapBusinessLogic
import my.hanitracker.ui.screens.Main
import my.hanitracker.ui.theme.CircularProgress
import my.hanitracker.ui.theme.HaniTrackerTheme

class MapActivity : ComponentActivity() {
    private lateinit var mapBusinessLogic: MapBusinessLogic

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val isLoading = remember { mutableStateOf(false) }

            fun startLoading() {
                isLoading.value = true
            }

            fun stopLoading() {
                isLoading.value = false
            }

            HaniTrackerTheme {
                startLoading()
                Main(
                    startTracking = { startTracking() },
                    stopTracking = { stopTracking() }
                ) { mapView ->
                    mapBusinessLogic = MapBusinessLogic(this, mapView)
                    isLoading.value = false
                }
            }
            CircularProgress(isLoading = isLoading)
        }
    }


    private fun startTracking() {
        val permissionList = mutableListOf<String>()

        if(!this.isPermissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION))
            permissionList.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        if(!this.isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION))
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION)
        if(!this.isPermissionGranted(Manifest.permission.POST_NOTIFICATIONS) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            permissionList.add(Manifest.permission.POST_NOTIFICATIONS)

        if(permissionList.isNotEmpty())
            this.requestPermissions(permissionList)
        else {
            Intent(applicationContext, LocationService::class.java).apply {
                action = LocationService.START
                startService(this)
            }
            mapBusinessLogic.startTracking()
        }

    }

    private fun stopTracking() {
        Intent(applicationContext, LocationService::class.java).apply {
            action = LocationService.STOP
            startService(this)
        }
        mapBusinessLogic.stopTracking()
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode) {
            PERMISSION_REQUEST_CODE -> {

                for (index in permissions.indices) {
                    if (grantResults[index] == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(
                            this,
                            "${permissions[index]} has been granted",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    else
                        Toast.makeText(
                            this,
                            "${permissions[index]} has not granted",
                            Toast.LENGTH_SHORT
                        ).show()
                }
            }
            else -> throw Exception("UNKNOWN PERMISSION REQUEST CODE FOUND")
        }
    }



}