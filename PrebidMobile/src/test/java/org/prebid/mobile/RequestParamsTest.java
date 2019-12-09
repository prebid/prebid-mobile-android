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

import org.apache.commons.lang3.reflect.FieldUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.testutils.BaseSetup;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.HashSet;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = BaseSetup.testSDK)
public class RequestParamsTest {
    @Test
    public void testCreation() throws Exception {
        HashSet<AdSize> sizes = new HashSet<>();
        sizes.add(new AdSize(320, 50));
        RequestParams requestParams = new RequestParams("123456", AdType.BANNER, sizes);
        assertEquals("123456", FieldUtils.readField(requestParams, "configId", true));
        assertEquals(AdType.BANNER, FieldUtils.readField(requestParams, "adType", true));
        assertEquals(sizes, FieldUtils.readField(requestParams, "sizes", true));
        requestParams = new RequestParams("123456", AdType.INTERSTITIAL, null);
        assertEquals("123456", FieldUtils.readField(requestParams, "configId", true));
        assertEquals(AdType.INTERSTITIAL, FieldUtils.readField(requestParams, "adType", true));
        assertEquals(null, FieldUtils.readField(requestParams, "sizes", true));
    }

    @Test
    public void testCreationNative() throws  Exception {
        RequestParams requestParams = new RequestParams("123456", AdType.NATIVE, null);
        assertEquals("123456", FieldUtils.readField(requestParams, "configId", true));
        assertEquals(AdType.NATIVE, FieldUtils.readField(requestParams, "adType", true));
        assertEquals(null, FieldUtils.readField(requestParams, "sizes", true));
        NativeRequestParams nativeRequestParams = new NativeRequestParams();
        nativeRequestParams.setContextType(NativeAdUnit.CONTEXT_TYPE.SOCIAL_CENTRIC);
        nativeRequestParams.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.ARTICAL);
        nativeRequestParams.setPlacementType(NativeAdUnit.PLACEMENTTYPE.CONTENT_ATOMIC_UNIT);
        nativeRequestParams.setAUrlSupport(true);
        nativeRequestParams.setDUrlSupport(true);
        NativeTitleAsset title = new NativeTitleAsset();
        title.setLength(25);
        NativeImageAsset image = new NativeImageAsset();
        image.setWMin(20);
        image.setHMin(30);
        nativeRequestParams.addAsset(title);
        nativeRequestParams.addAsset(image);
        JSONObject ext = new JSONObject();
        try {
            ext.put("key", "value");
            ArrayList<NativeEventTracker.EVENT_TRACKING_METHOD> methods = new ArrayList<>();
            methods.add(NativeEventTracker.EVENT_TRACKING_METHOD.IMAGE);
            methods.add(NativeEventTracker.EVENT_TRACKING_METHOD.JS);
            NativeEventTracker eventTracker = new NativeEventTracker(NativeEventTracker.EVENT_TYPE.IMPRESSION, methods);
            nativeRequestParams.addEventTracker(eventTracker);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        nativeRequestParams.setExt(ext);

        requestParams.setNativeRequestParams(nativeRequestParams);

        assertEquals(requestParams.getNativeRequestParams().getContextType(), NativeAdUnit.CONTEXT_TYPE.SOCIAL_CENTRIC);
        assertEquals(requestParams.getNativeRequestParams().getContextsubtype(), NativeAdUnit.CONTEXTSUBTYPE.ARTICAL);
        assertEquals(requestParams.getNativeRequestParams().getPlacementType(), NativeAdUnit.PLACEMENTTYPE.CONTENT_ATOMIC_UNIT);
        assertEquals(requestParams.getNativeRequestParams().isAUrlSupport(), true);
        assertEquals(requestParams.getNativeRequestParams().isDUrlSupport(), true);
        assertNotNull(requestParams.getNativeRequestParams().getAssets());
        assertEquals(requestParams.getNativeRequestParams().getAssets().size(), 2);
        assertEquals(((NativeTitleAsset) requestParams.getNativeRequestParams().getAssets().get(0)).getLen(), 25);
        assertEquals(((NativeImageAsset) requestParams.getNativeRequestParams().getAssets().get(1)).getHMin(), 30);
        assertEquals(((NativeImageAsset) requestParams.getNativeRequestParams().getAssets().get(1)).getWMin(), 20);
        NativeEventTracker eventTracker = requestParams.getNativeRequestParams().getEventTrackers().get(0);
        assertEquals(eventTracker.event, NativeEventTracker.EVENT_TYPE.IMPRESSION);
        assertEquals(eventTracker.getMethods().size(), 2);
        assertEquals(eventTracker.getMethods().get(0), NativeEventTracker.EVENT_TRACKING_METHOD.IMAGE);
        assertEquals(eventTracker.getMethods().get(1), NativeEventTracker.EVENT_TRACKING_METHOD.JS);
        String value = "";
        try {
            JSONObject data = (JSONObject) requestParams.getNativeRequestParams().getExt();
            value = data.getString("key");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        assertEquals("value", value);
    }

    @Test
    public void testCreationWithAdditionalMap() throws Exception {
        HashSet<AdSize> sizes = new HashSet<>();
        sizes.add(new AdSize(500, 700));

        AdSize minSizePerc = new AdSize(50, 70);

        RequestParams requestParams = new RequestParams("123456", AdType.INTERSTITIAL, sizes, null, null, minSizePerc, null);

        AdSize minAdSizePerc = requestParams.getMinSizePerc();
        assertNotNull(minAdSizePerc);

        assertTrue(minAdSizePerc.getWidth() == 50 && minAdSizePerc.getHeight() == 70);
    }
}
