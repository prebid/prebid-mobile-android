package com.openx.internal_test_app.plugplay.utilities.consent

import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.openx.apollo.utils.logger.OXLog
import com.openx.internal_test_app.data.ConsentConfiguration
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class ConsentUpdateManager(private val defaultSharedPreferences: SharedPreferences) {
    private val TAG = ConsentUpdateManager::class.java.simpleName
    private val PREFIX_CONSENT = "IAB"

    private val scheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    private var updateConfigScheduledFuture: ScheduledFuture<*>? = null
    private var cancelUpdateConfigTimerScheduledFuture: ScheduledFuture<*>? = null

    private var consentConfigPosition = 0
    private val updateOptionsList = mutableListOf<Map<String, Any?>>()


    fun updateConsentConfiguration(configurationJson: String?) {
        val consentConfiguration = parseConsentConfiguration(configurationJson)
        if (consentConfiguration == null) {
            Log.e(TAG, "updateConsentConfiguration(): Failed. Provided configurationJson is null")
            return
        }
        updateConsentPreferences(consentConfiguration.launchOptions)
        startTimer(consentConfiguration.updateIntervalSec, consentConfiguration.updatedOptionList)
    }

    fun stopTimer() {
        updateConfigScheduledFuture?.cancel(true)
        cancelUpdateConfigTimerScheduledFuture?.cancel(true)

        updateConfigScheduledFuture = null
        cancelUpdateConfigTimerScheduledFuture = null
        consentConfigPosition = 0
    }

    private fun parseConsentConfiguration(configurationJson: String?) =
            try {
                Gson().fromJson(configurationJson, ConsentConfiguration::class.java)
            }
            catch (ex: Exception) {
                Log.d(TAG, "Unable to parse provided UserConsent ${Log.getStackTraceString(ex)}")
                null
            }

    private fun startTimer(updateIntervalSec: Long?, updateOptionsList: List<Map<String, Any?>>?) {
        if (updateIntervalSec == null || updateOptionsList == null) {
            OXLog.error(TAG, "startTimer(): Failed. Provided update internal or updateOptionsList is null.")
            return
        }

        this.updateOptionsList.clear()
        this.updateOptionsList.addAll(updateOptionsList)

        setupTimer(updateIntervalSec)
    }

    private fun setupTimer(updateIntervalSec: Long) {
        stopTimer()

        val repeatCount = this.updateOptionsList.size.toLong()
        val millisInFuture = repeatCount * updateIntervalSec

        scheduleUpdateConfigTimer(updateIntervalSec)
        scheduleCancelUpdateConfigTimer(millisInFuture)
    }

    private fun scheduleUpdateConfigTimer(updateIntervalSec: Long) {
        updateConfigScheduledFuture = scheduledExecutorService.scheduleAtFixedRate({
            val updateOptionsMap = updateOptionsList.getOrNull(consentConfigPosition)

            updateConsentPreferences(updateOptionsMap)
            consentConfigPosition = consentConfigPosition.inc()

        }, updateIntervalSec, updateIntervalSec, TimeUnit.SECONDS)
    }

    private fun scheduleCancelUpdateConfigTimer(millisInFuture: Long) {
        cancelUpdateConfigTimerScheduledFuture = scheduledExecutorService.schedule({
            updateConfigScheduledFuture?.cancel(true)
        }, millisInFuture, TimeUnit.SECONDS)
    }

    private fun updateConsentPreferences(updateOptionsMap: Map<String, Any?>?) {
        if (updateOptionsMap == null) {
            OXLog.error(TAG, "updateConsentPreferences(): Failed. Provided options map is null")
            return
        }

        val editor = defaultSharedPreferences.edit()

        updateOptionsMap.filterKeys { it.startsWith(PREFIX_CONSENT) }

        updateOptionsMap.forEach {
            when (val value = it.value) {
                is String -> editor.putString(it.key, value).apply()
                is Boolean -> editor.putBoolean(it.key, value).apply()
                is Int -> editor.putInt(it.key, value).apply()
                is Double -> editor.putInt(it.key, value.toInt()).apply()
                else -> editor.putString(it.key, null).apply()
            }
        }
    }
}