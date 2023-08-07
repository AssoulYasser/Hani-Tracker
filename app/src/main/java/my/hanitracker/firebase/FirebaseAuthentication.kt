package my.hanitracker.firebase

import android.content.Context
import android.net.Uri
import com.google.android.gms.auth.GoogleAuthException
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthEmailException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class FirebaseAuthentication {
    private val firebaseAuth: FirebaseAuth = Firebase.auth

    fun signOut(context: Context, onCompleteListener: OnCompleteListener<Void>) {
        firebaseAuth.signOut()
        GoogleSignIn.getClient(context, GoogleSignInOptions.DEFAULT_SIGN_IN).signOut()
            .addOnCompleteListener(onCompleteListener)
    }

    fun deleteCurrentAccount(
        onSuccessCallBack: () -> Unit,
        onFailureCallBack: (Exception) -> Unit
    ) {
        firebaseAuth.currentUser?.delete()?.addOnSuccessListener { onSuccessCallBack() }
            ?.addOnFailureListener(onFailureCallBack)
    }

    private fun googleLastSignIn(context: Context): Boolean {
        return GoogleSignIn.getLastSignedInAccount(context) != null
    }

    fun checkEmailExistence(email: String, callback: (Boolean) -> Unit) {
        firebaseAuth.fetchSignInMethodsForEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val signInMethods = task.result?.signInMethods
                    val emailExists = !signInMethods.isNullOrEmpty()
                    callback(emailExists)
                } else {
                    val exception = task.exception
                    throw Exception(exception)
                }
            }
    }

    fun createAccount(
        email: String,
        password: String,
        onSuccessCallBack: (AuthResult) -> Unit,
        onFailureCallBack: (Exception) -> Unit
    ) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener(onSuccessCallBack).addOnFailureListener(onFailureCallBack)
    }


    fun accessAccount(
        email: String,
        password: String,
        onSuccessCallBack: (AuthResult) -> Unit,
        onFailureCallBack: (Exception) -> Unit
    ) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener(onSuccessCallBack).addOnFailureListener(onFailureCallBack)
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
