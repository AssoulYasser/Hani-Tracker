package my.hanitracker.firebase

import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


object FirebaseUsers {
    private val firebaseAuth = Firebase.auth
    private const val GOOGLE_PROVIDER = "google.com"
    private const val EMAIL_PROVIDER = "password"

    private fun getProviderType(uid: String, onSuccess: (String) -> Unit) {
    }

    fun getUserData(uid: String, onSuccess: (User) -> Unit, onFailure: (EnumConstantNotPresentException) -> Unit){
        getProviderType(
            uid,
            onSuccess = { providerType ->
                when(providerType) {
                    GOOGLE_PROVIDER -> {

                    }
                    EMAIL_PROVIDER -> {

                    }
                }
            }
        )
    }

}