package my.hanitracker.map

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation
import my.hanitracker.firebase.LocationTrackingBusinessLogic
import my.hanitracker.location.UserGeoLocationDataClass
import my.hanitracker.location.UserPlaceNameLocationDataClass
import my.hanitracker.manager.BitmapManager
import my.hanitracker.user.UserLocalStorage

class MapBusinessLogic(private val activity: Activity, mapView: MapView) {
    private var mapBoxManager: MapBoxManager
    private val locationTrackingBusinessLogic: LocationTrackingBusinessLogic
    private val bitmapManager: BitmapManager
    private val locations = mutableStateMapOf<String, PointAnnotation>()
    private val context: Context

    init {
        mapBoxManager = MapBoxManager(mapView)
        locationTrackingBusinessLogic = LocationTrackingBusinessLogic()
        bitmapManager = BitmapManager(activity.applicationContext)
        context = activity.applicationContext
    }

    companion object{
        var isTracking = mutableStateOf(false)
        var isGpsProvided = mutableStateOf(true)
        var isConnectivityProvided = mutableStateOf(true)
        val users = mutableStateListOf<UserPlaceNameLocationDataClass>()
    }

    private fun Bitmap.setAnnotationBitmap(): Bitmap? = bitmapManager.createCircularBitmapWithImage(this, 10.0f, Color.BLACK)

    private fun addLocation(userLocation: UserGeoLocationDataClass){
        mapBoxManager.getPlaceName(
            context = context,
            latitude = userLocation.latitude,
            longitude = userLocation.longitude,
            onSuccess = {
                users.add(UserPlaceNameLocationDataClass(userLocation.user, it, userLocation.latitude, userLocation.longitude))
            },
            onFailure = {
                Toast.makeText(context, "Somethings went wrong in Map Box Business Logic", Toast.LENGTH_LONG).show()
            }
        )
        bitmapManager.uriToBitmap(
            uri = userLocation.user.pfp,
            callback = { pfpBitmap ->
                Log.d("DEBUGGING : ", "addLocation: INSIDE BITMAP AND I GUESS BEFORE EXCPETION")
                val pointAnnotationImage =
                    pfpBitmap?.setAnnotationBitmap() ?: return@uriToBitmap
                val pointAnnotationOption = mapBoxManager.createPointAnnotationOption(
                    latitude = userLocation.latitude,
                    longitude = userLocation.longitude,
                    bitmap = pointAnnotationImage
                )
                val pointAnnotation = mapBoxManager.addPointAnnotation(pointAnnotationOptions = pointAnnotationOption)
                locations[userLocation.user.uid] = pointAnnotation
            }
        )
    }

    private fun updateLocation(uid: String, latitude: Double, longitude: Double){
        mapBoxManager.getPlaceName(
            context = context,
            latitude = latitude,
            longitude = longitude,
            onSuccess = {
                var index = 0
                users.forEach{ user ->
                    if (user.user.uid == uid) {
                        users[index] = UserPlaceNameLocationDataClass(user.user, it, latitude, longitude)
                        return@forEach
                    }
                    index += 1
                }
            },
            onFailure = {
                Toast.makeText(context, "Somethings went wrong in Map Box Business Logic", Toast.LENGTH_LONG).show()
            }
        )
        if (locations.containsKey(uid)) {
            mapBoxManager.updatePointAnnotationLocation(
                pointAnnotation = locations[uid]!!,
                latitude = latitude,
                longitude = longitude
            )
        }
    }

    private fun deleteLocation(uid: String){
        if (locations.containsKey(uid)) {
            mapBoxManager.deletePointAnnotation(locations[uid]!!)
            var userToDelete = -1
            users.forEach {  user ->
                userToDelete += 1
                if (user.user.uid == uid)
                    return
            }
            if (userToDelete >= 0)
                users.removeAt(userToDelete)
        }
    }

    private fun deleteAllLocations() {
        locations.clear()
        users.clear()
        mapBoxManager.deleteAllPointAnnotations()
    }

    fun startTracking() {
        locationTrackingBusinessLogic.startTrackLocation(
            onAdd = ::addLocation,
            onChange = ::updateLocation,
            onDelete = ::deleteLocation,
            onFailure = {}
        )
    }

    fun stopTracking() {
        locationTrackingBusinessLogic.stopTrackingLocation()
        deleteAllLocations()
    }

    fun adjustCameraPosition() {
        val userGeometry = locations[UserLocalStorage.userId] ?: return

        val latitude = userGeometry.geometry.latitude()
        val longitude = userGeometry.geometry.longitude()
        mapBoxManager.adjustCameraPosition(20.0, latitude, longitude)
    }

    fun adjustCameraPosition(latitude: Double, longitude: Double){
         mapBoxManager.adjustCameraPosition(20.0, latitude, longitude)
    }

    fun onException() {

    }



}