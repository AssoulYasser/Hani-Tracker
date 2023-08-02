package my.hanitracker

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.content.edit
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import my.hanitracker.firebase.FirebaseAuthentication
import my.hanitracker.firebase.FirebaseCloudStore
import my.hanitracker.firebase.FirebaseFireStore
import my.hanitracker.manager.UserLocalStorage
import my.hanitracker.ui.screens.Authentication
import my.hanitracker.ui.theme.CircularProgress
import my.hanitracker.ui.theme.HaniTrackerTheme
import java.lang.NullPointerException


class AuthenticationActivity: ComponentActivity(){
    private lateinit var googleSignInOptions: GoogleSignInOptions
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var sharedPreferences : SharedPreferences

    companion object {
        private const val GOOGLE_SIGN_IN_REQUEST_CODE = 100
        private const val TAG = "DEBUGGING : "
    }




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initInstences()
        setContent {
            val isSigned = remember { mutableStateOf(true) }
            val emailOnError = remember { mutableStateOf(false) }
            val passwordOnError = remember {  mutableStateOf(false) }
            val firstNameOnError = remember {  mutableStateOf(false) }
            val lastNameOnError = remember {  mutableStateOf(false) }
            val isDialogShown = remember { mutableStateOf(false) }
            val isLoading = remember { mutableStateOf(false) }


            fun startLoading() {
                isLoading.value = true
            }

            fun endLoading() {
                isLoading.value = false
            }

            if(FirebaseAuthentication.hasSignedIn(this)) {
                Toast.makeText(this, "LAST SIGN IN", Toast.LENGTH_LONG).show()
            }

            HaniTrackerTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Authentication(
                        isSigned = isSigned.value,
                        isDialogShown = isDialogShown,
                        googleOnClick = { googleAuthentication() },
                        switchAuthOnClick = { isSigned.value = !isSigned.value },
                        emailOnChange = { newValue ->
                            FirebaseAuthentication.EmailAuthenticator.setEmail(newValue)
                            emailOnError.value = false
                        },
                        emailOnError = emailOnError.value,
                        passwordOnChange = { newValue ->
                            FirebaseAuthentication.EmailAuthenticator.setPassword(newValue)
                            passwordOnError.value = false
                        },
                        passwordOnError = passwordOnError.value,
                        firstNameOnChange = {newValue ->
                            FirebaseAuthentication.EmailAuthenticator.setFirstName(newValue)
                            firstNameOnError.value = false
                        },
                        firstNameOnError = firstNameOnError.value,
                        lastNameOnChange = {newValue ->
                            FirebaseAuthentication.EmailAuthenticator.setLastName(newValue)
                            lastNameOnError.value = false
                        },
                        lastNameOnError = lastNameOnError.value,
                        photoOnChange = {newValue ->
                            FirebaseAuthentication.EmailAuthenticator.setPhoto(newValue)
                            lastNameOnError.value = false
                        },
                        onRegister = {
                            if (!FirebaseAuthentication.EmailAuthenticator.checkEmailValidation()) {
                                Toast.makeText(this, "Email Not Valid", Toast.LENGTH_SHORT).show()
                                emailOnError.value = true
                                return@Authentication
                            }

                            if (!FirebaseAuthentication.EmailAuthenticator.checkPasswordValidation()){
                                Toast.makeText(this, "Password Not Valid", Toast.LENGTH_SHORT).show()
                                passwordOnError.value = true
                                return@Authentication
                            }

                            if (isSigned.value) {
                                startLoading()
                                signIn {
                                    endLoading()
                                }
                            }
                            else
                                try {
                                    startLoading()
                                    FirebaseAuthentication.EmailAuthenticator.checkEmailExistence {doesExists ->
                                        endLoading()
                                        if (doesExists)
                                            Toast.makeText(this, "Email Already Exists", Toast.LENGTH_SHORT).show()
                                        else
                                            isDialogShown.value = true
                                    }
                                } catch (e:Exception){
                                    Toast.makeText(this, "Error : ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        },
                        onSubmit = {

                            if (!FirebaseAuthentication.EmailAuthenticator.checkFirstNameValidation()){
                                Toast.makeText(this, "First Name Not Valid", Toast.LENGTH_SHORT).show()
                                firstNameOnError.value = true
                                return@Authentication
                            }
                            if (!FirebaseAuthentication.EmailAuthenticator.checkLastNameValidation()){
                                Toast.makeText(this, "Last Name Not Valid", Toast.LENGTH_SHORT).show()
                                lastNameOnError.value = true
                                return@Authentication
                            }
                            if (!FirebaseAuthentication.EmailAuthenticator.checkPhotoValidation()){
                                Toast.makeText(this, "Photo must be provided", Toast.LENGTH_SHORT).show()
                                return@Authentication
                            }

                            FirebaseAuthentication.EmailAuthenticator.also {
                                startLoading()
                                it.createAccount(
                                    onSuccessCallBack = { authResult ->
                                        val id = authResult.user!!.uid
                                        it.setAccountData(
                                            id = id,
                                            onSuccessCallBack = {
                                                it.setAccountFiles(
                                                    id = id,
                                                    onSuccessCallBack = { uri ->
                                                        UserLocalStorage.setUser(
                                                            userId = id,
                                                            firstName = it.firstName,
                                                            lastName = it.lastName,
                                                            email = it.email,
                                                            photo = uri!!
                                                        )
                                                        sharedPreferences.edit(){
                                                            putString("token", it.getAccountToken())
                                                            apply()
                                                        }
                                                        startActivity(Intent(this@AuthenticationActivity, MainActivity::class.java))
                                                        endLoading()
                                                    },
                                                    onFailureCallBack = { exception ->
                                                        endLoading()
                                                        FirebaseFireStore.deleteData("user", id, {}, {})
                                                    }
                                                )
                                            },
                                            onFailureCallBack = { exception ->
                                                endLoading()
                                                FirebaseAuthentication.delete({}, {})
                                            }
                                        )
                                        isDialogShown.value = false
                                    },
                                    onFailureCallBack = {
                                        exception ->
                                        endLoading()
                                        Toast.makeText(
                                            this,
                                            FirebaseAuthentication.exceptionMessage(exception),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                )
                            }

                        }
                    )
                }
                CircularProgress(isLoading = isLoading)
            }
        }
    }

    private fun signIn(onComplete : () -> Unit) {
        FirebaseAuthentication.EmailAuthenticator.accessAccount({ authResult ->

            if (authResult.user == null)
                return@accessAccount
            val currentUser = authResult.user

            FirebaseFireStore.getData(
                collection = "user",
                document = currentUser!!.uid,
                onSuccess = { doc ->
                    val firstName = doc["first name"] 
                    val lastName = doc["last name"]
                    FirebaseCloudStore.getFileUri(
                        location = "user/${currentUser.uid}/pfp",
                        onSuccess = { uri ->
                            try {
                                UserLocalStorage.setUser(
                                    currentUser.uid,
                                    firstName = firstName.toString(),
                                    lastName = lastName.toString(),
                                    email = currentUser.email!!,
                                    photo = uri!!
                                )
                                startActivity(Intent(this, MainActivity::class.java))
                            } catch (e: NullPointerException){
                                Toast.makeText(this, "Pfp is not set up", Toast.LENGTH_SHORT).show()
                            }
                            sharedPreferences.edit(){
                                putString("token", FirebaseAuthentication.EmailAuthenticator.getAccountToken())
                                apply()
                            }
                            onComplete()
                            finish()

                        },
                        onFailure = {

                        }
                    )
                },
                onFailure = {
                    Log.e(TAG, "signIn: ${it.message}" )
                    onComplete()
                },
            )
        }, {
            Toast.makeText(this, FirebaseAuthentication.exceptionMessage(it), Toast.LENGTH_SHORT).show()
            onComplete()
        })
    }



    private fun initInstences() {
        googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.google_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)
        sharedPreferences = getSharedPreferences(getString(R.string.user_shared_preferences), Context.MODE_PRIVATE)
    }


    private fun googleAuthentication(){
        val signInIntent: Intent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, GOOGLE_SIGN_IN_REQUEST_CODE)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GOOGLE_SIGN_IN_REQUEST_CODE){
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            if(task.isSuccessful) {
                val googleAccount = task.getResult(ApiException::class.java)
                UserLocalStorage.setUser(
                    userId = googleAccount.id!!,
                    firstName = googleAccount.givenName!!,
                    lastName = googleAccount.familyName!!,
                    email = googleAccount.email!!,
                    photo = googleAccount.photoUrl!!
                )
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                Toast.makeText(this, "TASK UNSC", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
