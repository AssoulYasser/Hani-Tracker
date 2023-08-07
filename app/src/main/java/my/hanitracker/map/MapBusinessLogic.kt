package my.hanitracker.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation
import my.hanitracker.firebase.LocationTrackingBusinessLogic
import my.hanitracker.location.UserLocationDataClass
import my.hanitracker.manager.BitmapManager

class MapBusinessLogic(val context: Context, mapView: MapView) {
    private var mapBoxManager: MapBoxManager
    private val locationTrackingBusinessLogic: LocationTrackingBusinessLogic
    private val bitmapManager: BitmapManager
    private val locations = HashMap<String, PointAnnotation>()


    init {
        mapBoxManager = MapBoxManager(mapView)
        locationTrackingBusinessLogic = LocationTrackingBusinessLogic()
        bitmapManager = BitmapManager()
    }

    private fun Bitmap.setAnnotationBitmap(context: Context) : Bitmap? = bitmapManager.createCircularBitmapWithImage(context, this, 10.0f, Color.BLACK)


    private fun addLocation(userLocation: UserLocationDataClass){
        bitmapManager.uriToBitmap(
            context = context,
            uri = userLocation.user.pfp,
            callback = { pfpBitmap ->
                val pointAnnotationImage =
                    pfpBitmap?.setAnnotationBitmap(context = context) ?: return@uriToBitmap
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
        if (locations.containsKey(uid))
            mapBoxManager.updatePointAnnotationLocation(
                pointAnnotation = locations[uid]!!,
                latitude = latitude,
                longitude = longitude
            )
    }

    private fun deleteLocation(uid: String){
        if (locations.containsKey(uid))
            mapBoxManager.deletePointAnnotation(locations[uid]!!)
    }

    private fun deleteAllLocations() {
        locations.clear()
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

    fun onException() {

    }


}