package my.hanitracker

import android.annotation.SuppressLint
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

class App: Application() {

    override fun onCreate() {
        super.onCreate()
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            setNotificationChannels()
    }

    @SuppressLint("NewApi")
    private fun setNotificationChannels() {

        val locationTrackingChannel = NotificationChannel(
            getString(R.string.location_tracking_notification),
            "Location",
            NotificationManager.IMPORTANCE_LOW
        )

        locationTrackingChannel.description = "This channel for providing location data"

        val errorChannel = NotificationChannel(
            getString(R.string.error_notification),
            "Error",
            NotificationManager.IMPORTANCE_HIGH
        )

        errorChannel.description = "This channel is for error handling"

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(locationTrackingChannel)
        notificationManager.createNotificationChannel(errorChannel)

    }


}