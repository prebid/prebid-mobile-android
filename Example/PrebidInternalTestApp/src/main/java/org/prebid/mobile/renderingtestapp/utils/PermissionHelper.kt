/*
 *    Copyright 2018-2021 Prebid.org, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

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