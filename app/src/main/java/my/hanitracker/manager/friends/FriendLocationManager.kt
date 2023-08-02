package my.hanitracker.manager.friends

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import my.hanitracker.firebase.FirebaseCloudStore
import my.hanitracker.firebase.FirebaseFireStore
import my.hanitracker.firebase.FirebaseRealtimeStore
import my.hanitracker.manager.UserLocalStorage

object FriendLocationManager {
    fun fetchLocation(
        uid: String,
        latitude: Double,
        longitude: Double,
        onSuccess: (FriendLocation) -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        FirebaseFireStore.getData(
            collection = "user",
            document = uid,
            onSuccess = { user ->
                FirebaseCloudStore.getFileUri(
                    location = "user/$uid/pfp",
                    onSuccess = { pfp ->
                        val newUserLocation = FriendLocation(
                            uid = uid,
                            fullName = "${user["first name"]} ${user["last name"]}",
                            photo = pfp!!,
                            latitude = latitude,
                            longitude = longitude
                        )
                        onSuccess(newUserLocation)
                        Log.d(
                            "DEBUGGING : ",
                            "fetchLocations: new user : $newUserLocation"
                        )

                    },
                    onFailure = {
                        Log.d("DEBUGGING : ", "fetchLocations: $it")
                        onFailure(it)
                    }
                )

            },
            onFailure = {
                Log.d("DEBUGGING : ", "fetchLocations: $it")
                onFailure(it)
            })
    }

}