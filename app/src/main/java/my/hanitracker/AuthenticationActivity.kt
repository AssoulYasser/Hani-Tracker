package my.hanitracker

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
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
import com.google.android.gms.auth.api.signin.GoogleSignIn
import my.hanitracker.firebase.AuthenticationBusinessLogic
import my.hanitracker.ui.screens.Authentication
import my.hanitracker.ui.theme.CircularProgress
import my.hanitracker.ui.theme.HaniTrackerTheme


class AuthenticationActivity: ComponentActivity(){
    private lateinit var authenticationBusinessLogic : AuthenticationBusinessLogic
    private lateinit var connectivityManager : ConnectivityManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authenticationBusinessLogic = AuthenticationBusinessLogic(this)
        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        setContent {
            val isSigned = remember { mutableStateOf(true) }

            val email = remember { mutableStateOf("") }
            val emailOnError = remember { mutableStateOf(false) }

            val password = remember { mutableStateOf("") }
            val passwordOnError = remember {  mutableStateOf(false) }

            val firstName = remember { mutableStateOf("") }
            val firstNameOnError = remember {  mutableStateOf(false) }

            val lastName = remember { mutableStateOf("") }
            val lastNameOnError = remember {  mutableStateOf(false) }

            val photo = remember { mutableStateOf<Uri?>(null) }

            val isDialogShown = remember { mutableStateOf(false) }
            val isLoading = remember { mutableStateOf(false) }


            fun startLoading() {
                isLoading.value = true
            }

            fun endLoading() {
                isLoading.value = false
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
                        googleOnClick = { authenticationBusinessLogic.googleSignInIntent() },
                        switchAuthOnClick = { isSigned.value = !isSigned.value },
                        emailOnChange = { newValue ->
                            email.value = newValue
                            emailOnError.value = false
                        },
                        emailOnError = emailOnError.value,
                        passwordOnChange = { newValue ->
                            password.value = newValue
                            passwordOnError.value = false
                        },
                        passwordOnError = passwordOnError.value,
                        firstNameOnChange = {newValue ->
                            firstName.value = newValue
                            firstNameOnError.value = false
                        },
                        firstNameOnError = firstNameOnError.value,
                        lastNameOnChange = {newValue ->
                            lastName.value = newValue
                            lastNameOnError.value = false
                        },
                        lastNameOnError = lastNameOnError.value,
                        photoOnChange = {newValue ->
                            photo.value = newValue
                            lastNameOnError.value = false
                        },
                        onRegister = {
                            startLoading()
                            if (connectivityManager.activeNetworkInfo?.isConnectedOrConnecting != true) {
                                AlertDialog.Builder(this)
                                    .setTitle("No network provided")
                                    .setMessage("your device is not connected to any network provider")
                                    .setNegativeButton("CLOSE") { dialogInterface, _ ->
                                        dialogInterface.dismiss()
                                    }
                                    .show()
                                endLoading()
                            }
                            else
                                try {
                                    authenticationBusinessLogic.checkEmailAndPassword(email.value, password.value)

                                    if (isSigned.value)
                                        authenticationBusinessLogic.checkAccountExistence(
                                            email = email.value,
                                            onSuccess = { accountExistence ->
                                                when(accountExistence){
                                                    AuthenticationBusinessLogic.ACCOUNT_RELATED_WITH_GOOGLE_AUTHENTICATOR -> {
                                                        Toast.makeText(this, "This email is used with Google Authenticator", Toast.LENGTH_LONG).show()
                                                        authenticationBusinessLogic.googleSignInIntent()
                                                    }
                                                    AuthenticationBusinessLogic.ACCOUNT_RELATED_WITH_EMAIL_AND_PASSWORD -> {
                                                        authenticationBusinessLogic.signIn(
                                                            email = email.value,
                                                            password = password.value,
                                                            onSuccess = {
                                                                endLoading()
                                                                authenticationSuccess()
                                                            },
                                                            onFailure = {
                                                                endLoading()
                                                                Toast.makeText(this, authenticationBusinessLogic.getExceptionMessage(it), Toast.LENGTH_LONG).show()
                                                            })
                                                    }
                                                    else -> {
                                                        Toast.makeText(this, "Invalid account detected", Toast.LENGTH_LONG).show()
                                                        emailOnError.value = true
                                                    }
                                                }
                                            },
                                            onFailure = {
                                                Toast.makeText(this, authenticationBusinessLogic.getExceptionMessage(it), Toast.LENGTH_LONG).show()
                                                endLoading()
                                            }
                                        )

                                    else
                                        authenticationBusinessLogic.checkAccountExistence(
                                            email.value,
                                            onSuccess = { accountExistence ->
                                                when(accountExistence) {
                                                    AuthenticationBusinessLogic.ACCOUNT_RELATED_WITH_EMAIL_AND_PASSWORD -> {
                                                        Toast.makeText(
                                                            this,"Email has already been used", Toast.LENGTH_LONG).show()
                                                        emailOnError.value = true
                                                        endLoading()
                                                    }

                                                    AuthenticationBusinessLogic.ACCOUNT_RELATED_WITH_GOOGLE_AUTHENTICATOR -> {
                                                        Toast.makeText(this, "This email is used with Google Authenticator", Toast.LENGTH_LONG).show()
                                                        authenticationBusinessLogic.googleSignInIntent()
                                                        emailOnError.value = true
                                                    }

                                                    else -> {
                                                        isDialogShown.value = true
                                                        endLoading()
                                                    }
                                                }

                                            },
                                            onFailure = {
                                                Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
                                            }
                                        )


                                } catch (e : Exception) {
                                    Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
                                    when(e.message){
                                        AuthenticationBusinessLogic.EMAIL_VALIDATION_EXCEPTION_MESSAGE -> emailOnError.value = true
                                        AuthenticationBusinessLogic.PASSWORD_VALIDATION_EXCEPTION_MESSAGE -> passwordOnError.value = true
                                        AuthenticationBusinessLogic.FIRST_NAME_VALIDATION_EXCEPTION_MESSAGE -> firstNameOnError.value = true
                                        AuthenticationBusinessLogic.LAST_NAME_VALIDATION_EXCEPTION_MESSAGE -> lastNameOnError.value = true
                                    }
                                    endLoading()
                                }



                        },
                        onSubmit = {
                            if (connectivityManager.activeNetworkInfo?.isConnectedOrConnecting != true)
                                AlertDialog.Builder(this)
                                    .setTitle("No network provided")
                                    .setMessage("your device is not connected to any network provider")
                                    .setNegativeButton("CLOSE") { dialogInterface, _ ->
                                        dialogInterface.dismiss()
                                    }
                                    .show()
                            else {
                                try {
                                    authenticationBusinessLogic.checkOtherFields(
                                        firstName.value,
                                        lastName.value,
                                        photo.value
                                    )
                                } catch (e: Exception) {
                                    Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
                                }

                                startLoading()
// DATA CLASS USER
                                authenticationBusinessLogic.signUp(
                                    email = email.value,
                                    password = password.value,
                                    firstName = firstName.value,
                                    lastName = lastName.value,
                                    photo = photo.value,
                                    onSuccess = {
                                        authenticationSuccess()
                                        endLoading()
                                    },
                                    onFailure = {
                                        Toast.makeText(
                                            this,
                                            authenticationBusinessLogic.getExceptionMessage(it),
                                            Toast.LENGTH_LONG
                                        ).show()
                                        endLoading()
                                    })
                            }
                        }
                    )
                }
                CircularProgress(isLoading = isLoading)
            }
        }
    }

    private fun authenticationSuccess() {
        Intent(this, MapActivity::class.java).also {
            startActivity(it)
        }
        finish()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AuthenticationBusinessLogic.GOOGLE_SIGN_IN_REQUEST_CODE){
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            Log.d(AuthenticationBusinessLogic.TAG, "onActivityResult: $task")
            try {
                authenticationBusinessLogic.googleAuthenticatorResult(task,
                    onSuccess = {
                        authenticationSuccess()
                    },
                    onFailure = {
                        Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
                    })
            } catch (e: Exception) {
                Toast.makeText(this, authenticationBusinessLogic.getExceptionMessage(e), Toast.LENGTH_LONG).show()
            }
        }
    }

}
