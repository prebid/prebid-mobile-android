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
import static org.junit.Assert.assertNull;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.telephony.TelephonyManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.GeoCountryFormat;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.TargetingParams;
import org.prebid.mobile.reflection.sdk.ManagersResolverReflection;
import org.prebid.mobile.rendering.models.openrtb.BidRequest;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.devices.Geo;
import org.prebid.mobile.rendering.sdk.ManagersResolver;
import org.prebid.mobile.rendering.sdk.PrebidContextHolder;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowLocationManager;
import org.robolectric.shadows.ShadowTelephonyManager;

@RunWith(RobolectricTestRunner.class)
public class GeoLocationParameterBuilderTest {

    private final Double LATITUDE = 1.0;
    private final Double LONGITUDE = -1.0;

    private Activity robolectricActivity;

    @Before
    public void setUp() throws Exception {
        PrebidMobile.setShareGeoLocation(true);
        robolectricActivity = Robolectric.buildActivity(Activity.class).create().get();
        ShadowActivity shadowActivity = shadowOf(robolectricActivity);
        shadowActivity.grantPermissions("android.permission.ACCESS_FINE_LOCATION");

        LocationManager locationManager = (LocationManager) robolectricActivity.getSystemService(Context.LOCATION_SERVICE);
        ShadowLocationManager shadowLocationManager = shadowOf(locationManager);
        Location location = new Location("");
        location.setLatitude(LATITUDE);
        location.setLongitude(LONGITUDE);
        shadowLocationManager.setLastKnownLocation("gps", location);

        ManagersResolver resolver = ManagersResolver.getInstance();
        ManagersResolverReflection.resetManagers(resolver);
        resolver.prepare(robolectricActivity);
    }

    @Test
    public void testAppendBuilderParameters() throws Exception {
        GeoLocationParameterBuilder builder = new GeoLocationParameterBuilder();
        AdRequestInput adRequestInput = new AdRequestInput();
        builder.appendBuilderParameters(adRequestInput);

        BidRequest expectedBidRequest = new BidRequest();
        expectedBidRequest.getDevice().getGeo().lat = LATITUDE.floatValue();
        expectedBidRequest.getDevice().getGeo().lon = LONGITUDE.floatValue();
        expectedBidRequest.getDevice().getGeo().type = GeoLocationParameterBuilder.LOCATION_SOURCE_GPS;

        assertEquals(expectedBidRequest.getJsonObject().toString(),
                     adRequestInput.getBidRequest().getJsonObject().toString());
    }

    /**
     * Ignoring publisher values for geographic data, even if the SDK fails to auto-detect values
     */
    @Test
    public void testIgnorePublisherValues() throws Exception {
        BidRequest ignoredBidRequest = new BidRequest();
        ignoredBidRequest.getDevice().getGeo().lat = 100f;
        ignoredBidRequest.getDevice().getGeo().lon = 200f;
        ignoredBidRequest.getDevice().getGeo().type = GeoLocationParameterBuilder.LOCATION_SOURCE_GPS;

        GeoLocationParameterBuilder builder = new GeoLocationParameterBuilder();
        AdRequestInput adRequestInput = new AdRequestInput();
        adRequestInput.setBidRequest(ignoredBidRequest);
        builder.appendBuilderParameters(adRequestInput);

        assertEquals("{\"lat\":1,\"lon\":-1,\"type\":1}", adRequestInput.getBidRequest().getDevice().getGeo().getJsonObject().toString());
    }

    @Test
    public void testDoNotSendGeoWhenGeoFlagIsFalse() throws Exception {
        PrebidMobile.setShareGeoLocation(false);

        GeoLocationParameterBuilder builder = new GeoLocationParameterBuilder();
        AdRequestInput adRequestInput = new AdRequestInput();
        builder.appendBuilderParameters(adRequestInput);

        assertEquals(new Geo().getJsonObject().toString(), adRequestInput.getBidRequest().getDevice().getGeo().getJsonObject().toString());
    }

    @Test
    public void testLocationPrecisionApplied() throws Exception {
        // Set precision to 2 decimal places
        TargetingParams.setLocationDecimalPrecision(2);
        
        GeoLocationParameterBuilder builder = new GeoLocationParameterBuilder();
        AdRequestInput adRequestInput = new AdRequestInput();
        builder.appendBuilderParameters(adRequestInput);

        // Verify that coordinates are rounded to 2 decimal places
        assertEquals(1.00f, adRequestInput.getBidRequest().getDevice().getGeo().lat, 0.001f);
        assertEquals(-1.00f, adRequestInput.getBidRequest().getDevice().getGeo().lon, 0.001f);
    }

    @Test
    public void testLocationPrecisionNoLimit() throws Exception {
        // Set precision to no limit (null)
        TargetingParams.setLocationDecimalPrecision(null);
        
        GeoLocationParameterBuilder builder = new GeoLocationParameterBuilder();
        AdRequestInput adRequestInput = new AdRequestInput();
        builder.appendBuilderParameters(adRequestInput);

        // Verify that coordinates maintain full precision
        assertEquals(LATITUDE.floatValue(), adRequestInput.getBidRequest().getDevice().getGeo().lat, 0.0f);
        assertEquals(LONGITUDE.floatValue(), adRequestInput.getBidRequest().getDevice().getGeo().lon, 0.0f);
    }

    @Test
    public void testToAlpha3_convertsAlpha2AndPassesThroughAlpha3() {
        // Telephony (getSimCountryIso/getNetworkCountryIso) and Geocoder return
        // alpha-2; oRTB device.geo.country wants alpha-3. Full ISO coverage.
        assertEquals("USA", GeoLocationParameterBuilder.toAlpha3("US"));
        assertEquals("USA", GeoLocationParameterBuilder.toAlpha3("us"));
        assertEquals("GBR", GeoLocationParameterBuilder.toAlpha3("GB"));
        assertEquals("CAN", GeoLocationParameterBuilder.toAlpha3("CA"));
        // Already valid alpha-3 -> unchanged (idempotent).
        assertEquals("USA", GeoLocationParameterBuilder.toAlpha3("USA"));
        // Empty / null / unknown / non-ISO -> null so the field is omitted (not
        // sent as "" or a malformed value).
        assertNull(GeoLocationParameterBuilder.toAlpha3(""));
        assertNull(GeoLocationParameterBuilder.toAlpha3(null));
        assertNull(GeoLocationParameterBuilder.toAlpha3("ZZ"));   // unknown alpha-2
        assertNull(GeoLocationParameterBuilder.toAlpha3("419"));  // UN M.49, not ISO alpha-3
        assertNull(GeoLocationParameterBuilder.toAlpha3("XK"));   // Kosovo — not in the JDK ISO tables
    }

    @Test
    public void geoCountryFormat_flagSetGetAndNullResetsToDefault() {
        try {
            PrebidMobile.setGeoCountryFormat(GeoCountryFormat.ALPHA3);
            assertEquals(GeoCountryFormat.ALPHA3, PrebidMobile.getGeoCountryFormat());
            // null resets to the default (alpha-2, today's behavior).
            PrebidMobile.setGeoCountryFormat(null);
            assertEquals(GeoCountryFormat.ALPHA2, PrebidMobile.getGeoCountryFormat());
        } finally {
            PrebidMobile.setGeoCountryFormat(GeoCountryFormat.ALPHA2);
        }
    }

    @Test
    public void testCountryDefaultFormat_sendsAlpha2() throws Exception {
        try {
            setUpTelephonyCountry("us");

            GeoLocationParameterBuilder builder = new GeoLocationParameterBuilder();
            AdRequestInput adRequestInput = new AdRequestInput();
            builder.appendBuilderParameters(adRequestInput);

            assertEquals("US", adRequestInput.getBidRequest().getDevice().getGeo().country);
        } finally {
            PrebidContextHolder.clearContext();
        }
    }

    @Test
    public void testCountryAlpha3Format_sendsAlpha3() throws Exception {
        try {
            PrebidMobile.setGeoCountryFormat(GeoCountryFormat.ALPHA3);
            setUpTelephonyCountry("us");

            GeoLocationParameterBuilder builder = new GeoLocationParameterBuilder();
            AdRequestInput adRequestInput = new AdRequestInput();
            builder.appendBuilderParameters(adRequestInput);

            assertEquals("USA", adRequestInput.getBidRequest().getDevice().getGeo().country);
        } finally {
            PrebidMobile.setGeoCountryFormat(GeoCountryFormat.ALPHA2);
            PrebidContextHolder.clearContext();
        }
    }

    private void setUpTelephonyCountry(String countryIso) {
        // The builder reads TelephonyManager from PrebidContextHolder.getContext(),
        // so shadow the instance of that same context.
        PrebidContextHolder.setContext(robolectricActivity);
        TelephonyManager telephonyManager = (TelephonyManager) robolectricActivity.getSystemService(Context.TELEPHONY_SERVICE);
        ShadowTelephonyManager shadowTelephonyManager = shadowOf(telephonyManager);
        shadowTelephonyManager.setSimCountryIso(countryIso);
        shadowTelephonyManager.setNetworkCountryIso(countryIso);
    }
}
