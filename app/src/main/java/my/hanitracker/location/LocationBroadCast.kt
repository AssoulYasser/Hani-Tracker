package my.hanitracker.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class LocationBroadCast : BroadcastReceiver() {

    private val TAG = "DEBUGGING : "

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "onReceive: START")
        when(intent?.action) {
            LocationService.STOP -> {
                Log.d(TAG, "onReceive: LocationService.STOP : START")
                Intent(context, LocationService::class.java).apply {
                    action = LocationService.STOP
                    context?.startService(this)
                }
                Log.d(TAG, "onReceive: LocationService.STOP : END")
            }
        }

        Log.d(TAG, "onReceive: END")

    }
}