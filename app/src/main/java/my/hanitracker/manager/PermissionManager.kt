package my.hanitracker.manager

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionManager(private val activity: Activity) {

    fun isPermissionGranted(permission: String) : Boolean = ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED

    private fun getPermissionRequestCode(permission: String) : Int{
        return when(permission){
            Manifest.permission.POST_NOTIFICATIONS -> 1
            Manifest.permission.ACCESS_COARSE_LOCATION -> 2
            Manifest.permission.ACCESS_FINE_LOCATION -> 3
            else -> -1
        }
    }

    private fun getPermissionRequestCode(permissions: Array<String>) : Array<Int>{
        val result = arrayOf<Int>()
        for (permission in permissions){
            result.plus(getPermissionRequestCode(permission))
        }
        return result
    }

    fun requestPermission(permission: String) {
        ActivityCompat.requestPermissions(activity, arrayOf(permission), getPermissionRequestCode(permission))
    }

    fun requestPermission(permissions: Array<String>){
        ActivityCompat.requestPermissions(activity, permissions, 0)
    }

    fun onPermissionResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 0) {
            for (index in permissions.indices)
                if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                    permissionRequestDialog()
                    break
                }
        }
        else {
            val grantResult = grantResults[0]
            if (grantResult != PackageManager.PERMISSION_GRANTED)
                permissionRequestDialog()
        }
    }

    private fun permissionRequestDialog() {
        AlertDialog.Builder(activity)
            .setTitle("Permissions required")
            .setMessage("some permissions are required, please grant us to access it")
            .setPositiveButton(
                "GRANT ACCESS"
            ) { dialogInterface, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = android.net.Uri.parse("package:" + activity.packageName)
                activity.startActivity(intent)
                dialogInterface.dismiss()
            }
            .setNegativeButton(
                "DENY ACCESS"
            ) { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .show()
    }


}