package com.openx.internal_test_app.utils

import com.openx.apollo.models.ntv.NativeAdConfiguration
import com.openx.apollo.models.ntv.NativeEventTracker
import com.openx.apollo.models.openrtb.bidRequests.Ext
import com.openx.apollo.models.openrtb.bidRequests.assets.*
import org.json.JSONException
import org.json.JSONObject

object NativeConfigurationStore {

    private var storedConfiguration: NativeAdConfiguration? = null

    @Throws(JSONException::class)
    fun createNativeConfigFrom(nativeJsonString: String) {
        val nativeJsonObject = JSONObject(nativeJsonString)
        val nativeAdConfiguration = NativeAdConfiguration()
        if (nativeJsonObject.has("context")) {
            nativeAdConfiguration.contextType = getContext(nativeJsonObject)
        }
        if (nativeJsonObject.has("contextsubtype")) {
            nativeAdConfiguration.contextSubType = getContextSubType(nativeJsonObject)
        }
        if (nativeJsonObject.has("plcmttype")) {
            nativeAdConfiguration.placementType = getPlacementType(nativeJsonObject)
        }
        if (nativeJsonObject.has("seq")) {
            nativeAdConfiguration.setSeq(nativeJsonObject.getInt("seq"))
        }
        if (nativeJsonObject.has("privacy")) {
            nativeAdConfiguration.privacy = nativeJsonObject.getInt("privacy") == 1
        }
        if (nativeJsonObject.has("assets")) {
            extractAssets(nativeAdConfiguration, nativeJsonObject)
        }
        if (nativeJsonObject.has("eventtrackers")) {
            extractEventTrackers(nativeAdConfiguration, nativeJsonObject)
        }
        if (nativeJsonObject.has("ext")) {
            val ext = Ext()
            ext.put(nativeJsonObject.getJSONObject("ext"))
            nativeAdConfiguration.ext = ext
        }

        storedConfiguration = nativeAdConfiguration
    }

    fun getStoredNativeConfig() = storedConfiguration

    private fun extractAssets(nativeAdConfiguration: NativeAdConfiguration, jsonObject: JSONObject) {
        val assetsJsonArray = jsonObject.getJSONArray("assets")
        for (i in 0 until assetsJsonArray.length()) {
            val assetJsonObject = assetsJsonArray.getJSONObject(i)
            nativeAdConfiguration.addAsset(getNativeAsset(assetJsonObject))
        }
    }

    private fun getContext(nativeJsonObject: JSONObject): NativeAdConfiguration.ContextType {
        val contextList = NativeAdConfiguration.ContextType.values()
        val contextInt = nativeJsonObject.getInt("context")

        return contextList.find { it.id == contextInt }
                ?: NativeAdConfiguration.ContextType.CUSTOM.apply { id = contextInt }
    }

    private fun getContextSubType(nativeJsonObject: JSONObject): NativeAdConfiguration.ContextSubType {
        val contextSubTypeList = NativeAdConfiguration.ContextSubType.values()
        val contextSubTypeInt = nativeJsonObject.getInt("contextsubtype")

        return contextSubTypeList.find { it.id == contextSubTypeInt }
                ?: NativeAdConfiguration.ContextSubType.CUSTOM.apply { id = contextSubTypeInt }
    }

    private fun getPlacementType(nativeJsonObject: JSONObject): NativeAdConfiguration.PlacementType {
        val placementTypeList = NativeAdConfiguration.PlacementType.values()
        val placementTypeInt = nativeJsonObject.getInt("plcmttype")

        return placementTypeList.find { it.id == placementTypeInt }
                ?: NativeAdConfiguration.PlacementType.CUSTOM.apply { id = placementTypeInt }
    }

    private fun getNativeAsset(assetJson: JSONObject): NativeAsset? {
        val nativeAsset: NativeAsset? = when {
            assetJson.has("title") -> getNativeTitle(assetJson)
            assetJson.has("img") -> getNativeImage(assetJson)
            assetJson.has("video") -> getNativeVideo(assetJson)
            assetJson.has("data") -> getNativeData(assetJson)
            else -> null
        }
        if (assetJson.has("required")) {
            nativeAsset?.isRequired = assetJson.getInt("required") == 1
        }
        if (assetJson.has("ext")) {
            nativeAsset?.assetExt?.put(assetJson.getJSONObject("ext"))
        }

        return nativeAsset
    }

    private fun getNativeTitle(assetJson: JSONObject): NativeAssetTitle {
        val assetTitle = NativeAssetTitle()
        val titleJson = assetJson.getJSONObject("title")
        if (titleJson.has("len")) {
            assetTitle.len = titleJson.getInt("len")
        }
        if (titleJson.has("ext")) {
            assetTitle.titleExt.put(titleJson.getJSONObject("ext"))
        }
        return assetTitle
    }

    private fun getNativeVideo(assetJson: JSONObject): NativeAssetVideo {
        val assetVideo = NativeAssetVideo()
        val videoJson = assetJson.getJSONObject("video")
        if (videoJson.has("mimes")) {
            val mimesJsonArray = videoJson.getJSONArray("mimes")
            val mimesArray = arrayOfNulls<String>(mimesJsonArray.length())
            for (i in mimesArray.indices) {
                mimesArray[i] = mimesJsonArray.getString(i)
            }
            assetVideo.mimes = mimesArray
        }
        if (videoJson.has("minduration")) {
            assetVideo.minDuration = videoJson.getInt("minduration")
        }
        if (videoJson.has("maxduration")) {
            assetVideo.maxDuration = videoJson.getInt("maxduration")
        }
        if (videoJson.has("protocols")) {
            val protocolsJsonArray = videoJson.getJSONArray("protocols")
            val protocolsArray = arrayOfNulls<Int>(protocolsJsonArray.length())
            for (i in protocolsArray.indices) {
                protocolsArray[i] = protocolsJsonArray.getInt(i)
            }
            assetVideo.protocols = protocolsArray
        }
        if (videoJson.has("ext")) {
            assetVideo.videoExt.put(videoJson.getJSONObject("ext"))
        }
        return assetVideo
    }

    private fun getNativeImage(assetJson: JSONObject): NativeAssetImage {
        val assetImage = NativeAssetImage()
        val imageJson = assetJson.getJSONObject("img")
        if (imageJson.has("type")) {
            val imageTypeList = NativeAssetImage.ImageType.values()
            val imageTypeInt = imageJson.getInt("type")
            assetImage.type = imageTypeList.find { it.id == imageTypeInt }
                    ?: NativeAssetImage.ImageType.CUSTOM.apply { id = imageTypeInt }
        }
        if (imageJson.has("w")) {
            assetImage.w = imageJson.getInt("w")
        }
        if (imageJson.has("wmin")) {
            assetImage.wMin = imageJson.getInt("wmin")
        }
        if (imageJson.has("h")) {
            assetImage.h = imageJson.getInt("h")
        }
        if (imageJson.has("hmin")) {
            assetImage.hMin = imageJson.getInt("hmin")
        }
        if (imageJson.has("mimes")) {
            val mimesJsonArray = imageJson.getJSONArray("mimes")
            val mimesArray = arrayOfNulls<String>(mimesJsonArray.length())
            for (i in mimesArray.indices) {
                mimesArray[i] = mimesJsonArray.getString(i)
            }
            assetImage.mimes = mimesArray
        }
        if (imageJson.has("ext")) {
            assetImage.imageExt.put(imageJson.getJSONObject("ext"))
        }
        return assetImage
    }

    private fun getNativeData(assetJson: JSONObject): NativeAssetData {
        val assetData = NativeAssetData()
        val dataJson = assetJson.getJSONObject("data")
        if (dataJson.has("type")) {
            val dataTypeList = NativeAssetData.DataType.values()
            val dataTypeInt = dataJson.getInt("type")
            assetData.type = dataTypeList.find { it.id == dataTypeInt }
                    ?: NativeAssetData.DataType.CUSTOM.apply { id = dataTypeInt }
        }
        if (dataJson.has("len")) {
            assetData.len = dataJson.getInt("len")
        }
        if (dataJson.has("ext")) {
            assetData.dataExt.put(dataJson.getJSONObject("ext"))
        }
        return assetData
    }

    private fun extractEventTrackers(nativeAdConfiguration: NativeAdConfiguration, jsonObject: JSONObject) {
        val trackersJsonArray = jsonObject.getJSONArray("eventtrackers")
        for (i in 0 until trackersJsonArray.length()) {
            val trackerJsonObject = trackersJsonArray.getJSONObject(i)
            nativeAdConfiguration.addTracker(getEventTracker(trackerJsonObject))
        }
    }

    private fun getEventTracker(trackerJsonObject: JSONObject): NativeEventTracker {
        var eventType: NativeEventTracker.EventType? = null
        var methods: ArrayList<NativeEventTracker.EventTrackingMethod>? = null
        if (trackerJsonObject.has("type")) {
            eventType = getEventType(trackerJsonObject)
        }
        if (trackerJsonObject.has("methods")) {
            methods = getEventTrackingMethods(trackerJsonObject)
        }

        val eventTracker = NativeEventTracker(eventType, methods)
        if (trackerJsonObject.has("ext")) {
            val ext = Ext()
            ext.put(trackerJsonObject.getJSONObject("ext"))
            eventTracker.ext = ext
        }

        return eventTracker
    }

    private fun getEventType(trackerJsonObject: JSONObject): NativeEventTracker.EventType {
        val eventTypeList = NativeEventTracker.EventType.values()
        val eventTypeInt = trackerJsonObject.getInt("type")

        return eventTypeList.find { it.id == eventTypeInt }
                ?: NativeEventTracker.EventType.CUSTOM.apply { id = eventTypeInt }
    }

    private fun getEventTrackingMethods(trackerJsonObject: JSONObject): ArrayList<NativeEventTracker.EventTrackingMethod> {
        val methodsJsonArray = trackerJsonObject.getJSONArray("methods")
        val methodsList = ArrayList<NativeEventTracker.EventTrackingMethod>()
        for (i in 0 until methodsJsonArray.length()) {
            val methodTypeList = NativeEventTracker.EventTrackingMethod.values()
            val trackingMethodInt = methodsJsonArray.getInt(i)
            val method = methodTypeList.find { it.id == trackingMethodInt }
                    ?: NativeEventTracker.EventTrackingMethod.CUSTOM.apply { id = trackingMethodInt }
            methodsList.add(method)
        }

        return methodsList
    }
}