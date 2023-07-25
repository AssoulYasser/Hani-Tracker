package my.hanitracker.manager.location

import android.location.Location
import kotlinx.coroutines.flow.Flow

interface LocationClient {

    fun getLocationUpdate(interval: Long) : Flow<Location>

    fun checkHardwareAvailability(interval: Long)

    class LocationException(message: String) : Exception()

}