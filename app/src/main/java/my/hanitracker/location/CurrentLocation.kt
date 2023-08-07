package my.hanitracker.location

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import my.hanitracker.firebase.FirebaseRealtimeStore
import my.hanitracker.user.UserLocalStorage

object CurrentLocation {

    var isTracking = mutableStateOf(false)

    var latitude = 0.0
        private set
    var longitude = 0.0
        private set

    fun setLocation(latitude : Double, longitude: Double){
        CurrentLocation.latitude = latitude
        CurrentLocation.longitude = longitude
        streamLocation()
    }

    private fun streamLocation() {
        val firebaseRealtimeStore = FirebaseRealtimeStore()
        firebaseRealtimeStore.storeData(
            data = hashMapOf("latitude" to latitude, "longitude" to longitude),
            path = "location/${UserLocalStorage.userId}",
            onSuccess = {
                Log.d("DEBUGGING : ", "streamLocation: SUCCESS")
            },
            onFailure = {
                Log.d("DEBUGGING : ", "streamLocation: FAIL")
            }
        )
    }

    fun stopStreamingLocation() {
        val firebaseRealtimeStore = FirebaseRealtimeStore()

        firebaseRealtimeStore.storeData(
            data = null,
            path = "location/${UserLocalStorage.userId}",
            onSuccess = { Log.d("DEBUGGING : ", "streamLocation: SUCCESS") },
            onFailure = { Log.d("DEBUGGING : ", "streamLocation: FAIL") }
        )
    }

}