package my.hanitracker.map

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.mapbox.api.geocoding.v5.MapboxGeocoding
import com.mapbox.api.geocoding.v5.models.GeocodingResponse
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.annotation.AnnotationPlugin
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import my.hanitracker.R
import kotlin.reflect.typeOf

class MapBoxManager(private val mapView: MapView) {

    private var annotationApi : AnnotationPlugin = mapView.annotations
    private var pointAnnotationManager : PointAnnotationManager = annotationApi.createPointAnnotationManager(mapView)

    fun createPointAnnotationOption(latitude: Double, longitude: Double, bitmap: Bitmap) : PointAnnotationOptions = PointAnnotationOptions()
            .withPoint(Point.fromLngLat(longitude, latitude))
            .withIconImage(bitmap)


    fun addPointAnnotation(pointAnnotationOptions: PointAnnotationOptions) = pointAnnotationManager.create(pointAnnotationOptions)

    fun updatePointAnnotationLocation(pointAnnotation: PointAnnotation, latitude: Double, longitude: Double) {
        pointAnnotation.point = Point.fromLngLat(longitude, latitude)
        pointAnnotationManager.update(pointAnnotation)
    }

    fun deletePointAnnotation(pointAnnotation: PointAnnotation) {
        pointAnnotationManager.delete(pointAnnotation)
    }

    fun deleteAllPointAnnotations() {
        pointAnnotationManager.deleteAll()
    }

    fun adjustCameraPosition(zoomLevel: Double, latitude: Double, longitude: Double) {
        val cameraPosition = CameraOptions.Builder()
            .zoom(zoomLevel)
            .center(Point.fromLngLat(longitude, latitude))
            .build()

        mapView.getMapboxMap().setCamera(cameraPosition)
    }

    private fun geoCoding(context: Context, latitude: Double, longitude: Double, onSuccess: (GeocodingResponse) -> Unit, onFailure: (Exception) -> Unit) {
        val geoCoding = MapboxGeocoding.builder()
            .accessToken(context.getString(R.string.mapbox_access_token))
            .query("$longitude,$latitude")
            .build()

        geoCoding.enqueueCall(object: Callback<GeocodingResponse>{
            override fun onResponse(
                call: Call<GeocodingResponse>,
                response: Response<GeocodingResponse>
            ) {
                if (response.isSuccessful) {
                    onSuccess(response.body()!!)
                } else {
                    onFailure(Exception(response.message()))
                }
            }

            override fun onFailure(call: Call<GeocodingResponse>, t: Throwable) {
                onFailure(t as Exception)
            }

        })

    }

    fun getPlaceName(context: Context, latitude: Double, longitude: Double, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        geoCoding(
            context = context,
            latitude = latitude,
            longitude = longitude,
            onSuccess = { geocodingResponse ->
                try {
                    val place = geocodingResponse.features()[0].placeName()!!
                    onSuccess(place)
                } catch (e:Exception) {
                    onFailure(e)
                }
            },
            onFailure = onFailure
        )
    }

}