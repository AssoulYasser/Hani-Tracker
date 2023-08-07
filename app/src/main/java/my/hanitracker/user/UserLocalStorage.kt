package my.hanitracker.user

import android.net.Uri


object UserLocalStorage {
    lateinit var userId: String
        private set
    lateinit var firstName: String
        private set
    lateinit var lastName: String
        private set
    lateinit var email: String
        private set
    lateinit var photo: Uri
        private set

    fun setUser(userId: String, firstName: String, lastName: String, email: String, photo: Uri){
        UserLocalStorage.userId = userId
        UserLocalStorage.firstName = firstName
        UserLocalStorage.lastName = lastName
        UserLocalStorage.email = email
        UserLocalStorage.photo = photo
    }

}