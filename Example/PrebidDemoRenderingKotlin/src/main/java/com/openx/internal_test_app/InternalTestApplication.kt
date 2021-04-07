package com.openx.internal_test_app

import android.content.Context
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.preference.PreferenceManager
import android.webkit.WebView
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import com.openx.apollo.sdk.ApolloSettings
import com.openx.internal_test_app.utils.DemoItemProvider
import com.openx.internal_test_app.utils.MockServerUtils
import com.openx.internal_test_app.utils.SourcePicker


class InternalTestApplication : MultiDexApplication() {

    companion object {
        @JvmStatic
        lateinit var instance: InternalTestApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        ApolloSettings.setAccountId(getString(R.string.openx_account_id_prod))
        ApolloSettings.logLevel = ApolloSettings.LogLevel.DEBUG

        // Setup mock responses only in mock build
        if (BuildConfig.FLAVOR == "mock") {
            setupMockResponses()
            SourcePicker.useMockServer = true
        }
        DemoItemProvider.init(this)

        // Only uncomment while testing memory leaks
        checkKeepConsentSettingsFlag()
        WebView.setWebContentsDebuggingEnabled(true)
    }

    private fun setupMockResponses() {
        MockServerUtils.clearLogs()
    }

    private fun enableStrictMode() {
        StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                        .detectAll()
                        .penaltyLog()
                        .build()
        )
        StrictMode.setVmPolicy(VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .penaltyLog()
                .penaltyDeath()
                .build())
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    fun clearAdConfigSettings() {
        PreferenceManager.getDefaultSharedPreferences(this).edit().clear().apply()
    }

    private fun checkKeepConsentSettingsFlag() {
        val keepSettingsKey = getString(R.string.key_keep_settings)
        val keepSettings = PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(keepSettingsKey, false)

        if (!keepSettings) {
            clearAdConfigSettings()
        }
    }
}