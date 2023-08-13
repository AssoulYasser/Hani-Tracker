package my.hanitracker.location

import my.hanitracker.user.UserDataClass

data class UserPlaceNameLocationDataClass(val user: UserDataClass, val placeName: String, val latitude: Double, val longitude: Double)
