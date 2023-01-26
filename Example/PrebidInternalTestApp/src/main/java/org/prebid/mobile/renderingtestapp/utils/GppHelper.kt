package org.prebid.mobile.renderingtestapp.utils

import android.content.SharedPreferences

class GppHelper(private val defaultPreferences: SharedPreferences) {

    companion object {
        private const val GPP_STRING_KEY = "IABGPP_HDR_GppString"
        private const val GPP_SID_KEY = "IABGPP_GppSID"
    }

    fun addGppStringTestValue(value: String) {
        defaultPreferences.edit().putString(GPP_STRING_KEY, value).apply()
    }

    fun addGppSidTestValue(value: String) {
        defaultPreferences.edit().putString(GPP_SID_KEY, value).apply()
    }

}