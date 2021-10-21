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

package org.prebid.mobile.mopub.adapters;

import android.app.Activity;
import android.view.View;

import com.mopub.mobileads.MoPubInterstitial;
import com.mopub.mobileads.MoPubView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.display.ReflectionUtils;
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
import static org.mockito.Mockito.verifyNoInteractions;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class ReflectionUtilsTest {
    private static final String KEY_BID_RESPONSE = "PREBID_BID_RESPONSE_ID";
    private Activity mActivity;

    public class TestObject {
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

        verifyNoInteractions(mockView);
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
}