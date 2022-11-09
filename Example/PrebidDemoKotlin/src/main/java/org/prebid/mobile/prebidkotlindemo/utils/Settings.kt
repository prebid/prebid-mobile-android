package org.prebid.mobile.prebidkotlindemo.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

class Settings private constructor(
    private val preferences: SharedPreferences
) {

    companion object {
        private const val KEY_REFRESH_TIME = "KEY_REFRESH_TIME"

        private var INSTANCE: Settings? = null

        fun init(context: Context) {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            INSTANCE = Settings(preferences).apply {
                setFakeGdpr()
            }
        }

        fun get(): Settings {
            return INSTANCE ?: throw NullPointerException()
        }
    }

    var refreshTimeSeconds: Int
        get() = preferences.getInt(KEY_REFRESH_TIME, 30)
        set(value) {
            preferences.edit().putInt(KEY_REFRESH_TIME, value).apply()
        }

    /**
     * Only for test cases!
     */
    private fun setFakeGdpr() {
        preferences.edit().apply {
            putInt("IABTCF_gdprApplies", 0)
            putInt("IABTCF_CmpSdkID", 123)
            apply()
        }
    }

}