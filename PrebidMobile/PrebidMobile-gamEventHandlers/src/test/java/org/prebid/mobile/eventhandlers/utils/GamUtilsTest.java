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

package org.prebid.mobile.eventhandlers.utils;

import android.os.Bundle;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.nativead.NativeCustomFormatAd;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.CacheManager;
import org.prebid.mobile.NativeAdUnit;
import org.prebid.mobile.PrebidNativeAd;
import org.prebid.mobile.rendering.utils.ntv.NativeAdProvider;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashMap;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 21)
public class GamUtilsTest {

    public static final String KEY_IS_PREBID = "isPrebid";

    @After
    public void cleanup() {
        GamUtils.RESERVED_KEYS.clear();
    }

    @Test
    public void didPrebidWin_customTemplate_adIsNullOrNoEvents_ReturnFalse() {
        NativeCustomFormatAd ad = null;
        assertFalse(GamUtils.didPrebidWin(ad));

        ad = mock(NativeCustomFormatAd.class);
        when(ad.getText(KEY_IS_PREBID)).thenReturn("0");

        assertFalse(GamUtils.didPrebidWin(ad));
    }

    @Test
    public void didPrebidWin_customTemplate_adContainsEvents_ReturnTrue() {
        NativeCustomFormatAd ad = mock(NativeCustomFormatAd.class);

        when(ad.getText(KEY_IS_PREBID)).thenReturn("0");
        assertFalse(GamUtils.didPrebidWin(ad));

        when(ad.getText(KEY_IS_PREBID)).thenReturn("1");
        assertTrue(GamUtils.didPrebidWin(ad));
    }

    @Test
    public void didPrebidWin_unifiedAd_adIsNullOrNoEvents_ReturnFalse() {
        com.google.android.gms.ads.nativead.NativeAd ad = null;
        assertFalse(GamUtils.didPrebidWin(ad));

        ad = mock(com.google.android.gms.ads.nativead.NativeAd.class);
        when(ad.getBody()).thenReturn("");

        assertFalse(GamUtils.didPrebidWin(ad));
    }

    @Test
    public void nativeAdProvider_putExtrasWithCacheId_returnNativeAd() {
        String cacheId = CacheManager.save(getNativeAdContent());
        Bundle extras = new Bundle();
        extras.putString(NativeAdUnit.BUNDLE_KEY_CACHE_ID, cacheId);

        PrebidNativeAd prebidNativeAd = NativeAdProvider.getNativeAd(extras);
        assertNotNull(prebidNativeAd);
    }

    @Test
    public void nativeAdProvider_putEmptyExtras_returnNull() {
        Bundle extras = new Bundle();
        PrebidNativeAd prebidNativeAd = NativeAdProvider.getNativeAd(extras);
        assertNull(prebidNativeAd);
    }

    @Test
    public void prepare_AddReservedKeys() {
        final AdManagerAdRequest publisherAdRequest = new AdManagerAdRequest.Builder().build();
        Bundle extras = new Bundle();
        extras.putString("key1", "param1");
        extras.putString("key2", "param2");
        extras.putString("key3", "param3");
        extras.putString(NativeAdUnit.BUNDLE_KEY_CACHE_ID, "param4");

        GamUtils.prepare(publisherAdRequest, extras);

        Bundle actualTargeting = publisherAdRequest.getCustomTargeting();

        assertEquals(3, actualTargeting.size());
        assertEquals("Bundle[{key1=param1, key2=param2, key3=param3}]", actualTargeting.toString());
    }

    @Test
    public void handleGamKeywordsUpdate_KeyWordsShouldMatchExpected() {
        AdManagerAdRequest.Builder builder = new AdManagerAdRequest.Builder();
        builder.addCustomTargeting("Key", "Value");
        HashMap<String, String> bids = new HashMap<>();
        bids.put("hb_pb", "0.50");
        bids.put("hb_cache_id", "123456");
        AdManagerAdRequest request = builder.build();
        GamUtils.handleGamCustomTargetingUpdate(request, bids);

        Assert.assertEquals(3, request.getCustomTargeting().size());
        assertTrue(request.getCustomTargeting().containsKey("Key"));
        Assert.assertEquals("Value", request.getCustomTargeting().get("Key"));
        assertTrue(request.getCustomTargeting().containsKey("hb_pb"));
        Assert.assertEquals("0.50", request.getCustomTargeting().get("hb_pb"));
        assertTrue(request.getCustomTargeting().containsKey("hb_cache_id"));
        Assert.assertEquals("123456", request.getCustomTargeting().get("hb_cache_id"));

        GamUtils.handleGamCustomTargetingUpdate(request, null);
        Assert.assertEquals(1, request.getCustomTargeting().size());
        assertTrue(request.getCustomTargeting().containsKey("Key"));
        Assert.assertEquals("Value", request.getCustomTargeting().get("Key"));
    }

    private String getNativeAdContent() {
        return "{\"id\":\"test-bid-id-1\",\"impid\":\"2CA244FB-489F-486C-A314-D62079D49129\",\"price\":0.1,\"adm\":\"{ \\\"assets\\\": [{ \\\"required\\\": 1, \\\"title\\\": { \\\"text\\\": \\\"OpenX (Title)\\\" } }, { \\\"required\\\": 1, \\\"img\\\": { \\\"type\\\": 1, \\\"url\\\": \\\"https:\\/\\/www.saashub.com\\/images\\/app\\/service_logos\\/5\\/1df363c9a850\\/large.png?1525414023\\\" } }, { \\\"required\\\": 1, \\\"img\\\": { \\\"type\\\": 3, \\\"url\\\": \\\"https:\\/\\/ssl-i.cdn.openx.com\\/mobile\\/demo-creatives\\/mobile-demo-banner-640x100.png\\\" } }, { \\\"required\\\": 1, \\\"data\\\": { \\\"type\\\": 1, \\\"value\\\": \\\"OpenX (Brand)\\\" } }, { \\\"required\\\": 1, \\\"data\\\": { \\\"type\\\": 2, \\\"value\\\": \\\"Learn all about this awesome story of someone using out OpenX SDK.\\\" } }, { \\\"required\\\": 1, \\\"data\\\": { \\\"type\\\": 12, \\\"value\\\": \\\"Click here to visit our site!\\\" } } ], \\\"link\\\":{ \\\"url\\\": \\\"https:\\/\\/www.openx.com\\/\\\", \\\"clicktrackers\\\":[\\\"https:\\/\\/10.0.2.2:8000\\/events\\/click\\/root\\/url\\\"] }, \\\"eventtrackers\\\":[ { \\\"event\\\":1, \\\"method\\\":1, \\\"url\\\":\\\"https:\\/\\/10.0.2.2:8000\\/events\\/tracker\\/impression\\\" }, { \\\"event\\\":2, \\\"method\\\":1, \\\"url\\\":\\\"https:\\/\\/10.0.2.2:8000\\/events\\/tracker\\/mrc50\\\" }, { \\\"event\\\":3, \\\"method\\\":1, \\\"url\\\":\\\"https:\\/\\/10.0.2.2:8000\\/events\\/tracker\\/mrc100\\\" },{\\\"event\\\":555,\\\"method\\\":2,\\\"url\\\":\\\"http:\\/\\/10.0.2.2:8002\\/static\\/omid-validation-verification-script-v1-ios-video.js\\\",\\\"ext\\\":{\\\"vendorKey\\\":\\\"iabtechlab.com-omid\\\",\\\"verification_parameters\\\":\\\"iabtechlab-openx\\\"}} ] }\",\"adid\":\"test-ad-id-12345\",\"adomain\":[\"openx.com\"],\"crid\":\"test-creative-id-1\",\"w\":300,\"h\":250,\"ext\":{\"prebid\":{\"cache\":{\"key\":\"\",\"url\":\"\",\"bids\":{\"url\":\"10.0.2.2:8000\\/cache?uuid=native-default-example\",\"cacheId\":\"native-default-example\"}},\"targeting\":{\"hb_bidder\":\"openx\",\"hb_bidder_openx\":\"openx\",\"hb_cache_host\":\"10.0.2.2:8000\",\"hb_cache_host_openx\":\"10.0.2.2:8000\",\"hb_cache_id\":\"native-default-example\",\"hb_cache_id_openx\":\"native-default-example\",\"hb_cache_path\":\"\\/cache\",\"hb_cache_path_openx\":\"\\/cache\",\"hb_env\":\"mobile-app\",\"hb_env_openx\":\"mobile-app\",\"hb_pb\":\"0.10\",\"hb_pb_openx\":\"0.10\",\"hb_size\":\"300x250\",\"hb_size_openx\":\"300x250\"},\"type\":\"native\",\"video\":{\"duration\":0,\"primary_category\":\"\"}},\"bidder\":{\"ad_ox_cats\":[2],\"agency_id\":\"agency_10\",\"brand_id\":\"brand_10\",\"buyer_id\":\"buyer_10\",\"matching_ad_id\":{\"campaign_id\":1,\"creative_id\":3,\"placement_id\":2},\"next_highest_bid_price\":0.099}}}";
    }

}