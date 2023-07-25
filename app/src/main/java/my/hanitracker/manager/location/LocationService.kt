package my.hanitracker.manager.location

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
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

class LocationService : Service() {

    private val service = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var locationTracker: LocationTracker
    private lateinit var notificationManager: NotificationManager
    private val TAG = "DEBUGGING : "

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate: START")
        locationTracker = LocationTracker(
            applicationContext,
            LocationServices.getFusedLocationProviderClient(applicationContext)
        )
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        Log.d(TAG, "onCreate: END")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: START")
        when(intent?.action){
            START -> startCoroutine()
            STOP -> stopCoroutine()
            ENABLE_GPS -> enableGps()
        }
        Log.d(TAG, "onStartCommand: END")
        return super.onStartCommand(intent, flags, startId)
    }

    private fun enableGps() {
        Log.d(TAG, "enableGps: START")

        val goToGpsSettingsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        val goToGpsSettingsIntentPendingIntent = PendingIntent.getActivity(this, 0, goToGpsSettingsIntent, PendingIntent.FLAG_IMMUTABLE)

        val closeServiceIntent = Intent(this, LocationBroadCast::class.java).apply {
            action = STOP
        }
        val closeServicePendingIntent = PendingIntent.getBroadcast(this, 0, closeServiceIntent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, getString(R.string.error_notification))
            .setContentTitle("GPS is disabled")
            .setContentText("Enable it so your friends can still finds you")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .addAction(R.drawable.baseline_gps_fixed_24, "Go to settings", goToGpsSettingsIntentPendingIntent)
            .addAction(R.drawable.baseline_close_24, "Stop tracking", closeServicePendingIntent)
            .setOngoing(true)
            .build()

        notificationManager.notify(resources.getInteger(R.integer.location_update), notification)

        Log.d(TAG, "enableGps: END")
    }

    private fun startCoroutine() {
        Log.d(TAG, "startCoroutine: START")
        val notification = NotificationCompat.Builder(this, getString(R.string.location_tracking_notification))
            .setContentTitle("Tracking Location ...")
            .setContentText("Location: calculating ...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)

        locationTracker.checkHardwareAvailability(1000L)

        locationTracker
            .getLocationUpdate(1000L)
            .catch { e -> Log.d(TAG, "startCoroutine getLocationUpdate Exception: ${e.printStackTrace()}") }
            .onEach { location ->
                if (!locationTracker.isCheckingTheHardware) {
                    locationTracker.checkHardwareAvailability(1000L)
                    locationTracker.isCheckingTheHardware = true
                }
                val latitude = location.latitude.toString()
                val longitude = location.longitude.toString()
                val updateNotification = notification.setContentText("Location: ($latitude , $longitude)")
                notificationManager.notify(resources.getInteger(R.integer.location_update), updateNotification.build())
            }
            .launchIn(service)

        notificationManager.notify(resources.getInteger(R.integer.location_update), notification.build())

        startForeground(resources.getInteger(R.integer.location_update), notification.build())

        Log.d(TAG, "startCoroutine: END")

    }

    private fun stopCoroutine() {
        Log.d(TAG, "stopCoroutine: START")
        stopForeground(true)
        stopSelf()
        Log.d(TAG, "stopCoroutine: END")
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy: START")
        super.onDestroy()
        service.cancel()
        Log.d(TAG, "onDestroy: END")
    }

    companion object {
        const val START = "START"
        const val STOP = "STOP"
        const val ENABLE_GPS = "ENABLE_GPS"
        const val ENABLE_NETWORK = "ENABLE_NETWORK"
    }



}