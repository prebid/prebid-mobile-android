/*
 *    Copyright 2018-2019 Prebid.org, Inc.
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

package org.prebid.mobile;

import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.mopub.mobileads.MoPubInterstitial;
import com.mopub.mobileads.MoPubView;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.testutils.BaseSetup;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashMap;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;


@RunWith(RobolectricTestRunner.class)
@Config(sdk = BaseSetup.testSDK)
public class UtilTest extends BaseSetup {

    class TestObject {
        public String testString() {
            return "test";
        }
    }

    @Test
    public void testCallMethodOnObject() throws Exception {
        TestObject object = new TestObject();
        Object result = Util.callMethodOnObject(object, "testString");
        assertEquals("test", result);
    }

    @Test
    public void testGetClassFromString() throws Exception {
        assertEquals(MoPubView.class, Util.getClassFromString(Util.MOPUB_BANNER_VIEW_CLASS));
        assertEquals(MoPubInterstitial.class, Util.getClassFromString(Util.MOPUB_INTERSTITIAL_CLASS));
        assertEquals(PublisherAdRequest.class, Util.getClassFromString(Util.DFP_AD_REQUEST_CLASS));
    }

    @Test
    public void testApplyBidsToMoPubAdobject() throws Exception {
        MoPubView adView = new MoPubView(activity);
        adView.setKeywords("key1:value1,key2:value2");
        HashMap<String, String> bids = new HashMap<>();
        bids.put("hb_pb", "0.50");
        bids.put("hb_cache_id", "123456");
        Util.apply(bids, adView);
        String adViewKeywords = adView.getKeywords();
        assertEquals("hb_pb:0.50,hb_cache_id:123456,key1:value1,key2:value2", adViewKeywords);
        Util.apply(null, adView);
        assertEquals("key1:value1,key2:value2", adView.getKeywords());
        MoPubInterstitial instl = new MoPubInterstitial(activity, "123456");
        instl.setKeywords("key1:value1,key2:value2");
        Util.apply(bids, instl);
        assertEquals("hb_pb:0.50,hb_cache_id:123456,key1:value1,key2:value2", instl.getKeywords());
        Util.apply(null, instl);
        assertEquals("key1:value1,key2:value2", instl.getKeywords());
    }

    @Test
    public void testApplyBidsToDFOAdObject() throws Exception {
        PublisherAdRequest.Builder builder = new PublisherAdRequest.Builder();
        builder.addCustomTargeting("Key", "Value");
        HashMap<String, String> bids = new HashMap<>();
        bids.put("hb_pb", "0.50");
        bids.put("hb_cache_id", "123456");
        PublisherAdRequest request = builder.build();
        Util.apply(bids, request);
        assertEquals(3, request.getCustomTargeting().size());
        assertTrue(request.getCustomTargeting().containsKey("Key"));
        assertEquals("Value", request.getCustomTargeting().get("Key"));
        assertTrue(request.getCustomTargeting().containsKey("hb_pb"));
        assertEquals("0.50", request.getCustomTargeting().get("hb_pb"));
        assertTrue(request.getCustomTargeting().containsKey("hb_cache_id"));
        assertEquals("123456", request.getCustomTargeting().get("hb_cache_id"));
        Util.apply(null, request);
        assertEquals(1, request.getCustomTargeting().size());
        assertTrue(request.getCustomTargeting().containsKey("Key"));
        assertEquals("Value", request.getCustomTargeting().get("Key"));
    }

    @Test
    public void testSupportedAdObject() throws Exception {
        MoPubView testView = new MoPubView(activity);
        assertTrue(Util.supportedAdObject(testView));
        assertFalse(Util.supportedAdObject(null));
        MoPubInterstitial interstitial = new MoPubInterstitial(activity, "");
        assertTrue(Util.supportedAdObject(interstitial));
        PublisherAdRequest request = new PublisherAdRequest.Builder().build();
        assertTrue(Util.supportedAdObject(request));
        Object object = new Object();
        assertFalse(Util.supportedAdObject(object));
    }
}
