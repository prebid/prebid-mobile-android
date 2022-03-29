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

package org.prebid.mobile.mopub.tests;

import android.app.Activity;
import com.mopub.mediation.MoPubBannerMediationUtils;
import com.mopub.mediation.MoPubInterstitialMediationUtils;
import com.mopub.mobileads.MoPubInterstitial;
import com.mopub.mobileads.MoPubView;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class KeywordsSetupTest {

    private static final String KEY_BID_RESPONSE = "PREBID_BID_RESPONSE_ID";
    private Activity activity;

    @Before
    public void setup() {
        activity = Robolectric.buildActivity(Activity.class).create().get();
    }

    @Test
    public void handleMoPubKeywordsUpdateWithBanner_KeyWordsShouldMatchExpected() {
        MoPubView adView = new MoPubView(activity);
        MoPubBannerMediationUtils mediationUtils = new MoPubBannerMediationUtils(adView);

        adView.setKeywords("key1:value1,key2:value2");
        HashMap<String, String> bids = new HashMap<>();
        bids.put("hb_pb", "0.50");
        bids.put("hb_cache_id", "123456");
        mediationUtils.handleKeywordsUpdate(bids);
        String adViewKeywords = adView.getKeywords();
        assertEquals("hb_pb:0.50,hb_cache_id:123456,key1:value1,key2:value2", adViewKeywords);

        mediationUtils.handleKeywordsUpdate(null);
        assertEquals("key1:value1,key2:value2", adView.getKeywords());
    }

    @Test
    public void handleMoPubKeywordsUpdateWithInterstitial_KeyWordsShouldMatchExpected() {
        MoPubInterstitial moPubInterstitial = new MoPubInterstitial(activity, "123456");
        MoPubInterstitialMediationUtils mediationUtils = new MoPubInterstitialMediationUtils(moPubInterstitial);
        HashMap<String, String> bids = new HashMap<>();
        bids.put("hb_pb", "0.50");
        bids.put("hb_cache_id", "123456");

        moPubInterstitial.setKeywords("key1:value1,key2:value2");
        mediationUtils.handleKeywordsUpdate(bids);
        assertEquals("hb_pb:0.50,hb_cache_id:123456,key1:value1,key2:value2", moPubInterstitial.getKeywords());

        mediationUtils.handleKeywordsUpdate(null);
        assertEquals("key1:value1,key2:value2", moPubInterstitial.getKeywords());
    }

    @Test
    public void setResponseToMoPubLocalExtrasWithMoPubBannerOrInterstitialView_ResponseStoredInLocalExtras() {
        MoPubView moPubView = new MoPubView(activity);
        MoPubBannerMediationUtils mediationUtils = new MoPubBannerMediationUtils(moPubView);
        BidResponse response = new BidResponse("{\"response\":\"test\", \"id\":\"1234\"}");
        Map<String, Object> expectedLocalExtras = Collections.singletonMap(KEY_BID_RESPONSE, response.getId());

        mediationUtils.setResponseToLocalExtras(response);

        assertEquals(expectedLocalExtras, moPubView.getLocalExtras());
    }

}