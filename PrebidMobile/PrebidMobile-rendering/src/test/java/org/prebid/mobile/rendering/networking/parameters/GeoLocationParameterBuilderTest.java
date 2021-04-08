package org.prebid.mobile.rendering.networking.parameters;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.rendering.models.openrtb.BidRequest;
import org.prebid.mobile.rendering.sdk.ManagersResolver;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowLocationManager;

import static org.junit.Assert.assertEquals;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
public class GeoLocationParameterBuilderTest {

    private final Double LATITUDE = 1.0;
    private final Double LONGITUDE = -1.0;

    @Before
    public void setUp() throws Exception {
        Activity robolectricActivity = Robolectric.buildActivity(Activity.class).create().get();
        ShadowActivity shadowActivity = shadowOf(robolectricActivity);
        shadowActivity.grantPermissions("android.permission.ACCESS_FINE_LOCATION");

        LocationManager locationManager = (LocationManager) robolectricActivity.getSystemService(Context.LOCATION_SERVICE);
        ShadowLocationManager shadowLocationManager = shadowOf(locationManager);
        Location location = new Location("");
        location.setLatitude(LATITUDE);
        location.setLongitude(LONGITUDE);
        shadowLocationManager.setLastKnownLocation("gps", location);

        ManagersResolver.getInstance().prepare(robolectricActivity);
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
}
