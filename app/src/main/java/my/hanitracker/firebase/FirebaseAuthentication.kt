package my.hanitracker.firebase

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.android.gms.auth.GoogleAuthException
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthEmailException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import my.hanitracker.firebase.FirebaseAuthentication.EmailAuthenticator.emailLastSignIn
import my.hanitracker.firebase.FirebaseAuthentication.GoogleAuthenticator.googleLastSignIn

object FirebaseAuthentication {
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    fun hasSignedIn(token: String?) : Boolean {
        return if (token == null)
            googleLastSignIn()
        else
            googleLastSignIn() || emailLastSignIn(token)
    }

    fun signOut(context: Context, onCompleteListener: OnCompleteListener<Void>){
        firebaseAuth.signOut()
        GoogleSignIn.getClient(context, GoogleSignInOptions.DEFAULT_SIGN_IN).signOut().addOnCompleteListener (onCompleteListener)
    }

    fun delete(onSuccessCallBack : () -> Unit, onFailureCallBack: (Exception) -> Unit) {
        firebaseAuth.currentUser?.delete()?.addOnSuccessListener{ onSuccessCallBack() }?.addOnFailureListener(onFailureCallBack)
    }


    object GoogleAuthenticator {

        fun googleLastSignIn(): Boolean {
            return false
        }

    }

    object EmailAuthenticator {
        lateinit var email : String
            private set
        private lateinit var password : String
        lateinit var firstName: String
            private set
        lateinit var lastName: String
            private set
        private lateinit var photo: Uri

        fun setEmail(email: String){
            this.email = email
        }

        fun setPassword(password: String){
            this.password = password
        }

        fun setFirstName(firstName: String){
            this.firstName = firstName
        }

        fun setLastName(lastName: String){
            this.lastName = lastName
        }

        fun setPhoto(photo: Uri?){
            if (photo != null)
                this.photo = photo
        }

        fun emailLastSignIn(token: String) : Boolean {
            var isValidToken = false
            firebaseAuth.signInWithCustomToken(token).addOnSuccessListener { isValidToken = true }
            return isValidToken
        }

        fun checkEmailValidation() : Boolean {
            if (!::email.isInitialized)
                return false

            val emailRegex = Regex("^[a-zA-Z0-9.!#\$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*\$")

            if (email.matches(regex = emailRegex))
                return true

            return false

        }

        fun checkPasswordValidation() : Boolean {
            if (!::password.isInitialized)
                return false

            if (password.length in 8..16)
                return true

            return false
        }

        fun checkFirstNameValidation(): Boolean {
            if (!::firstName.isInitialized)
                return false
            if (firstName.length !in 4..16)
                return false
            return true
        }

        fun checkLastNameValidation(): Boolean {
            if (!::lastName.isInitialized)
                return false
            if (lastName.length !in 4..16)
                return false
            return true
        }

        fun checkPhotoValidation(): Boolean {
            if (::photo.isInitialized)
                return true
            return false
        }

        fun checkEmailExistence(callback: (Boolean) -> Unit) {
            firebaseAuth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val signInMethods = task.result?.signInMethods
                        val emailExists = !signInMethods.isNullOrEmpty()
                        callback(emailExists)
                    } else {
                        val exception = task.exception
                        throw Throwable(exception)
                    }
                }
        }

        fun createAccount(onSuccessCallBack : (AuthResult) -> Unit, onFailureCallBack: (Exception) -> Unit) {
            firebaseAuth.createUserWithEmailAndPassword(email, password).addOnSuccessListener(onSuccessCallBack).addOnFailureListener(onFailureCallBack)
        }

        fun setAccountData(
            id: String,
            onSuccessCallBack: () -> Unit,
            onFailureCallBack: (Exception) -> Unit
        ) {
            FirebaseFireStore.storeData(
                hashMapOf(
                    "first name" to firstName,
                    "last name" to lastName
                ),
                collection = "user",
                document = id,
                onSuccess = {
                    onSuccessCallBack.invoke()
                },
                onFailure = {
                    onFailureCallBack.invoke(it)
                }
            )
        }

        fun getAccountToken() : String? = firebaseAuth.currentUser?.getIdToken(false)?.result?.token

        fun setAccountFiles(id: String, onSuccessCallBack : (Uri?) -> Unit, onFailureCallBack: (Exception) -> Unit){
            FirebaseCloudStore.uploadFile(photo, "user/$id/pfp", onSuccessCallBack, onFailureCallBack)
        }

        fun accessAccount(onSuccessCallBack : (AuthResult) -> Unit, onFailureCallBack: (Exception) -> Unit) {
            firebaseAuth.signInWithEmailAndPassword(email, password).addOnSuccessListener(onSuccessCallBack).addOnFailureListener(onFailureCallBack)
        }

    }

    fun exceptionMessage(exception: Exception?): String {
        return try {
            throw exception!!
        } catch (e: FirebaseAuthWeakPasswordException) {
            "WEAK PASSWORD DETECTED"
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            "INVALID CREDENTIALS DETECTED"
        } catch (e: FirebaseAuthUserCollisionException) {
            "USED EMAIL DETECTED"
        } catch (e: FirebaseAuthEmailException) {
            "VERIFICATION ERROR DETECTED"
        } catch (e: FirebaseNetworkException) {
            "NETWORK ERROR DETECTED"
        } catch (e: FirebaseAuthInvalidUserException) {
            "INVALID USER DETECTED"
        } catch (e: ApiException) {
            "API ERROR DETECTED"
        } catch (e: GoogleAuthException) {
            "ERROR IN GOOGLE ACCOUNT DETECTED"
        } catch (e: GooglePlayServicesNotAvailableException) {
            "GOOGLE SERVICE ERROR DETECTED"
        } catch (e: GooglePlayServicesRepairableException) {
            "OUTDATED GOOGLE PLAY SERVICE DETECTED"
        } catch (e: FirebaseAuthException) {
            "UNKNOWN ERROR DETECTED"
        }
    }
}