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

import com.google.gson.Gson
import org.prebid.mobile.rendering.models.openrtb.bidRequests.Ext
import org.prebid.mobile.rendering.networking.parameters.UserParameters
import org.prebid.mobile.rendering.networking.targeting.Targeting
import java.lang.reflect.InvocationTargetException

object OpenRtbConfigs {

    var impContextData: Map<String, List<String>>? = null

    fun setTargeting(openRtbExtra: OpenRtbExtra) {
        if (openRtbExtra.age != 0) {
            Targeting.setUserAge(openRtbExtra.age)
        }
        if (openRtbExtra.appStoreUrl != null) {
            Targeting.setAppStoreMarketUrl(openRtbExtra.appStoreUrl)
        }
        if (openRtbExtra.carrier != null) {
            Targeting.setCarrier(openRtbExtra.carrier)
        }
        if (openRtbExtra.ipAddress != null) {
            Targeting.setDeviceIpAddress(openRtbExtra.ipAddress)
        }
        if (openRtbExtra.userId != null) {
            Targeting.setUserId(openRtbExtra.userId)
        }
        if (openRtbExtra.gender != null) {
            Targeting.setUserGender(UserParameters.Gender.valueOf(openRtbExtra.gender))
        }
        if (openRtbExtra.buyerId != null) {
            Targeting.setBuyerUid(openRtbExtra.buyerId)
        }
        if (openRtbExtra.customData != null) {
            Targeting.setUserCustomData(openRtbExtra.customData)
        }
        if (openRtbExtra.keywords != null) {
            Targeting.setUserKeywords(openRtbExtra.keywords)
        }
        if (openRtbExtra.geo != null) {
            Targeting.setUserLatLng(openRtbExtra.geo.lat, openRtbExtra.geo.lon)
        }
        if (openRtbExtra.publisherName != null) {
            Targeting.setPublisherName(openRtbExtra.publisherName)
        }
        if (openRtbExtra.userExt?.isNotEmpty() == true) {
            val ext = Ext()
            val gson = Gson()
            for (key in openRtbExtra.userExt.keys) {
                ext.put(key, gson.toJson(openRtbExtra.userExt[key]))
            }
            Targeting.setUserExt(ext)
        }
        if (openRtbExtra.accessControl?.isNotEmpty() == true) {
            for (bidder in openRtbExtra.accessControl) {
                Targeting.addBidderToAccessControlList(bidder)
            }
        }
        if (openRtbExtra.userData?.isNotEmpty() == true) {
            for (key in openRtbExtra.userData.keys) {
                val dataList = openRtbExtra.userData[key]
                if (dataList != null) {
                    for (data in dataList) {
                        Targeting.addUserData(key, data)
                    }
                }
            }
        }
        if (openRtbExtra.appContextData?.isNotEmpty() == true) {
            for (key in openRtbExtra.appContextData.keys) {
                val dataList = openRtbExtra.appContextData[key]
                if (dataList != null) {
                    for (data in dataList) {
                        Targeting.addContextData(key, data)
                    }
                }
            }
        }
        if (openRtbExtra.impContextData?.isNotEmpty() == true) {
            impContextData = openRtbExtra.impContextData
        }
    }

    fun setImpContextDataTo(adView: Any?) {
        if (adView == null || impContextData == null || impContextData?.isEmpty() == true) {
            return
        }
        for (key in impContextData!!.keys) {
            val dataList = impContextData!![key]
            if (dataList != null) {
                for (data in dataList) {
                    callMethodOnObject(adView, "addContextData", key, data)
                }
            }
        }
    }

    private fun callMethodOnObject(target: Any, methodName: String?, vararg params: Any): Any? {
        try {
            val len = params.size
            val classes: Array<Class<*>?> = arrayOfNulls(len)
            for (i in 0 until len) {
                classes[i] = params[i].javaClass
            }
            val method = target.javaClass.getMethod(methodName, *classes)
            return method.invoke(target, *params)
        }
        catch (e: NullPointerException) {
            e.printStackTrace()
        }
        catch (e: NoSuchMethodException) {
            e.printStackTrace()
        }
        catch (e: InvocationTargetException) {
            e.printStackTrace()
        }
        catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
        return null
    }
}
