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

package org.prebid.mobile.rendering.networking.urlBuilder;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.telephony.TelephonyManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.models.openrtb.BidRequest;
import org.prebid.mobile.rendering.networking.parameters.*;
import org.prebid.mobile.rendering.sdk.ManagersResolver;
import org.prebid.mobile.rendering.utils.helpers.AdIdManager;
import org.prebid.mobile.rendering.utils.helpers.AppInfoManager;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowLocationManager;
import org.robolectric.shadows.ShadowTelephonyManager;

import java.util.ArrayList;

import static org.junit.Assert.assertNotEquals;
import static org.robolectric.Shadows.shadowOf;

/**
 * These tests check that certain values in the BidRequest supplied by the publisher are
 * overwritten by auto-detected values during the URL building stage of an ad request
 */

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19, qualifiers = "w1dp-h1dp")
public class AutoDetectedOpenRtbTest {

    private ArrayList<ParameterBuilder> paramBuilderArray;

    private Activity activity;
    private AdRequestInput originalAdRequestInput;
    private BidRequest originalOpenRtbParams;

    @Before
    public void setup() {
        activity = Robolectric.buildActivity(Activity.class).create().get();
        ShadowActivity shadowActivity = shadowOf(activity);
        shadowActivity.grantPermissions("android.permission.ACCESS_FINE_LOCATION");

        LocationManager locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        ShadowLocationManager shadowLocationManager = shadowOf(locationManager);
        Location location = new Location("");
        location.setLatitude(1);
        location.setLongitude(1);
        shadowLocationManager.setLastKnownLocation("gps", location);

        ShadowTelephonyManager shadowTelephonyManager = shadowOf((TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE));
        shadowTelephonyManager.setNetworkOperatorName("carrier");
        shadowTelephonyManager.setNetworkOperator("carrier");

        ManagersResolver.getInstance().prepare(activity);

        paramBuilderArray = new ArrayList<>();
        originalAdRequestInput = new AdRequestInput();
        originalOpenRtbParams = new BidRequest();
    }

    @Test
    public void overwrittenGeoLocationParameterBuilderTest() {
        originalOpenRtbParams.getDevice().getGeo().lat = 0f;
        originalOpenRtbParams.getDevice().getGeo().lon = 0f;
        originalOpenRtbParams.getDevice().getGeo().type = 0;
        originalAdRequestInput.setBidRequest(originalOpenRtbParams);

        paramBuilderArray.add(new GeoLocationParameterBuilder());
        AdRequestInput newAdRequestInput = URLBuilder.buildParameters(paramBuilderArray, originalAdRequestInput);
        BidRequest newOpenRtbParams = newAdRequestInput.getBidRequest();

        assertNotEquals(originalOpenRtbParams.getDevice().getGeo().lat, newOpenRtbParams.getDevice().getGeo().lat);
        assertNotEquals(originalOpenRtbParams.getDevice().getGeo().lon, newOpenRtbParams.getDevice().getGeo().lon);
        assertNotEquals(originalOpenRtbParams.getDevice().getGeo().type, newOpenRtbParams.getDevice().getGeo().type);
    }

    @Test
    public void overwrittenAppInfoParameterBuilder() {
        originalOpenRtbParams.getApp().name = "foo";
        originalOpenRtbParams.getApp().bundle = "foo";
        originalOpenRtbParams.getDevice().ifa = "foo";
        originalOpenRtbParams.getDevice().lmt = 0;

        AppInfoManager.setAppName("bar");
        AppInfoManager.setPackageName("bar");
        AdIdManager.setAdId("bar");
        AdIdManager.setLimitAdTrackingEnabled(true);

        paramBuilderArray.add(new AppInfoParameterBuilder(new AdUnitConfiguration()));
        AdRequestInput newAdRequestInput = URLBuilder.buildParameters(paramBuilderArray, originalAdRequestInput);
        BidRequest newOpenRtbParams = newAdRequestInput.getBidRequest();

        assertNotEquals(originalOpenRtbParams.getApp().name, newOpenRtbParams.getApp().name);
        assertNotEquals(originalOpenRtbParams.getApp().bundle, newOpenRtbParams.getApp().bundle);
        assertNotEquals(originalOpenRtbParams.getDevice().ifa, newOpenRtbParams.getDevice().ifa);
        assertNotEquals(originalOpenRtbParams.getDevice().lmt, newOpenRtbParams.getDevice().lmt);
    }

    @Test
    public void overwrittenDeviceInfoParameterBuilder() {
        originalOpenRtbParams.getDevice().dpidmd5 = "foo";
        originalOpenRtbParams.getDevice().dpidsha1 = "foo";
        originalOpenRtbParams.getDevice().w = 0;
        originalOpenRtbParams.getDevice().h = 0;

        paramBuilderArray.add(new DeviceInfoParameterBuilder(new AdUnitConfiguration()));
        AdRequestInput newAdRequestInput = URLBuilder.buildParameters(paramBuilderArray, originalAdRequestInput);
        BidRequest newOpenRtbParams = newAdRequestInput.getBidRequest();

        assertNotEquals(originalOpenRtbParams.getDevice().dpidmd5, newOpenRtbParams.getDevice().dpidmd5);
        assertNotEquals(originalOpenRtbParams.getDevice().dpidsha1, newOpenRtbParams.getDevice().dpidsha1);
        assertNotEquals(originalOpenRtbParams.getDevice().w, newOpenRtbParams.getDevice().w);
        assertNotEquals(originalOpenRtbParams.getDevice().h, newOpenRtbParams.getDevice().h);
    }

    @Test
    public void overwrittenNetworkParameterBuilder() {
        originalOpenRtbParams.getDevice().mccmnc = "foo";
        originalOpenRtbParams.getDevice().carrier = "foo";
        originalOpenRtbParams.getDevice().connectiontype = 0;

        paramBuilderArray.add(new NetworkParameterBuilder());
        AdRequestInput newAdRequestInput = URLBuilder.buildParameters(paramBuilderArray, originalAdRequestInput);
        BidRequest newOpenRtbParams = newAdRequestInput.getBidRequest();

        assertNotEquals(originalOpenRtbParams.getDevice().mccmnc, newOpenRtbParams.getDevice().mccmnc);
        assertNotEquals(originalOpenRtbParams.getDevice().carrier, newOpenRtbParams.getDevice().carrier);
        assertNotEquals(originalOpenRtbParams.getDevice().connectiontype, newOpenRtbParams.getDevice().connectiontype);
    }
}
