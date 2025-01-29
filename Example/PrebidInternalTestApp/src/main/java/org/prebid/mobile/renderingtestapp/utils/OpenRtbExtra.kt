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
        @SerializedName("appContextData") val appExtData: Map<String, List<String>>?,
        @SerializedName("impContextData") val impExtData: Map<String, List<String>>?
) {
    data class Geo(
            @SerializedName("lat") val lat: Float,
            @SerializedName("lon") val lon: Float
    )
}