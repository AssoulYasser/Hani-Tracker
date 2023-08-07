package my.hanitracker.map

import android.graphics.Bitmap
import com.mapbox.geojson.Point
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.annotation.AnnotationPlugin
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager

class MapBoxManager(mapView: MapView) {
    private var annotationApi : AnnotationPlugin
    private var pointAnnotationManager : PointAnnotationManager

    init {
        annotationApi = mapView.annotations
        pointAnnotationManager = annotationApi.createPointAnnotationManager(mapView)
    }

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

}