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

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.Host;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.rendering.sdk.deviceData.listeners.SdkInitListener;
import org.prebid.mobile.rendering.utils.helpers.AppInfoManager;
import org.prebid.mobile.test.utils.WhiteBox;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class PrebidMobileTest {

    @Before
    public void setUp() throws Exception {

        initAndroidVersion();
    }

    // Sets Build.VERSION.SDK_INT to LOLLIPOP(21) which prevents ProviderInstaller from execution
    private void initAndroidVersion() throws NoSuchFieldException, IllegalAccessException {
        Field versionField = (Build.VERSION.class.getField("SDK_INT"));
        versionField.setAccessible(true);

        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(versionField, versionField.getModifiers() & ~Modifier.FINAL);

        versionField.set(null, Build.VERSION_CODES.LOLLIPOP);
    }

    @After
    public void tearDown() throws Exception {
        PrebidMobile.setStoredAuctionResponse(null);
        PrebidMobile.clearStoredBidResponses();
    }

    @Test
    public void testGetDeviceName() throws Exception {
        assertEquals("Unknown robolectric", AppInfoManager.getDeviceName());
    }

    @Test
    public void testOnSDKInitWithoutVideoPreCache() throws Exception {
        //test if sdkinit is sent even if precache fails for any reason, as it is optional & should not avoid further sdk actions
        WhiteBox.field(PrebidMobile.class, "isSdkInitialized").set(null, false);
        Context context = Robolectric.buildActivity(Activity.class).create().get();
        SdkInitListener mockSdkInitListener = mock(SdkInitListener.class);

        PrebidMobile.setApplicationContext(context, mockSdkInitListener);
        verify(mockSdkInitListener, times(1)).onSDKInit();
    }

    @Test
    public void setBidServerHost_nullValue_ReturnDefaultBidServerHost() {
        String expected = PrebidMobile.getPrebidServerHost().getHostUrl();

        PrebidMobile.setPrebidServerHost(null);
        assertEquals(expected, PrebidMobile.getPrebidServerHost().getHostUrl());
    }

    @Test
    public void setBidServerHost_validValue_ReturnModifiedHost() {
        final String hostUrl = "http://customserver.com";
        final Host host = Host.CUSTOM;
        host.setHostUrl(hostUrl);

        PrebidMobile.setPrebidServerHost(host);

        assertEquals(host, PrebidMobile.getPrebidServerHost());
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