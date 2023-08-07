package my.hanitracker.user

import android.net.Uri

data class UserDataClass(val uid: String, val firstName: String, val lastName: String, val email: String, val pfp: Uri)
