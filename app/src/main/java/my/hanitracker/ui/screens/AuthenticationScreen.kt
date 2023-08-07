package my.hanitracker.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import my.hanitracker.R
import my.hanitracker.ui.theme.Logo
import my.hanitracker.ui.theme.Merriweather
import my.hanitracker.ui.theme.SocialMediaAuthButton
import my.hanitracker.ui.theme.UnderlinedTextField
import my.hanitracker.ui.theme.rememberImeState


@Composable
fun Authentication(
    isSigned: Boolean = false,
    isDialogShown: MutableState<Boolean>,
    googleOnClick: () -> Unit,
    switchAuthOnClick: () -> Unit,
    emailOnChange: (String) -> Unit,
    emailOnError: Boolean = false,
    passwordOnChange: (String) -> Unit,
    passwordOnError: Boolean = false,
    firstNameOnChange: (String) -> Unit,
    firstNameOnError: Boolean = false,
    lastNameOnChange: (String) -> Unit,
    lastNameOnError: Boolean = false,
    photoOnChange: (Uri?) -> Unit,
    onRegister: () -> Unit,
    onSubmit: () -> Unit,
) {

    val context = LocalContext.current
    val isInInputMode = rememberImeState()
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp
    val titlePosition = screenHeight * 0.45f - 100f
    var titlePositionState by remember{ mutableStateOf(titlePosition) }
    titlePositionState = if (isInInputMode.value) 0f else titlePosition

    val titleAnimation = animateFloatAsState(targetValue = titlePositionState, animationSpec = tween(80))


    Dialog(
        isDialogShown = isDialogShown,
        firstNameOnChange = firstNameOnChange,
        firstNameOnError = firstNameOnError,
        lastNameOnChange = lastNameOnChange,
        lastNameOnError = lastNameOnError,
        photoOnChange = photoOnChange,
        onSubmit = onSubmit
    )

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {


        Box(
            contentAlignment = Alignment.TopCenter,
            modifier = Modifier
                .align(alignment = Alignment.TopCenter)
                .padding(bottom = 10.dp)
        ) {

            AnimatedVisibility(
                visible = !isInInputMode.value,
                enter = slideInVertically{ -1500 },
                exit = slideOutVertically { -1500 }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.auth_background),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(fraction = 0.45f),
                    contentScale = ContentScale.FillBounds
                )

                Logo(modifier = Modifier
                    .align(alignment = Alignment.TopCenter)
                    .padding(80.dp))

            }

            Text(
                modifier = Modifier
                    .padding(10.dp)
                    .align(alignment = Alignment.TopCenter)
                    .padding(top = titleAnimation.value.dp),
                text = if(isSigned) "Login" else "Register",
                fontFamily = Merriweather,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )

        }


        Column(
            modifier = Modifier
                .align(alignment = Alignment.BottomCenter)
                .padding(horizontal = 30.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            UnderlinedTextField(label = "Email", onValueChange = { newValue -> emailOnChange(newValue)}, isError = emailOnError)

            Spacer(modifier = Modifier.height(20.dp))

            UnderlinedTextField(label = "Password", password = true, isError = passwordOnError, trailing = if (isSigned) {
                {
                    TextButton(onClick = {
                        Toast.makeText(
                            context,
                            "We did not work on that yet",
                            Toast.LENGTH_LONG
                        ).show()
                    }) {
                        Text(
                            text = "Forget?",
                            color = Color(0xFF0466c8)
                        )
                    }
                }
            } else {
                {}
            }, onValueChange = { newValue -> passwordOnChange(newValue)})

            Spacer(modifier = Modifier.height(20.dp))

            Button(onClick = onRegister, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(4.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF000113))) {
                Text(text = if(isSigned) "Login" else "Register", modifier = Modifier.padding(vertical = 5.dp), color = Color.White, style = MaterialTheme.typography.labelLarge)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(text = "Or continue with", style = MaterialTheme.typography.labelSmall, color = Color(0xFF64748B))

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                SocialMediaAuthButton(
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .weight(1f),
                    rowModifier = Modifier.padding(vertical = 5.dp),
                    space = 10.dp,
                    icon = R.drawable.google_icon,
                    text = "Google",
                    onClick = googleOnClick
                )

            }

            Spacer(modifier = Modifier.height(30.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isSigned) {
                    Text(
                        text = "You don't have an account?",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF64748B)
                    )
                    TextButton(
                        onClick = switchAuthOnClick,
                        colors = ButtonDefaults.buttonColors(contentColor = Color(0xFF0466c8), containerColor = Color.Transparent)
                    ) {
                        Text(text = "Create now", fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = Merriweather)
                    }
                }
                else{
                    Text(
                        text = "You have an account?",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF64748B)
                    )
                    TextButton(
                        onClick = switchAuthOnClick,
                        colors = ButtonDefaults.buttonColors(contentColor = Color(0xFF0466c8), containerColor = Color.Transparent)
                    ) {
                        Text(text = "Login now", fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = Merriweather)
                    }
                }

            }


        }

    }

}



@Composable
private fun Dialog(
    isDialogShown: MutableState<Boolean>,
    firstNameOnChange: (String) -> Unit,
    firstNameOnError: Boolean = false,
    lastNameOnChange: (String) -> Unit,
    lastNameOnError: Boolean = false,
    photoOnChange: (Uri?) -> Unit,
    onSubmit: () -> Unit
) {
    val photoState = remember {
        mutableStateOf<Uri?>(null)
    }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                photoState.value = uri
                photoOnChange(uri)
            }
        }
    )

    if (isDialogShown.value)
        androidx.compose.ui.window.Dialog(onDismissRequest = { isDialogShown.value = !isDialogShown.value }) {
            Box(
                modifier = Modifier
                    .fillMaxHeight(0.96f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .background(
                            Color(0xFF0466c8)
                        )
                ) {
                    Text(
                        text = "Additional Data",
                        textAlign = TextAlign.Center,
                        fontFamily = Merriweather,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier
                            .padding(vertical = 20.dp)
                    )
                }

                Column(
                    modifier = Modifier
                        .padding(horizontal = 15.dp, vertical = 30.dp)
                        .align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    UnderlinedTextField(
                        label = "First Name",
                        isError = firstNameOnError,
                        onValueChange = { newValue -> firstNameOnChange(newValue) })
                    Spacer(modifier = Modifier.height(25.dp))
                    UnderlinedTextField(
                        label = "Last Name",
                        isError = lastNameOnError,
                        onValueChange = { newValue -> lastNameOnChange(newValue) })
                    Spacer(modifier = Modifier.height(25.dp))
                    AsyncImage(
                        model = photoState.value,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop,
                    )
                    Spacer(modifier = Modifier.height(25.dp))
                    Button(
                        modifier = Modifier,
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF000113)),
                        onClick = {
                            photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }) {
                        Text(text = if (photoState.value == null) "Pick Photo" else "Change Photo")
                    }

                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Button(
                        onClick = {
                            isDialogShown.value = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        modifier = Modifier
                            .weight(1f)
                            .background(Color(0xFF000000))
                            .height(IntrinsicSize.Max),
                        shape = RectangleShape
                    ) {
                        Text(text = "Niggative", color = Color.White)
                    }
                    Button(
                        onClick = onSubmit,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        modifier = Modifier
                            .weight(1f)
                            .background(Color(0xFF0466c8))
                            .height(IntrinsicSize.Max),
                        shape = RectangleShape
                    ) {
                        Text(text = "Affirmative", color = Color.White)
                    }
                }

            }
        }
}

