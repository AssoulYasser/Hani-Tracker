package my.hanitracker.ui.screens

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
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
import my.hanitracker.firebase.FirebaseFireStore
import my.hanitracker.firebase.FirebaseRealtimeStore
import my.hanitracker.location.CurrentLocation
import my.hanitracker.manager.friends.FriendLocation
import my.hanitracker.manager.setAnnotationBitmap
import my.hanitracker.manager.uriToBitmap
import my.hanitracker.ui.theme.CircularProgress

private lateinit var firebaseRealtimeStore: FirebaseRealtimeStore
private lateinit var firebaseFireStore: FirebaseFireStore
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
    annotationUid: String,
    latitude: Double,
    longitude: Double
) {
    if (annotations.containsKey(annotationUid)) {
        val mAnnotation = annotations[annotationUid]!!
        mAnnotation.point = Point.fromLngLat(latitude, longitude)
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
    firebaseRealtimeStore = FirebaseRealtimeStore()
    firebaseFireStore = FirebaseFireStore()
    

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
            firebaseRealtimeStore.trackData(
                "location",
                onAdd = { dataSnapshot ->
                    firebaseFireStore.getDocumentWhereDataEqualTo(
                        collection = "user",
                        fieldName = "uid",
                        fieldValue = dataSnapshot.key!!,
                        onSuccess = { querySnapshot ->
                            for (document in querySnapshot){
                                val location = dataSnapshot.value
                                if (location is Map<*, *>)
                                    addAnnotationToMap(
                                        context = context,
                                        friendLocation = FriendLocation(
                                            uid = dataSnapshot.key!!,
                                            fullName = "${document.get("first name")} ${document.get("last name")}",
                                            photo = Uri.parse(document.get("profile picture uri") as String),
                                            latitude = location["latitude"] as Double,
                                            longitude = location["longitude"] as Double
                                        )
                                    )
                            }
                        },
                        onFailure = {
                            Toast.makeText(context, it.message, Toast.LENGTH_LONG).show()
                        })
                },
                onChange = {
                    val location = it.value as Map<*, *>
                    updateAnnotationInMap(it.key!!, location["latitude"] as Double, location["longitude"] as Double)
                },
                onDelete = {
                    deleteAnnotation(annotations[it.key!!]!!)
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