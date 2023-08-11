package my.hanitracker.location

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.net.ConnectivityManager
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask

class LocationTracker(private val context : Context, private val client: FusedLocationProviderClient) : LocationClient {

    var isGpsProvided = true

    @SuppressLint("MissingPermission")
    override fun getLocationUpdate(interval: Long): Flow<Location> = callbackFlow {

        val locateRequest = LocationRequest.create().setInterval(interval).setFastestInterval(interval)

        val locationRequestCallBack = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                super.onLocationResult(result)
                result.locations.lastOrNull()?.let { location -> launch { send(location) } }
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

    override fun checkGpsProvider(interval: Long) {
        val statusCheckTimer = Timer()
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        statusCheckTimer.scheduleAtFixedRate(
            object : TimerTask() {
                override fun run() {
                    if (isGpsProvided && !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        Intent(context, LocationService::class.java).apply {
                            action = LocationService.ENABLE_GPS
                            context.startService(this)
                        }
                        isGpsProvided = false
                    }
                }
            },
            0,
            interval
        )
    }

}