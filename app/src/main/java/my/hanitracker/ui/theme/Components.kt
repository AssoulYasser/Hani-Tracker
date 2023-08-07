package my.hanitracker.ui.theme

import android.net.Uri
import android.view.ViewTreeObserver
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import coil.compose.rememberImagePainter
import coil.transform.CircleCropTransformation
import my.hanitracker.R

@Composable
fun Logo(modifier: Modifier = Modifier) {
    Image(painter = painterResource(id = R.drawable.logo), contentDescription = null, modifier = modifier)
}

@Composable
fun rememberImeState(): State<Boolean> {
    val imeState = remember {
        mutableStateOf(false)
    }

    val view = LocalView.current
    DisposableEffect(view) {
        val listener = ViewTreeObserver.OnGlobalLayoutListener {
            val isKeyboardOpen = ViewCompat.getRootWindowInsets(view)
                ?.isVisible(WindowInsetsCompat.Type.ime()) ?: true
            imeState.value = isKeyboardOpen
        }

        view.viewTreeObserver.addOnGlobalLayoutListener(listener)
        onDispose {
            view.viewTreeObserver.removeOnGlobalLayoutListener(listener)
        }
    }
    return imeState
}

@Composable
fun SocialMediaAuthButton(
    modifier: Modifier = Modifier,
    rowModifier: Modifier = Modifier,
    space: Dp = 0.dp,
    icon: Int,
    text: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RectangleShape,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9)),
        elevation = ButtonDefaults.buttonElevation(0.dp)
    ) {
        Row(modifier = rowModifier, verticalAlignment = Alignment.CenterVertically) {
            Image(painter = painterResource(id = icon), contentDescription = null)
            Spacer(modifier = Modifier.width(space))
            Text(text = text, color = Color(0xFF475569), style = MaterialTheme.typography.labelLarge)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnderlinedTextField(
    label:String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    password: Boolean = false,
    isError: Boolean = false,
    trailing: @Composable () -> Unit = {},
    onValueChange: (String) -> Unit
) {
    var value by remember {
        mutableStateOf("")
    }

    TextField(
        value = value,
        onValueChange = {
            value = it
            onValueChange(it)
        },
        modifier = modifier,
        enabled = enabled,
        label = { Text(text = label) },
        singleLine = true,
        colors = TextFieldDefaults.textFieldColors(
            containerColor = Color.Transparent,
            focusedLabelColor = Color(0xFF0466c8),
            focusedIndicatorColor = Color(0xFF0466c8)
        ),
        visualTransformation = if(password) PasswordVisualTransformation() else VisualTransformation.None,
        trailingIcon = trailing,
        isError = isError,
    )
}

@Composable
fun CircularProgress(isLoading : MutableState<Boolean>) {
    if (isLoading.value){
        Box(modifier = Modifier
            .fillMaxSize()
            .background(Color(0x63000000))) {

            Dialog(onDismissRequest = {}) {
                CircularProgressIndicator (
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFF0466c8)
                )
            }

        }

    }
}

@Composable
fun ImageLoading(imageUri : Uri?, modifier : Modifier = Modifier) {
    val painter = rememberImagePainter(
        data = imageUri,
        builder = {
            placeholder(R.drawable.profile_circle)
            crossfade(200)
            transformations(
                CircleCropTransformation()
            )
        }
    )

    Image(painter = painter, contentDescription = null, modifier = modifier)

}