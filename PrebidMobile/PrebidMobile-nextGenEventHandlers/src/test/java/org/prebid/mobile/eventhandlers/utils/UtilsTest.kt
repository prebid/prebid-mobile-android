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
import com.google.android.libraries.ads.mobile.sdk.nativead.CustomNativeAd
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd
import org.junit.After
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.prebid.mobile.CacheManager
import org.prebid.mobile.NativeAdUnit
import org.prebid.mobile.eventhandlers.utils.Utils.didPrebidWin
import org.prebid.mobile.rendering.utils.ntv.NativeAdProvider
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [24])
class UtilsTest {
    @After
    fun cleanup() {
        Utils.RESERVED_KEYS.clear()
    }

    @Test
    fun didPrebidWin_customTemplate_adIsNullOrNoEvents_ReturnFalse() {
        var ad: CustomNativeAd? = null
        Assert.assertFalse(didPrebidWin(ad))

        ad = Mockito.mock(CustomNativeAd::class.java)
        Mockito.`when`<String?>(ad.getText(KEY_IS_PREBID)).thenReturn("0")

        Assert.assertFalse(didPrebidWin(ad))
    }

    @Test
    fun didPrebidWin_customTemplate_adContainsEvents_ReturnTrue() {
        val ad = Mockito.mock(CustomNativeAd::class.java)

        Mockito.`when`<String?>(ad.getText(KEY_IS_PREBID)).thenReturn("0")
        Assert.assertFalse(didPrebidWin(ad))

        Mockito.`when`<String?>(ad.getText(KEY_IS_PREBID)).thenReturn("1")
        Assert.assertTrue(didPrebidWin(ad))
    }

    @Test
    fun didPrebidWin_unifiedAd_adIsNullOrNoEvents_ReturnFalse() {
        var ad: NativeAd? = null
        Assert.assertFalse(Utils.didPrebidWin(ad))


        ad = Mockito.mock(NativeAd::class.java)
        Mockito.`when`<String?>(ad.body).thenReturn("")

        Assert.assertFalse(didPrebidWin(ad))
    }

    @Test
    fun nativeAdProvider_putExtrasWithCacheId_returnNativeAd() {
        val cacheId = CacheManager.save(this.nativeAdContent)
        val extras = Bundle()
        extras.putString(NativeAdUnit.BUNDLE_KEY_CACHE_ID, cacheId)

        val prebidNativeAd = NativeAdProvider.getNativeAd(extras)
        Assert.assertNotNull(prebidNativeAd)
    }

    @Test
    fun nativeAdProvider_putEmptyExtras_returnNull() {
        val extras = Bundle()
        val prebidNativeAd = NativeAdProvider.getNativeAd(extras)
        Assert.assertNull(prebidNativeAd)
    }

    private val nativeAdContent: String
        get() = "{\"id\":\"test-bid-id-1\",\"impid\":\"2CA244FB-489F-486C-A314-D62079D49129\",\"price\":0.1,\"adm\":\"{ \\\"assets\\\": [{ \\\"required\\\": 1, \\\"title\\\": { \\\"text\\\": \\\"OpenX (Title)\\\" } }, { \\\"required\\\": 1, \\\"img\\\": { \\\"type\\\": 1, \\\"url\\\": \\\"https:\\/\\/www.saashub.com\\/images\\/app\\/service_logos\\/5\\/1df363c9a850\\/large.png?1525414023\\\" } }, { \\\"required\\\": 1, \\\"img\\\": { \\\"type\\\": 3, \\\"url\\\": \\\"https:\\/\\/ssl-i.cdn.openx.com\\/mobile\\/demo-creatives\\/mobile-demo-banner-640x100.png\\\" } }, { \\\"required\\\": 1, \\\"data\\\": { \\\"type\\\": 1, \\\"value\\\": \\\"OpenX (Brand)\\\" } }, { \\\"required\\\": 1, \\\"data\\\": { \\\"type\\\": 2, \\\"value\\\": \\\"Learn all about this awesome story of someone using out OpenX SDK.\\\" } }, { \\\"required\\\": 1, \\\"data\\\": { \\\"type\\\": 12, \\\"value\\\": \\\"Click here to visit our site!\\\" } } ], \\\"link\\\":{ \\\"url\\\": \\\"https:\\/\\/www.openx.com\\/\\\", \\\"clicktrackers\\\":[\\\"https:\\/\\/10.0.2.2:8000\\/events\\/click\\/root\\/url\\\"] }, \\\"eventtrackers\\\":[ { \\\"event\\\":1, \\\"method\\\":1, \\\"url\\\":\\\"https:\\/\\/10.0.2.2:8000\\/events\\/tracker\\/impression\\\" }, { \\\"event\\\":2, \\\"method\\\":1, \\\"url\\\":\\\"https:\\/\\/10.0.2.2:8000\\/events\\/tracker\\/mrc50\\\" }, { \\\"event\\\":3, \\\"method\\\":1, \\\"url\\\":\\\"https:\\/\\/10.0.2.2:8000\\/events\\/tracker\\/mrc100\\\" },{\\\"event\\\":555,\\\"method\\\":2,\\\"url\\\":\\\"http:\\/\\/10.0.2.2:8002\\/static\\/omid-validation-verification-script-v1-ios-video.js\\\",\\\"ext\\\":{\\\"vendorKey\\\":\\\"iabtechlab.com-omid\\\",\\\"verification_parameters\\\":\\\"iabtechlab-openx\\\"}} ] }\",\"adid\":\"test-ad-id-12345\",\"adomain\":[\"openx.com\"],\"crid\":\"test-creative-id-1\",\"w\":300,\"h\":250,\"ext\":{\"prebid\":{\"cache\":{\"key\":\"\",\"url\":\"\",\"bids\":{\"url\":\"10.0.2.2:8000\\/cache?uuid=native-default-example\",\"cacheId\":\"native-default-example\"}},\"targeting\":{\"hb_bidder\":\"openx\",\"hb_bidder_openx\":\"openx\",\"hb_cache_host\":\"10.0.2.2:8000\",\"hb_cache_host_openx\":\"10.0.2.2:8000\",\"hb_cache_id\":\"native-default-example\",\"hb_cache_id_openx\":\"native-default-example\",\"hb_cache_path\":\"\\/cache\",\"hb_cache_path_openx\":\"\\/cache\",\"hb_env\":\"mobile-app\",\"hb_env_openx\":\"mobile-app\",\"hb_pb\":\"0.10\",\"hb_pb_openx\":\"0.10\",\"hb_size\":\"300x250\",\"hb_size_openx\":\"300x250\"},\"type\":\"native\",\"video\":{\"duration\":0,\"primary_category\":\"\"}},\"bidder\":{\"ad_ox_cats\":[2],\"agency_id\":\"agency_10\",\"brand_id\":\"brand_10\",\"buyer_id\":\"buyer_10\",\"matching_ad_id\":{\"campaign_id\":1,\"creative_id\":3,\"placement_id\":2},\"next_highest_bid_price\":0.099}}}"

    companion object {
        const val KEY_IS_PREBID: String = "isPrebid"
    }
}