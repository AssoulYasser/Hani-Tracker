package my.hanitracker.manager

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import my.hanitracker.R

object PermissionManager {

    const val PERMISSION_REQUEST_CODE = 300
    private const val TAG = "DEBUGGING : "

    fun Context.isPermissionGranted(permission: String) : Boolean =
        ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED


    fun Activity.requestPermission(
        permission: String
    ) {
        this.requestPermissions(
            permissions = mutableListOf(permission)
        )
    }

    fun Activity.requestPermissions(
        permissions: MutableList<String>
    ) {
        Log.d(TAG, "requestPermissions: START")
        val preferences = getSharedPreferences(getString(R.string.permission_shared_preferences), Context.MODE_PRIVATE)
        val permissionsToRequest = mutableListOf<String>()
        val permissionsToRedirect = mutableListOf<String>()
        for (permission in permissions){
            if (preferences.contains(permission)) {
                if (preferences.getBoolean(permission, false))
                    permissionsToRedirect.add(permission)
            }
            else
                permissionsToRequest.add(permission)
        }
        if (permissionsToRequest.isNotEmpty())
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), PERMISSION_REQUEST_CODE)
        if (permissionsToRedirect.isNotEmpty())
            requestPermissionsIntent(permissionsToRedirect)
        Log.d(TAG, "requestPermissions: END")
    }

    private fun Activity.requestPermissionsIntent(permissionsToRedirect: MutableList<String>) {
        Log.d(TAG, "requestPermissionsIntent: START")
        AlertDialog.Builder(this)
            .setTitle("PERMISSION NEEDED")
            .setView(
                ComposeView(
                    context = this,
                    attrs = null,
                    defStyleAttr = 0
                ).apply {
                    setContent {
                        MaterialTheme {
                            Text(text = "COMP TEXT")
                        }
                    }
                }
            )
    }

    fun Activity.savePermission(permission: String, isGranted : Boolean) {
        this.savePermissions(mutableListOf(permission), mutableListOf(isGranted))
    }


    fun Activity.savePermissions(permissions: MutableList<String>, isGranted : MutableList<Boolean>) {
        val preferences = getSharedPreferences(getString(R.string.permission_shared_preferences), Context.MODE_PRIVATE)
        with(preferences.edit()){
            for (index in permissions.indices) {
                putBoolean(permissions[index], isGranted[index])
                apply()
            }
        }
    }

}