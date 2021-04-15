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

package org.prebid.mobile.rendering.bidding.display;

import android.app.Activity;
import android.view.View;

import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdView;
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.prebid.mobile.rendering.bidding.display.ReflectionUtils.KEY_BID_RESPONSE;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class ReflectionUtilsTest {
    private Activity mActivity;

    class TestObject {
        public String testString() {
            return "test";
        }
    }

    @Before
    public void setup() {
        mActivity = Robolectric.buildActivity(Activity.class).create().get();
    }

    @Test
    public void callMethodOnObject_ExpectedResultIsEqual() {
        TestObject object = new TestObject();
        Object result = ReflectionUtils.callMethodOnObject(object, "testString");
        assertEquals("test", result);
    }

    @Test
    public void handleMoPubKeywordsUpdateWithBanner_KeyWordsShouldMatchExpected() {
        MoPubView adView = new MoPubView(mActivity);
        adView.setKeywords("key1:value1,key2:value2");
        HashMap<String, String> bids = new HashMap<>();
        bids.put("hb_pb", "0.50");
        bids.put("hb_cache_id", "123456");
        ReflectionUtils.handleMoPubKeywordsUpdate(adView, bids);
        String adViewKeywords = adView.getKeywords();
        assertEquals("hb_pb:0.50,hb_cache_id:123456,key1:value1,key2:value2", adViewKeywords);

        ReflectionUtils.handleMoPubKeywordsUpdate(adView, null);
        assertEquals("key1:value1,key2:value2", adView.getKeywords());
    }

    @Test
    public void handleMoPubKeywordsUpdateWithInterstitial_KeyWordsShouldMatchExpected() {
        MoPubInterstitial moPubInterstitial = new MoPubInterstitial(mActivity, "123456");
        HashMap<String, String> bids = new HashMap<>();
        bids.put("hb_pb", "0.50");
        bids.put("hb_cache_id", "123456");

        moPubInterstitial.setKeywords("key1:value1,key2:value2");
        ReflectionUtils.handleMoPubKeywordsUpdate(moPubInterstitial, bids);
        assertEquals("hb_pb:0.50,hb_cache_id:123456,key1:value1,key2:value2", moPubInterstitial.getKeywords());

        ReflectionUtils.handleMoPubKeywordsUpdate(moPubInterstitial, null);
        assertEquals("key1:value1,key2:value2", moPubInterstitial.getKeywords());
    }

    @Test
    public void handleGamKeywordsUpdate_KeyWordsShouldMatchExpected() {
        PublisherAdRequest.Builder builder = new PublisherAdRequest.Builder();
        builder.addCustomTargeting("Key", "Value");
        HashMap<String, String> bids = new HashMap<>();
        bids.put("hb_pb", "0.50");
        bids.put("hb_cache_id", "123456");
        PublisherAdRequest request = builder.build();
        ReflectionUtils.handleGamCustomTargetingUpdate(request, bids);

        assertEquals(3, request.getCustomTargeting().size());
        assertTrue(request.getCustomTargeting().containsKey("Key"));
        assertEquals("Value", request.getCustomTargeting().get("Key"));
        assertTrue(request.getCustomTargeting().containsKey("hb_pb"));
        assertEquals("0.50", request.getCustomTargeting().get("hb_pb"));
        assertTrue(request.getCustomTargeting().containsKey("hb_cache_id"));
        assertEquals("123456", request.getCustomTargeting().get("hb_cache_id"));

        ReflectionUtils.handleGamCustomTargetingUpdate(request, null);
        assertEquals(1, request.getCustomTargeting().size());
        assertTrue(request.getCustomTargeting().containsKey("Key"));
        assertEquals("Value", request.getCustomTargeting().get("Key"));
    }

    @Test
    public void setResponseToMoPubLocalExtrasWithMoPubBannerOrInterstitialView_ResponseStoredInLocalExtras() {
        MoPubView moPubView = new MoPubView(mActivity);
        BidResponse response = new BidResponse("{\"response\":\"test\", \"id\":\"1234\"}");
        Map<String, Object> expectedLocalExtras = Collections.singletonMap(KEY_BID_RESPONSE, response.getId());

        ReflectionUtils.setResponseIdToMoPubLocalExtras(moPubView, response);

        assertEquals(expectedLocalExtras, moPubView.getLocalExtras());
    }

    @Test
    public void setResponseToMoPubLocalExtrasWithNonMoPubView_DoNothing() {
        View mockView = mock(View.class);

        ReflectionUtils.setResponseIdToMoPubLocalExtras(mockView, new BidResponse("{}"));

        verifyZeroInteractions(mockView);
    }

    @Test
    public void isMoPubBannerViewWithMoPubBannerView_ReturnTrue() {
        assertTrue(ReflectionUtils.isMoPubBannerView(new MoPubView(mActivity)));
    }

    @Test
    public void isMoPubBannerViewWithMoPubInterstitialView_ReturnFalse() {
        assertFalse(ReflectionUtils.isMoPubBannerView(new MoPubInterstitial(mActivity, "1234")));
    }

    @Test
    public void isMoPubInterstitialViewWithMoPubInterstitialView_ReturnTrue() {
        assertTrue(ReflectionUtils.isMoPubInterstitialView(new MoPubInterstitial(mActivity, "1234")));
    }

    @Test
    public void isMoPubInterstitialViewWithMoPubBannerView_ReturnFalse() {
        assertFalse(ReflectionUtils.isMoPubInterstitialView(new MoPubView(mActivity)));
    }

    @Test
    public void isGamAdRequestWithGamAd_ReturnFalse() {
        assertFalse(ReflectionUtils.isGamAdRequest(new PublisherAdView(mActivity)));
    }

    @Test
    public void isGamAdRequestWithPublisherAdRequest_ReturnTrue() {
        assertTrue(ReflectionUtils.isGamAdRequest(new PublisherAdRequest.Builder().build()));
    }
}