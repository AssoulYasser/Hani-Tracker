package my.hanitracker.firebase

import android.app.Activity
import android.content.Intent
import android.net.Uri
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import my.hanitracker.R
import my.hanitracker.manager.UserLocalStorage

class AuthenticationBusinessLogic(private val activity: Activity) {

    private val firebaseAuthentication = FirebaseAuthentication()
    private val firebaseFireStore = FirebaseFireStore()
    private val firebaseCloudStore = FirebaseCloudStore()
    private val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(activity.getString(R.string.google_client_id))
        .requestEmail()
        .build()
    private val googleSignInClient = GoogleSignIn.getClient(activity, googleSignInOptions)

    companion object {
        const val EMAIL_VALIDATION_EXCEPTION_MESSAGE = "Email not valid"
        const val PASSWORD_VALIDATION_EXCEPTION_MESSAGE = "Password not valid"
        const val FIRST_NAME_VALIDATION_EXCEPTION_MESSAGE = "First name not valid"
        const val LAST_NAME_VALIDATION_EXCEPTION_MESSAGE = "Last name not valid"
        const val PHOTO_VALIDATION_EXCEPTION_MESSAGE = "Photo can not be null"

        const val GOOGLE_SIGN_IN_REQUEST_CODE = 100
        const val TAG = "DEBUGGING : "

        const val ACCOUNT_RELATED_WITH_EMAIL_AND_PASSWORD = 1
        const val ACCOUNT_RELATED_WITH_GOOGLE_AUTHENTICATOR = -1
        const val ACCOUNT_NOT_RELATED = 0

    }

    private fun checkEmailValidation(email: String) : Boolean =
        email.matches(regex = Regex("[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}\\@[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}(\\.[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25})+"))
    private fun checkPasswordValidation(password: String) : Boolean = password.length in 8..16
    private fun checkFirstNameValidation(firstName: String): Boolean = firstName.length in 4..16
    private fun checkLastNameValidation(lastName: String): Boolean = lastName.length in 4..16

    private fun setEmail(email: String){
        if(!checkEmailValidation(email))
            throw Exception(EMAIL_VALIDATION_EXCEPTION_MESSAGE)
    }
    private fun setPassword(password: String){
        if (!checkPasswordValidation(password))
            throw Exception(PASSWORD_VALIDATION_EXCEPTION_MESSAGE)
    }
    private fun setFirstName(firstName: String){
        if (!checkFirstNameValidation(firstName))
            throw Exception(FIRST_NAME_VALIDATION_EXCEPTION_MESSAGE)
    }
    private fun setLastName(lastName: String){
        if (!checkLastNameValidation(lastName))
            throw Exception(LAST_NAME_VALIDATION_EXCEPTION_MESSAGE)
    }
    private fun setPhoto(photo: Uri?){
        if (photo == null)
            throw Exception(PHOTO_VALIDATION_EXCEPTION_MESSAGE)
    }

    fun checkEmailAndPassword(email: String, password: String){
        setEmail(email)
        setPassword(password)
    }

    fun checkAccountExistence(email: String, onSuccess: (Int) -> Unit, onFailure: (Exception) -> Unit){
        firebaseAuthentication.checkEmailExistence(email){ emailExistence ->
            if (emailExistence)
                onSuccess(ACCOUNT_RELATED_WITH_EMAIL_AND_PASSWORD)
            else{
                firebaseFireStore.ifDocumentExists(
                    "user",
                    email,
                    onSuccess = {
                        if (it)
                            onSuccess(ACCOUNT_RELATED_WITH_GOOGLE_AUTHENTICATOR)
                        else
                            onSuccess(ACCOUNT_NOT_RELATED)
                    },
                    onFailure = onFailure
                )
            }
        }
    }

    fun checkOtherFields(firstName: String, lastName: String, photo: Uri?){
        setFirstName(firstName)
        setLastName(lastName)
        setPhoto(photo)
    }

    fun signIn(email: String, password: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        firebaseAuthentication.accessAccount(
            email = email,
            password = password,
            onSuccessCallBack = { authResult ->
                downloadUserData(uid = authResult.user!!.uid, email = email, onSuccess = onSuccess, onFailure = onFailure)
            },
            onFailureCallBack = onFailure
        )
    }

    fun signUp(email: String, password: String, firstName: String, lastName: String, photo: Uri?, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        firebaseAuthentication.createAccount(
            email,
            password,
            onSuccessCallBack = { authResult ->
                uploadUserFiles(
                    email = email,
                    photo = photo!!,
                    onSuccess = { photoUri ->
                        uploadUserData(
                            uid = authResult.user!!.uid,
                            email = email,
                            firstName = firstName,
                            lastName = lastName,
                            pfpUri = photoUri!!,
                            onSuccess = {
                                setUserLocalStorage(authResult.user!!.uid, firstName, lastName, email, photoUri)
                                onSuccess()
                            },
                            onFailure = { exception ->
                                onFailure(exception)
                            }
                        )
                    },
                    onFailure = { exception ->
                        onFailure(exception)
                    }
                )
            },
            onFailureCallBack = { exception ->
                onFailure(exception)
            }
        )
    }

    fun googleSignInIntent() {
        val signInIntent: Intent = googleSignInClient.signInIntent
        activity.startActivityForResult(signInIntent, GOOGLE_SIGN_IN_REQUEST_CODE)
    }

    fun googleAuthenticatorResult(task: Task<GoogleSignInAccount>, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        if(task.isSuccessful) {
            val googleAccount = task.getResult(ApiException::class.java)
            checkAccountExistence(
                googleAccount.email!!,
                onSuccess = { accountExistence ->
                    if (accountExistence == ACCOUNT_RELATED_WITH_GOOGLE_AUTHENTICATOR || accountExistence == ACCOUNT_RELATED_WITH_EMAIL_AND_PASSWORD)
                        downloadUserData(uid = googleAccount.id!!, email = googleAccount.email!!, onSuccess = onSuccess, onFailure = onFailure)

                    else
                        uploadUserData(
                            uid = googleAccount.id!!,
                            email = googleAccount.email!!,
                            firstName = googleAccount.givenName!!,
                            lastName = googleAccount.familyName!!,
                            pfpUri = googleAccount.photoUrl!!,
                            onSuccess = onSuccess,
                            onFailure = onFailure,
                        )

                },
                onFailure = onFailure
            )
        } else {
            throw Exception(task.exception)
        }
    }

    private fun downloadUserData(uid: String, email: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit){
        firebaseFireStore.getData(
            collection = "user",
            document = email,
            onSuccess = { documentSnapshot ->
                setUserLocalStorage(
                    uid = uid,
                    firstName = documentSnapshot["first name"] as String,
                    lastName = documentSnapshot["last name"] as String,
                    email = email,
                    uri = Uri.parse(documentSnapshot["profile picture uri"] as String)
                )
                onSuccess()
            },
            onFailure = onFailure)
    }

    private fun setUserLocalStorage(
        uid: String,
        firstName: String,
        lastName: String,
        email: String,
        uri: Uri?
    ) {
        UserLocalStorage.setUser(
            userId = uid,
            firstName = firstName,
            lastName = lastName,
            email = email,
            photo = uri!!
        )
    }

    private fun uploadUserData(uid: String, email: String, firstName: String, lastName: String, pfpUri: Uri,onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        firebaseFireStore.storeData(
            data = hashMapOf("first name" to firstName, "last name" to lastName, "uid" to uid,"profile picture uri" to pfpUri),
            collection = "user",
            document = email,
            onSuccess = {
                setUserLocalStorage(
                    uid = uid,
                    firstName = firstName,
                    lastName = lastName,
                    email = email,
                    uri = pfpUri
                )
                onSuccess()
            },
            onFailure = onFailure,
        )
    }

    private fun uploadUserFiles(email: String, photo: Uri, onSuccess: (Uri?) -> Unit, onFailure: (Exception) -> Unit){
        firebaseCloudStore.uploadFile(
            fileToUpload = photo,
            location = "user/$email/pfp",
            onSuccess = onSuccess,
            onFailure = onFailure
        )
    }

    private fun deleteAccount(email: String){

    }

    private fun deleteAccountData(email: String){

    }

    fun getExceptionMessage(exception: Exception) : String = firebaseAuthentication.exceptionMessage(exception)

}