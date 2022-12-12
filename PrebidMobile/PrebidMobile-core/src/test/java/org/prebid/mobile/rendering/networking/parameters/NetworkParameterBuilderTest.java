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

package org.prebid.mobile.rendering.networking.parameters;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.reflection.sdk.ManagersResolverReflection;
import org.prebid.mobile.rendering.models.openrtb.BidRequest;
import org.prebid.mobile.rendering.sdk.ManagersResolver;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowConnectivityManager;
import org.robolectric.shadows.ShadowTelephonyManager;

@RunWith(RobolectricTestRunner.class)
public class NetworkParameterBuilderTest {

    private String NETWORK_CARRIER = "carrier";

    private ShadowTelephonyManager shadowTelephonyManager;
    private ShadowConnectivityManager shadowConnectivityManager;

    @Before
    public void setUp() throws Exception {
        Activity robolectricActivity = Robolectric.buildActivity(Activity.class).create().get();
        ShadowActivity shadowActivity = shadowOf(robolectricActivity);
        shadowActivity.grantPermissions("android.permission.ACCESS_NETWORK_STATE");
        shadowConnectivityManager = shadowOf((ConnectivityManager) robolectricActivity.getSystemService(Context.CONNECTIVITY_SERVICE));
        shadowTelephonyManager = shadowOf((TelephonyManager) robolectricActivity.getSystemService(Context.TELEPHONY_SERVICE));

        shadowTelephonyManager.setNetworkOperatorName(NETWORK_CARRIER);
        shadowTelephonyManager.setNetworkOperator(NETWORK_CARRIER);


        ManagersResolver resolver = ManagersResolver.getInstance();
        ManagersResolverReflection.resetManagers(resolver);
        resolver.prepare(robolectricActivity);
    }

    @Test
    public void whenTestAppendBuilderParametersAndWifi_WiFiType() throws JSONException {
        NetworkInfo mockInfo = mock(NetworkInfo.class);
        when(mockInfo.getType()).thenReturn(ConnectivityManager.TYPE_WIFI);
        when(mockInfo.isConnected()).thenReturn(true);
        shadowConnectivityManager.setActiveNetworkInfo(mockInfo);

        BidRequest expectedBidRequest = new BidRequest();
        expectedBidRequest.getDevice().mccmnc = "car-rier";
        expectedBidRequest.getDevice().carrier = NETWORK_CARRIER;
        expectedBidRequest.getDevice().connectiontype = NetworkParameterBuilder.CONNECTION_TYPE_WIFI;

        NetworkParameterBuilder builder = new NetworkParameterBuilder();
        AdRequestInput adRequestInput = new AdRequestInput();
        builder.appendBuilderParameters(adRequestInput);
        assertEquals(expectedBidRequest.getJsonObject().toString(),
                     adRequestInput.getBidRequest().getJsonObject().toString());
    }

    @Test
    public void whenTestAppendBuilderParametersAndCell_CellType() throws JSONException {
        NetworkInfo mockInfo = mock(NetworkInfo.class);
        when(mockInfo.getType()).thenReturn(ConnectivityManager.TYPE_MOBILE);
        when(mockInfo.isConnected()).thenReturn(true);
        shadowConnectivityManager.setActiveNetworkInfo(mockInfo);

        BidRequest expectedBidRequest = new BidRequest();
        expectedBidRequest.getDevice().mccmnc = "car-rier";
        expectedBidRequest.getDevice().carrier = NETWORK_CARRIER;
        expectedBidRequest.getDevice().connectiontype = NetworkParameterBuilder.CONNECTION_TYPE_CELL_UNKNOWN_G;

        NetworkParameterBuilder builder = new NetworkParameterBuilder();
        AdRequestInput adRequestInput = new AdRequestInput();
        builder.appendBuilderParameters(adRequestInput);
        assertEquals(expectedBidRequest.getJsonObject().toString(),
                     adRequestInput.getBidRequest().getJsonObject().toString());
    }
}