package my.hanitracker.manager

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
        this.userId = userId
        this.firstName = firstName
        this.lastName = lastName
        this.email = email
        this.photo = photo
    }

}