package com.openx.internal_test_app.utils

import com.google.gson.annotations.SerializedName

data class OpenRtbExtra(
        @SerializedName("age") val age: Int,
        @SerializedName("url") val appStoreUrl: String?,
        @SerializedName("crr") val carrier: String?,
        @SerializedName("ip") val ipAddress: String?,
        @SerializedName("xid") val userId: String?,
        @SerializedName("gen") val gender: String?,
        @SerializedName("buyerid") val buyerId: String?,
        @SerializedName("customdata") val customData: String?,
        @SerializedName("keywords") val keywords: String?,
        @SerializedName("geo") val geo: Geo?,
        @SerializedName("ext") val userExt: Map<String, Any>?,
        @SerializedName("publisherName") val publisherName: String?,
        @SerializedName("accessControl") val accessControl: List<String>?,
        @SerializedName("userData") val userData: Map<String, List<String>>?,
        @SerializedName("appContextData") val appContextData: Map<String, List<String>>?,
        @SerializedName("impContextData") val impContextData: Map<String, List<String>>?
) {
    data class Geo(
            @SerializedName("lat") val lat: Float,
            @SerializedName("lon") val lon: Float
    )
}