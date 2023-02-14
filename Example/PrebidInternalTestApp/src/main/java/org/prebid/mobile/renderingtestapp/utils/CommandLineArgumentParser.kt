package org.prebid.mobile.renderingtestapp.utils

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import org.json.JSONArray
import org.json.JSONObject
import org.prebid.mobile.*
import org.prebid.mobile.renderingtestapp.plugplay.utilities.consent.ConsentUpdateManager

object CommandLineArgumentParser {

    private val adUnitSpecificData = AdUnitSpecificData()

    class AdUnitSpecificData(
        var extKeywords: String? = null,
        var extData: Map<String, List<String>>? = null,
        var appContentData: Map<String, List<String>>? = null,
        var userData: DataObject? = null,
    )

    fun parse(intent: Intent?, context: Context) {
        val extras = intent?.extras ?: return
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)

        extras.getBoolean("shareGeo").let { shareGeo ->
            PrebidMobile.setShareGeoLocation(shareGeo)
        }

        extras.getString("targetingDomain")?.let { targetingDomain ->
            TargetingParams.setDomain(targetingDomain)
        }

        extras.getString("gppString")?.let { gppStringValue ->
            GppHelper(preferences).addGppStringTestValue(gppStringValue)
        }

        extras.getString("gppSid")?.let { gppSidValue ->
            GppHelper(preferences).addGppSidTestValue(gppSidValue)
        }

        extras.getString("EXTRA_OPEN_RTB")?.let {
            extractOpenRtbExtra(it, context)
        }

        extras.getString("EXTRA_CONSENT_V1")?.let {
            handleConsentExtra(it, preferences)
        }

        extras.getString("EXTRA_EIDS")?.let {
            extractEidsExtras(it)
        }

        /* Global data */
        extras.getStringArray("BIDDER_ACCESS_CONTROL_LIST")?.forEach {
            TargetingParams.addBidderToAccessControlList(it)
        }
        extras.getString("ADD_USER_EXT_DATA")?.let {
            parseUserExtData(it)
        }
        extras.getString("ADD_APP_EXT")?.let {
            parseAppExtData(it)
        }
        extras.getString("ADD_APP_KEYWORD")?.let {
            TargetingParams.addContextKeyword(it)
        }
        extras.getString("ADD_USER_KEYWORD")?.let {
            TargetingParams.addUserKeyword(it)
        }

        /* Ad unit specific data */
        extras.getString("ADD_ADUNIT_CONTEXT")?.let {
            adUnitSpecificData.extData = parseJsonToMapOfStringsAndStringLists(it)
        }
        extras.getString("ADD_ADUNIT_KEYWORD")?.let {
            adUnitSpecificData.extKeywords = it
        }
        extras.getString("ADD_APP_CONTENT_DATA_EXT")?.let {
// TODO:            adUnitSpecificData.appContentData = parseJsonToMapOfStringsAndStringLists(it)
        }
        extras.getString("ADD_USER_DATA_EXT")?.let {
// TODO:           adUnitSpecificData.userData =
        }
    }

    fun addAdUnitSpecificData(adUnit: BannerAdUnit) {
        val extData = adUnitSpecificData.extData
        if (extData != null) {
            for (key in extData.keys) {
                for (value in extData[key]!!) {
                    adUnit.addContextData(key, value)
                }
            }
        }

        val extKeywords = adUnitSpecificData.extKeywords
        if (extKeywords != null) {
            adUnit.addExtKeyword(extKeywords)
        }

        // TODO: app content data
        // TODO: user data
    }


    private fun extractOpenRtbExtra(openRtbListJson: String, context: Context) {
        val openRtbExtrasList = try {
            Gson().fromJson<OpenRtbExtra>(openRtbListJson, object : TypeToken<OpenRtbExtra>() {}.type)
        } catch (ex: Exception) {
            Log.d("CommandLineArguments", "Unable to parse provided OpenRTB list ${Log.getStackTraceString(ex)}")
            Toast.makeText(
                context,
                "Unable to parse provided OpenRTB. Provided JSON might contain an error",
                Toast.LENGTH_LONG
            ).show()
            return
        }
        if (openRtbExtrasList != null) {
            OpenRtbConfigs.setTargeting(openRtbExtrasList)
        }
    }

    private fun handleConsentExtra(configurationJson: String, preferences: SharedPreferences) {
        val consentUpdateManager = ConsentUpdateManager(preferences)
        consentUpdateManager.updateConsentConfiguration(configurationJson)
    }

    private fun extractEidsExtras(eidsJsonString: String) {
        val eidsJsonArray = JSONArray(eidsJsonString)
        for (i in 0 until eidsJsonArray.length()) {
            val jsonObject = eidsJsonArray.get(i)
            if (jsonObject is JsonObject) {
                val source = jsonObject.get("source").asString
                val identifier = jsonObject.get("identifier").asString
                if (source == null || identifier == null) {
                    val aType = jsonObject.get("atype")
                    TargetingParams.storeExternalUserId(
                        if (aType == null) {
                            ExternalUserId(source, identifier, null, null)
                        } else {
                            ExternalUserId(source, identifier, aType.asInt, null)
                        }
                    )
                }
            }
        }
    }

    private fun parseUserExtData(json: String) {
        val map = parseJsonToMapOfStringsAndStringLists(json)
        map.forEach {
            for (value in it.value) {
                TargetingParams.addUserData(it.key, value)
            }
        }
    }

    private fun parseAppExtData(json: String) {
        val map = parseJsonToMapOfStringsAndStringLists(json)
        map.forEach {
            for (value in it.value) {
                TargetingParams.addContextData(it.key, value)
            }
        }
    }


    private fun parseJsonToMapOfStringsAndStringLists(json: String): Map<String, List<String>> {
        val result = mutableMapOf<String, List<String>>()

        val jsonObject = JSONObject(json)
        val keys = jsonObject.names() ?: return result
        for (i in 0 until keys.length()) {
            val key = keys.get(i) as String
            result[key] = jsonArrayToStringList(jsonObject.getJSONArray(key))
        }

        return result
    }

    private fun jsonArrayToStringList(array: JSONArray): List<String> {
        val result = mutableListOf<String>()
        for (i in 0 until array.length()) {
            val string = array.get(i) as String
            result.add(string)
        }
        return result
    }

}