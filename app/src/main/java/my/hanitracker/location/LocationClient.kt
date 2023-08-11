package my.hanitracker.location

import android.location.Location
import kotlinx.coroutines.flow.Flow

interface LocationClient {

    fun getLocationUpdate(interval: Long) : Flow<Location>

    fun checkGpsProvider(interval: Long)

    class LocationException(message: String) : Exception()

}