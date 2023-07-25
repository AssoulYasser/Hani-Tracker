package my.hanitracker

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import my.hanitracker.manager.PermissionManager.PERMISSION_REQUEST_CODE
import my.hanitracker.manager.PermissionManager.isPermissionGranted
import my.hanitracker.manager.PermissionManager.requestPermission
import my.hanitracker.manager.PermissionManager.requestPermissions
import my.hanitracker.manager.location.LocationService
import my.hanitracker.ui.screens.Main
import my.hanitracker.ui.theme.HaniTrackerTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HaniTrackerTheme {
                Main(
                    startTracking = { startTracking() },
                    stopTracking = { stopTracking() }
                )
            }
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
        else
            Intent(applicationContext, LocationService::class.java).apply {
                action = LocationService.START
                startService(this)
            }

    }

    private fun stopTracking() {
        Intent(applicationContext, LocationService::class.java).apply {
            action = LocationService.STOP
            startService(this)
        }
    }

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