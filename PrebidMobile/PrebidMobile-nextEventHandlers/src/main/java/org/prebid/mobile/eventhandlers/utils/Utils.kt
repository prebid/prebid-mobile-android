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
package org.prebid.mobile.eventhandlers.utils

import android.os.Bundle
import com.google.android.libraries.ads.mobile.sdk.common.BaseAdRequestBuilder
import com.google.android.libraries.ads.mobile.sdk.nativead.CustomNativeAd
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd
import org.prebid.mobile.LogUtil
import org.prebid.mobile.NativeAdUnit

object Utils {
    private val TAG: String = Utils::class.java.getSimpleName()
    private val RESERVED_KEYS: HashSet<String?> = HashSet()
    private const val KEY_IS_PREBID_CREATIVE = "isPrebid"

    fun prepare(builder: BaseAdRequestBuilder<*>, extras: Bundle) {
        val keywords: MutableMap<String, String> = HashMap()
        for (key in extras.keySet()) {
            val value = extras.get(key)
            if (value is String) {
                if (key != NativeAdUnit.BUNDLE_KEY_CACHE_ID) {
                    keywords[key] = value
                }
            }
        }

        handleCustomTargetingUpdate(builder, keywords)
    }

    fun handleCustomTargetingUpdate(
        builder: BaseAdRequestBuilder<*>,
        keywords: MutableMap<String, String>,
    ) {
        if (keywords.isEmpty()) {
            LogUtil.error(TAG, "prepare: Failed. Result contains invalid keywords")
            return
        }

        for (entry in keywords.entries) {
            builder.putCustomTargeting(entry.key, entry.value)
            addReservedKeys(entry.key)
        }
    }

    fun didPrebidWin(unifiedNativeAd: NativeAd): Boolean {
        val body = unifiedNativeAd.body
        return KEY_IS_PREBID_CREATIVE == body
    }

    fun didPrebidWin(ad: CustomNativeAd): Boolean {
        val isPrebidValue: CharSequence? = ad.getText(KEY_IS_PREBID_CREATIVE)

        return isPrebidValue != null && "1".contentEquals(isPrebidValue)
    }

    private fun addReservedKeys(key: String) {
        synchronized(RESERVED_KEYS) {
            RESERVED_KEYS.add(key)
        }
    }
}
