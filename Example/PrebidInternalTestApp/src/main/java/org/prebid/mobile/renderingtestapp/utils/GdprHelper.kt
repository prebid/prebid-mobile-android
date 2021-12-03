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

import android.content.SharedPreferences

class GdprHelper(private val defaultPreferences: SharedPreferences) {

    companion object {
        private const val GDPR_APPLIES = "IABTCF_gdprApplies"
        private const val CMP_SDK_ID = "IABTCF_CmpSdkID"
    }

    fun changeGdprState(isEnabled: Boolean) {
        if (isEnabled) {
            defaultPreferences.edit().remove(GDPR_APPLIES).apply()
            defaultPreferences.edit().remove(CMP_SDK_ID).apply()
        } else {
            //Set fake params to disable GDPR
            defaultPreferences.edit().putInt(GDPR_APPLIES, 0).apply()
            defaultPreferences.edit().putInt(CMP_SDK_ID, 123).apply()
        }
    }

    fun isGdprEnabled(): Boolean {
        val defaultValue = -1
        return defaultPreferences.getInt(GDPR_APPLIES, defaultValue) == 1
                || defaultPreferences.getInt(CMP_SDK_ID, defaultValue) == defaultValue
    }
}