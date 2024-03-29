package my.hanitracker.location

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import my.hanitracker.R
import my.hanitracker.firebase.LocationTrackingBusinessLogic
import my.hanitracker.map.MapBusinessLogic

class LocationService : Service() {

    private val service = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var locationTracker: LocationTracker
    private lateinit var notificationManager: NotificationManager
    private lateinit var locationTrackingBusinessLogic: LocationTrackingBusinessLogic
    private val TAG = "DEBUGGING : "

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        locationTracker = LocationTracker(
            applicationContext,
            LocationServices.getFusedLocationProviderClient(applicationContext)
        )
        locationTrackingBusinessLogic = LocationTrackingBusinessLogic()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action){
            START -> startCoroutine()
            STOP -> stopCoroutine()
            ENABLE_GPS -> enableGps()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun enableNetwork() {
        MapBusinessLogic.isConnectivityProvided.value = false
        val closeServiceIntent = Intent(this, LocationBroadCast::class.java).apply {
            action = STOP
        }
        val closeServicePendingIntent = PendingIntent.getBroadcast(this, 0, closeServiceIntent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, getString(R.string.error_notification))
            .setContentTitle("No network provided")
            .setContentText("trying to reconnect ... ")
            .setSmallIcon(R.drawable.white_logo)
//            .addAction(R.drawable.baseline_close_24, "Stop tracking", closeServicePendingIntent)
            .setOngoing(true)
            .build()
        notificationManager.notify(resources.getInteger(R.integer.location_update), notification)


    }

    private fun enableGps() {
        MapBusinessLogic.isGpsProvided.value = false
        val goToGpsSettingsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        val goToGpsSettingsIntentPendingIntent = PendingIntent.getActivity(this, 0, goToGpsSettingsIntent, PendingIntent.FLAG_IMMUTABLE)

        val closeServiceIntent = Intent(this, LocationBroadCast::class.java).apply {
            action = STOP
        }
        val closeServicePendingIntent = PendingIntent.getBroadcast(this, 0, closeServiceIntent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, getString(R.string.error_notification))
            .setContentTitle("GPS is disabled")
            .setContentText("Enable it so your friends can still finds you")
            .setSmallIcon(R.drawable.white_logo)
            .addAction(R.drawable.baseline_gps_fixed_24, "Go to settings", goToGpsSettingsIntentPendingIntent)
//            .addAction(R.drawable.baseline_close_24, "Stop tracking", closeServicePendingIntent)
            .setOngoing(true)
            .build()

        notificationManager.notify(resources.getInteger(R.integer.location_update), notification)

    }

    private fun startCoroutine() {

        val connectivityManager = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        var isNetworkProvided = true

        val closeServiceIntent = Intent(this, LocationBroadCast::class.java).apply {
            action = STOP
        }

        val closeServicePendingIntent = PendingIntent.getBroadcast(this, 0, closeServiceIntent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, getString(R.string.location_tracking_notification))
            .setContentTitle("Tracking Location ...")
            .setContentText("Location: calculating ...")
            .setSmallIcon(R.drawable.white_logo)
//            .addAction(R.drawable.baseline_close_24, "Stop tracking", closeServicePendingIntent)
            .setOngoing(true)

        locationTracker.checkGpsProvider(1000L)

        locationTracker
            .getLocationUpdate(1000L)
            .catch { e -> Log.d(TAG, "startCoroutine getLocationUpdate Exception: ${e.printStackTrace()}") }
            .onEach { location ->
                if (connectivityManager.activeNetworkInfo?.isConnectedOrConnecting == true) {
                    MapBusinessLogic.isTracking.value = true
                    MapBusinessLogic.isConnectivityProvided.value = true
                    MapBusinessLogic.isGpsProvided.value = true
                    isNetworkProvided = true
                    val latitude = location.latitude
                    val longitude = location.longitude
                    locationTrackingBusinessLogic.updateLocationInCloud(latitude, longitude)
                    val updateNotification =
                        notification.setContentText("Location: ($latitude , $longitude)")
                    notificationManager.notify(
                        resources.getInteger(R.integer.location_update),
                        updateNotification.build()
                    )
                }
                else {
                    if (isNetworkProvided){
                        isNetworkProvided = false
                        enableNetwork()
                    }

                }


            }
            .launchIn(service)

        notificationManager.notify(resources.getInteger(R.integer.location_update), notification.build())

        startForeground(resources.getInteger(R.integer.location_update), notification.build())
    }

    private fun stopCoroutine() {
        MapBusinessLogic.isTracking.value = false
        notificationManager.cancelAll()
        locationTrackingBusinessLogic.deleteLocationFromCloud()
        stopForeground(true)
        stopSelf()
        service.cancel()
    }

    companion object {
        const val START = "START"
        const val STOP = "STOP"
        const val ENABLE_GPS = "ENABLE_GPS"
    }



}