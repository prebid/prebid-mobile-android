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

package org.prebid.mobile.rendering.sdk;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.annotation.LooperMode.Mode.LEGACY;
import static java.lang.Thread.sleep;
import static android.os.Looper.getMainLooper;

import android.app.Activity;
import android.content.Context;
import android.os.Build;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.reflection.sdk.PrebidMobileReflection;
import org.prebid.mobile.rendering.listeners.SdkInitializationListener;
import org.prebid.mobile.rendering.utils.helpers.AppInfoManager;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.LooperMode;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

@RunWith(RobolectricTestRunner.class)
@LooperMode(LEGACY)
public class PrebidMobileTest {

    private MockWebServer server;

    @Before
    public void setUp() throws Exception {
        server = new MockWebServer();

        initAndroidVersion();
    }

    @After
    public void tearDown() throws IOException {
        server.shutdown();

        PrebidMobile.setStoredAuctionResponse(null);
        PrebidMobile.clearStoredBidResponses();
    }

    // Sets Build.VERSION.SDK_INT to LOLLIPOP(21) which prevents ProviderInstaller from execution
    private void initAndroidVersion() throws NoSuchFieldException, IllegalAccessException {
        Field versionField = (Build.VERSION.class.getField("SDK_INT"));
        versionField.setAccessible(true);

        versionField.set(null, Build.VERSION_CODES.LOLLIPOP);
    }

    @Test
    public void testGetDeviceName() throws Exception {
        assertEquals("Unknown robolectric", AppInfoManager.getDeviceName());
    }

    @Test
    public void testOnSDKInitWithoutVideoPreCache() throws Exception {
        PrebidMobileReflection.setFlagsThatSdkIsNotInitialized();
        PrebidContextHolder.clearContext();

        //test if sdkinit is sent even if precache fails for any reason, as it is optional & should not avoid further sdk actions

        MockResponse mockResponse = new MockResponse();
        mockResponse.setResponseCode(200);
        mockResponse.setBody("{\n    \"application\": {\n        \"status\": \"ok\"\n    }\n}");
        server.enqueue(mockResponse);
        server.start();
        HttpUrl url = server.url("/status");
        server.setProtocolNegotiationEnabled(false);

        Context context = Robolectric.buildActivity(Activity.class).create().get();
        SdkInitializationListener mockSdkInitListener = mock(SdkInitializationListener.class);
        PrebidMobile.initializeSdk(context, url.toString().replace("/status", "/openrtb2/auction"), mockSdkInitListener);

        sleep(3_000);
        shadowOf(getMainLooper()).idle();
        sleep(1_000);
        shadowOf(getMainLooper()).idle();

        verify(mockSdkInitListener, times(1)).onInitializationComplete(any());
    }

    @Test
    public void setStoreAuctionResponse_EqualsGetStoredAuctionResponse() {
        final String expected = "11111";
        PrebidMobile.setStoredAuctionResponse(expected);
        assertEquals(expected, PrebidMobile.getStoredAuctionResponse());
    }

    @Test
    public void addAndClearStoredBidResponseMap_ReturnExpectedResult() {
        Map<String, String> expectedMap = new LinkedHashMap<>();
        expectedMap.put("bidder1", "1111");
        expectedMap.put("bidder2", "2222");

        PrebidMobile.addStoredBidResponse("bidder1", "1111");
        PrebidMobile.addStoredBidResponse("bidder2", "2222");

        assertEquals(expectedMap, PrebidMobile.getStoredBidResponses());

        PrebidMobile.clearStoredBidResponses();

        assertTrue(PrebidMobile.getStoredBidResponses().isEmpty());
    }

}