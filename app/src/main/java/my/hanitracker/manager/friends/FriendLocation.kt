package my.hanitracker.manager.friends

import android.net.Uri

data class FriendLocation (val uid: String, val fullName: String, val photo: Uri, val latitude: Double, val longitude: Double)