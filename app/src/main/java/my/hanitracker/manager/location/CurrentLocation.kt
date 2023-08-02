package my.hanitracker.manager.location

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import my.hanitracker.firebase.FirebaseRealtimeStore
import my.hanitracker.manager.UserLocalStorage

object CurrentLocation {

    var isTracking = mutableStateOf(false)

    var latitude = 0.0
        private set
    var longitude = 0.0
        private set

    fun setLocation(latitude : Double, longitude: Double){
        this.latitude = latitude
        this.longitude = longitude
        streamLocation()
    }

    private fun streamLocation() {

        data class Location(val latitude : Double, val longitude: Double)

        FirebaseRealtimeStore.storeData(
            data = Location(latitude, longitude),
            path = "location/${UserLocalStorage.userId}",
            onSuccess = {
//                Log.d("DEBUGGING : ", "streamLocation: SUCCESS")
            },
            onFailure = {
                Log.d("DEBUGGING : ", "streamLocation: FAIL")
            }
        )
    }

    fun stopStreamingLocation() {
        data class Location(val latitude : Double, val longitude: Double)

        FirebaseRealtimeStore.storeData(
            data = null,
            path = "location/${UserLocalStorage.userId}",
            onSuccess = { Log.d("DEBUGGING : ", "streamLocation: SUCCESS") },
            onFailure = { Log.d("DEBUGGING : ", "streamLocation: FAIL") }
        )
    }

}