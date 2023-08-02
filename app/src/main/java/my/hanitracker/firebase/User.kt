package my.hanitracker.firebase

import android.net.Uri

data class User(
    val uid: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val photo: Uri
)
