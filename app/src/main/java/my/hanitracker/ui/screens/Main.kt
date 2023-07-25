package my.hanitracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.mapbox.maps.MapView
import com.mapbox.maps.ResourceOptionsManager
import com.mapbox.maps.Style
import my.hanitracker.R

@Composable
fun Main(
    startTracking : () -> Unit,
    stopTracking : () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) {

        AndroidView(
            modifier = Modifier,
            factory = { fContext ->
                ResourceOptionsManager.getDefault(
                    fContext,
                    fContext.getString(R.string.mapbox_access_token)
                )

                MapView(fContext).apply {
                    getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS)
                }

            }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(Color.White)
        ) {
            Button(onClick = startTracking) {
                Text(text = "Start")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = stopTracking) {
                Text(text = "Stop")
            }
        }
    }

}