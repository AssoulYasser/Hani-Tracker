package my.hanitracker.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class LocationBroadCast : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        when(intent?.action) {
            LocationService.STOP -> {
                Intent(context, LocationService::class.java).apply {
                    action = LocationService.STOP
                    context?.startService(this)
                }
            }
        }

    }

}