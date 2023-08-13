package my.hanitracker.ui.screens

import android.annotation.SuppressLint
import android.content.res.Resources
import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil.Coil
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.ImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageScope
import coil.compose.rememberImagePainter
import com.mapbox.maps.MapView
import com.mapbox.maps.ResourceOptionsManager
import my.hanitracker.R
import my.hanitracker.location.UserPlaceNameLocationDataClass
import my.hanitracker.map.MapBusinessLogic

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun MapScreen(
    startTracking : () -> Boolean,
    stopTracking : () -> Unit,
    onCenterCameraPosition : (latitude: Double?, longitude: Double?) -> Unit,
    onStart : (MapView) -> Unit
) {

//    val isTracking = remember { mutableStateOf(false) }
//    var list = mutableListOf<UserPlaceNameLocationDataClass>()

    val displayMetrics = Resources.getSystem().displayMetrics
    val density = LocalDensity.current
    val base = displayMetrics.heightPixels - 30 * density.density
    val pointerOffset = remember { mutableStateOf(base) }
    val isDragUp = remember { mutableStateOf(false) }
    val isUnderDraggingEffect = remember { mutableStateOf(false) }
    val usersListPosition = animateFloatAsState(
        targetValue = pointerOffset.value,
        animationSpec = tween(if(isUnderDraggingEffect.value) 0 else 500)
    )
//
//
//    if (pointerOffset.value == 0f) list = onListingOnlinePeople()
//    else if (pointerOffset.value == base) list.clear()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        AndroidView(
            modifier = Modifier,
            factory = { fContext ->
                ResourceOptionsManager.getDefault(
                    fContext,
                    fContext.getString(R.string.mapbox_access_token)
                )
                val map = MapView(fContext)
                onStart(map)
                map
            }
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(horizontal = 20.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (MapBusinessLogic.isTracking.value) {
                ListOnlinePeople {
                    pointerOffset.value = 0f
//                    list.addAll(onListingOnlinePeople())
                }
                Spacer(modifier = Modifier.height(20.dp))
                CenterCameraPosition(onCenterCameraPosition)
            }
            Spacer(modifier = Modifier.height(20.dp))
            TrackingButton(isTracking = MapBusinessLogic.isTracking, startTracking = startTracking, stopTracking = stopTracking)
        }


        Column(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = (usersListPosition.value / density.density).dp)
                .background(
                    Color.White,
                    shape = RoundedCornerShape(topStart = 25.dp, topEnd = 25.dp)
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp)
                    .pointerInput(Unit) {
                        this.dragEvent(
                            pointerOffset = pointerOffset,
                            isDragUp = isDragUp,
                            base = base, isUnderDraggingEffect = isUnderDraggingEffect,
                        ) {
                            if (!isDragUp.value)
                                pointerOffset.value = 0f
                            else
                                pointerOffset.value = base

                            isUnderDraggingEffect.value = false
                        }
                    },
                contentAlignment = Alignment.Center
            ){
                Box(modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(5.dp)
                    .clip(RoundedCornerShape(100))
                    .background(Color.Black))
            }
            LazyColumn() {
                items(items = MapBusinessLogic.users) { user ->
                    Row(
                        modifier = Modifier
                            .padding(vertical = 10.dp)
                            .clickable {
                                onCenterCameraPosition(user.latitude, user.longitude)
                                pointerOffset.value = base
                            },
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 5.dp)
                                .padding(start = 5.dp)
                                .weight(0.25f)
                        ) {
                            ProfilePictureImage(user.user.pfp)
                        }
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 5.dp)
                                .padding(end = 5.dp),
                            horizontalAlignment = Alignment.Start,
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Text(text = "${user.user.firstName} ${user.user.lastName}")
                            Text(text = user.placeName)
                        }
                    }
                }
            }
        }



    }

}

@Composable
fun ProfilePictureImage(pfp: Uri) {
    SubcomposeAsyncImage(
        model = pfp,
        loading = { CircularProgressIndicator() },
        contentDescription = null,
        modifier = Modifier
            .clip(CircleShape)
    )
}

suspend fun PointerInputScope.dragEvent(
    pointerOffset: MutableState<Float>,
    isDragUp: MutableState<Boolean>,
    isUnderDraggingEffect: MutableState<Boolean>,
    base: Float,
    onDragEnd: () -> Unit
) {
    detectDragGestures(
        onDragEnd = onDragEnd
    ) { change, dragAmount ->
        isUnderDraggingEffect.value = true
        pointerOffset.value += dragAmount.y
        isDragUp.value = dragAmount.y > 0
        change.consumeAllChanges()
    }
}

@Composable
fun ListOnlinePeople(onListingOnlinePeople: () -> Unit) {
    FloatingActionButton(
        onClick = onListingOnlinePeople,
        modifier = Modifier.size(40.dp),
        shape = CircleShape,
        containerColor = Color.White
    ) {
        Image(
            painter = painterResource(id = R.drawable.baseline_people_24),
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun CenterCameraPosition(onCenterCameraPosition: (latitude: Double?, longitude: Double?) -> Unit) {
    FloatingActionButton(
        onClick = { onCenterCameraPosition(null, null) },
        modifier = Modifier.size(40.dp),
        shape = CircleShape,
        containerColor = Color.White
    ) {
        Image(
            painter = painterResource(id = R.drawable.baseline_gps_fixed_24),
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
    }
}

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun TrackingButton(isTracking: MutableState<Boolean>, startTracking: () -> Boolean, stopTracking: () -> Unit) {

    FloatingActionButton(
        onClick = {
            if (isTracking.value) {
                stopTracking()
            }
            else {
                startTracking()
            }
        },
        modifier = Modifier.size(60.dp),
        shape = CircleShape,
        containerColor = if (isTracking.value) Color(0xFFFF0000) else Color(0xFF0466c8),
        contentColor = Color.White
    ) {
        Image(
            painter = painterResource(id = if (isTracking.value) R.drawable.baseline_close_24 else R.drawable.airplan),
            contentDescription = null,
            modifier = Modifier.size(25.dp)
        )
    }

}