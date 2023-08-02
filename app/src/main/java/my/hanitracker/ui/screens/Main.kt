package my.hanitracker.ui.screens

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.mapbox.geojson.Point
import com.mapbox.maps.MapView
import com.mapbox.maps.ResourceOptionsManager
import com.mapbox.maps.plugin.annotation.AnnotationPlugin
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import my.hanitracker.R
import my.hanitracker.firebase.FirebaseRealtimeStore
import my.hanitracker.firebase.FirebaseUsers
import my.hanitracker.manager.UserLocalStorage
import my.hanitracker.manager.friends.FriendLocation
import my.hanitracker.manager.friends.FriendLocationManager
import my.hanitracker.manager.location.CurrentLocation
import my.hanitracker.manager.setAnnotationBitmap
import my.hanitracker.manager.uriToBitmap
import my.hanitracker.ui.theme.CircularProgress

private lateinit var map : MapView
private lateinit var annotationApi : AnnotationPlugin
private lateinit var pointAnnotationManager : PointAnnotationManager
private var annotations = HashMap<String,PointAnnotation>()


private fun addAnnotationToMap(
    context: Context,
    friendLocation: FriendLocation
) {
    uriToBitmap(context, friendLocation.photo) {
        if (it == null)
            return@uriToBitmap

        val bitmap = it.setAnnotationBitmap(context)
        Log.d("DEBUGGING : ", "addAnnotationToMap: $bitmap")
        if (bitmap != null) {
            val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
                .withPoint(Point.fromLngLat(friendLocation.longitude, friendLocation.latitude))
                .withIconImage(bitmap)
            val annotationId = pointAnnotationManager.create(pointAnnotationOptions)
            annotations[friendLocation.uid] = annotationId
        }
    }
}

private fun updateAnnotationInMap(
    context: Context,
    friendLocation: FriendLocation
) {
    if (annotations.containsKey(friendLocation.uid)) {
        val mAnnotation = annotations[friendLocation.uid]!!
        mAnnotation.point = Point.fromLngLat(friendLocation.longitude, friendLocation.latitude)
    }

}

private fun deleteAllAnnotations() {
    if (::pointAnnotationManager.isInitialized) {
        pointAnnotationManager.deleteAll()
    }
}

private fun deleteAnnotation(pointAnnotation: PointAnnotation) {
    if (::pointAnnotationManager.isInitialized) {
        pointAnnotationManager.delete(pointAnnotation)
    }
}


@Composable
fun Main(
    startTracking : () -> Unit,
    stopTracking : () -> Unit
) {
    val isLoading = remember { mutableStateOf(false) }
    val context = LocalContext.current

    fun startLoading() {
        isLoading.value = true
    }

    fun stopLoading() {
        isLoading.value = false
    }
    val TAG = "DEBUGGING : "

    CircularProgress(isLoading = isLoading)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        AndroidView(
            modifier = Modifier,
            factory = { fContext ->
                ResourceOptionsManager.getDefault(
                    fContext,
                    fContext.getString(R.string.mapbox_access_token)
                )
                startLoading()
                map = MapView(fContext)
                annotationApi = map.annotations
                pointAnnotationManager = annotationApi.createPointAnnotationManager(map)
                stopLoading()
                map
            }
        )


        if (CurrentLocation.isTracking.value) {
            Log.d("DEBUGGING : ", "Main: START TRACKING")
            startLoading()

            FirebaseRealtimeStore.trackData(
                "location",
                onAdd = {
                    FirebaseUsers.getUserData(uid =  it.key!!, onSuccess = {}, onFailure = {})
//                    val uid = it.key!!
//                    if (annotations.containsKey(uid))
//                        return@trackData
//                    val location = it.value
//                    val latitude : Double
//                    val longitude : Double
//                    if (location is Map<*, *>) {
//                        latitude = location["latitude"] as Double
//                        longitude = location["longitude"] as Double
//                        Log.d(TAG, "data added : uid = $uid, latitude = $latitude, longitude = $longitude")
//                        FriendLocationManager.fetchLocation(
//                            uid = uid,
//                            latitude = latitude,
//                            longitude = longitude,
//                            onSuccess = { friendLocation ->
//                                addAnnotationToMap(context, friendLocation)
//                            },
//                            onFailure = { exception ->
//                                throw exception
//                            }
//                        )
//                    }
                },
                onChange = {
                    Log.d(TAG, "data changed : $it")
                },
                onDelete = {
                    Log.d(TAG, "data deleted : $it")
                }
            )



            stopLoading()
        } else {
            Log.d("DEBUGGING : ", "Main: STOP TRACKING")
            deleteAllAnnotations()
        }


        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(Color.White)
        ) {
            Button(onClick = startTracking) {
                Text(text = "Start")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = stopTracking) {
                Text(text = "Stop")
            }
        }
    }

}