package org.prebid.mobile.renderingtestapp.utils

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionHelper {

    companion object {
        @JvmStatic
        fun requestPermission(activityContext: Activity) {
            val permissions = ArrayList<String>()
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)

            val permissionsNeeded = ArrayList<String>()
            for (permission in permissions) {
                if (ContextCompat.checkSelfPermission(activityContext, permission) != PackageManager.PERMISSION_GRANTED) {
                    permissionsNeeded.add(permission)
                }
            }

            if (permissionsNeeded.size > 0) {
                val permissionsToRequest = arrayOfNulls<String>(permissionsNeeded.size)
                permissionsNeeded.toArray(permissionsToRequest)
                ActivityCompat.requestPermissions(activityContext, permissionsToRequest, 0)
            }
        }
    }
}