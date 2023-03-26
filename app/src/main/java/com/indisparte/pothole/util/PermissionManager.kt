package com.indisparte.pothole.util

/**
 * Provides location permission management
 * @author Antonio Di Nuzzo (Indisparte)
 */
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionManager(
    private val activity: Activity,
    private val permissions: Array<String>,
    private val requestCode: Int,
    private val onPermissionsResult: (Map<String, Boolean>) -> Unit
) {

    fun checkPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            // Permissions are granted at install time on devices with API level lower than 23
            onPermissionsResult(permissions.associateWith { true })
            return
        }

        val missingPermissions = mutableListOf<String>()

        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(activity, permission)
                != PackageManager.PERMISSION_GRANTED
            ) {
                missingPermissions.add(permission)
            }
        }

        if (missingPermissions.isEmpty()) {
            // All permissions are granted
            onPermissionsResult(permissions.map { it to true }.toMap())
        } else {
            // Request missing permissions
            ActivityCompat.requestPermissions(activity, missingPermissions.toTypedArray(), requestCode)
        }
    }

    fun handlePermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == this.requestCode) {
            val result = mutableMapOf<String, Boolean>()
            for (i in permissions.indices) {
                result[permissions[i]] = grantResults[i] == PackageManager.PERMISSION_GRANTED
            }
            onPermissionsResult(result)
        }
    }
}


