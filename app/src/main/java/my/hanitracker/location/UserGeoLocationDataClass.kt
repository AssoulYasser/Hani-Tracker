package my.hanitracker.location

import my.hanitracker.user.UserDataClass

data class UserGeoLocationDataClass(val user: UserDataClass, var latitude: Double, var longitude: Double)
