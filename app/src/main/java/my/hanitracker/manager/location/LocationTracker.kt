package my.hanitracker.manager.location

import android.content.Context
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import my.hanitracker.manager.PermissionManager
import my.hanitracker.manager.PermissionManager.isPermissionGranted
import java.util.Timer
import java.util.TimerTask

class LocationTracker(private val context : Context, private val client: FusedLocationProviderClient) : LocationClient {

    private val TAG = "DEBUGGING : "
    var isCheckingTheHardware = true

    private fun Context.hasLocationPermission() : Boolean =
        this.isPermissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION) && this.isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)

    @SuppressLint("MissingPermission")
    override fun getLocationUpdate(interval: Long): Flow<Location> = callbackFlow {
        Log.d(TAG, "getLocationUpdate: START")
        if (!context.hasLocationPermission())
            throw LocationClient.LocationException("PERMISSION NOT GRANTED")


        val locateRequest = LocationRequest.create().setInterval(interval).setFastestInterval(interval)

        val locationRequestCallBack = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                Log.d(TAG, "onLocationResult: START")
                super.onLocationResult(result)
                result.locations.lastOrNull()?.let { location -> launch { send(location) } }
                Log.d(TAG, "onLocationResult: END")
            }
        }

        client.requestLocationUpdates(
            locateRequest,
            locationRequestCallBack,
            Looper.getMainLooper()
        )

        awaitClose {
            client.removeLocationUpdates(locationRequestCallBack)
        }

    }

    override fun checkHardwareAvailability(interval: Long) {
        val statusCheckTimer = Timer()
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        statusCheckTimer.scheduleAtFixedRate(
            object : TimerTask() {
                override fun run() {
                    if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        Intent(context, LocationService::class.java).apply {
                            action = LocationService.ENABLE_GPS
                            context.startService(this)
                        }
                        isCheckingTheHardware = false
                        statusCheckTimer.cancel()
                    }

                    else if (!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                        Intent(context, LocationService::class.java).apply {
                            action = LocationService.ENABLE_NETWORK
                            context.startService(this)
                        }
                        isCheckingTheHardware = false
                        statusCheckTimer.cancel()
                    }
                }
            },
            0,
            interval
        )
    }

}