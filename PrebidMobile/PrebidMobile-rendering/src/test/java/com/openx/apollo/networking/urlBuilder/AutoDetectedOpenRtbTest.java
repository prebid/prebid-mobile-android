package com.openx.apollo.networking.urlBuilder;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.telephony.TelephonyManager;

import com.openx.apollo.models.AdConfiguration;
import com.openx.apollo.models.openrtb.BidRequest;
import com.openx.apollo.networking.parameters.AdRequestInput;
import com.openx.apollo.networking.parameters.AppInfoParameterBuilder;
import com.openx.apollo.networking.parameters.DeviceInfoParameterBuilder;
import com.openx.apollo.networking.parameters.GeoLocationParameterBuilder;
import com.openx.apollo.networking.parameters.NetworkParameterBuilder;
import com.openx.apollo.networking.parameters.ParameterBuilder;
import com.openx.apollo.sdk.ManagersResolver;
import com.openx.apollo.utils.helpers.AdIdManager;
import com.openx.apollo.utils.helpers.AppInfoManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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

    private ArrayList<ParameterBuilder> mParamBuilderArray;

    private Activity mActivity;
    private AdRequestInput mOriginalAdRequestInput;
    private BidRequest mOriginalOpenRtbParams;

    @Before
    public void setup() {
        mActivity = Robolectric.buildActivity(Activity.class).create().get();
        ShadowActivity shadowActivity = shadowOf(mActivity);
        shadowActivity.grantPermissions("android.permission.ACCESS_FINE_LOCATION");

        LocationManager locationManager = (LocationManager) mActivity.getSystemService(Context.LOCATION_SERVICE);
        ShadowLocationManager shadowLocationManager = shadowOf(locationManager);
        Location location = new Location("");
        location.setLatitude(1);
        location.setLongitude(1);
        shadowLocationManager.setLastKnownLocation("gps", location);

        ShadowTelephonyManager shadowTelephonyManager = shadowOf((TelephonyManager) mActivity.getSystemService(Context.TELEPHONY_SERVICE));
        shadowTelephonyManager.setNetworkOperatorName("carrier");
        shadowTelephonyManager.setNetworkOperator("carrier");

        ManagersResolver.getInstance().prepare(mActivity);

        mParamBuilderArray = new ArrayList<>();
        mOriginalAdRequestInput = new AdRequestInput();
        mOriginalOpenRtbParams = new BidRequest();
    }

    @Test
    public void overwrittenGeoLocationParameterBuilderTest() {
        mOriginalOpenRtbParams.getDevice().getGeo().lat = 0f;
        mOriginalOpenRtbParams.getDevice().getGeo().lon = 0f;
        mOriginalOpenRtbParams.getDevice().getGeo().type = 0;
        mOriginalAdRequestInput.setBidRequest(mOriginalOpenRtbParams);

        mParamBuilderArray.add(new GeoLocationParameterBuilder());
        AdRequestInput newAdRequestInput = URLBuilder.buildParameters(mParamBuilderArray, mOriginalAdRequestInput);
        BidRequest newOpenRtbParams = newAdRequestInput.getBidRequest();

        assertNotEquals(mOriginalOpenRtbParams.getDevice().getGeo().lat, newOpenRtbParams.getDevice().getGeo().lat);
        assertNotEquals(mOriginalOpenRtbParams.getDevice().getGeo().lon, newOpenRtbParams.getDevice().getGeo().lon);
        assertNotEquals(mOriginalOpenRtbParams.getDevice().getGeo().type, newOpenRtbParams.getDevice().getGeo().type);
    }

    @Test
    public void overwrittenAppInfoParameterBuilder() {
        mOriginalOpenRtbParams.getApp().name = "foo";
        mOriginalOpenRtbParams.getApp().bundle = "foo";
        mOriginalOpenRtbParams.getDevice().ifa = "foo";
        mOriginalOpenRtbParams.getDevice().lmt = 0;

        AppInfoManager.setAppName("bar");
        AppInfoManager.setPackageName("bar");
        AdIdManager.setAdId("bar");
        AdIdManager.setLimitAdTrackingEnabled(true);

        mParamBuilderArray.add(new AppInfoParameterBuilder());
        AdRequestInput newAdRequestInput = URLBuilder.buildParameters(mParamBuilderArray, mOriginalAdRequestInput);
        BidRequest newOpenRtbParams = newAdRequestInput.getBidRequest();

        assertNotEquals(mOriginalOpenRtbParams.getApp().name, newOpenRtbParams.getApp().name);
        assertNotEquals(mOriginalOpenRtbParams.getApp().bundle, newOpenRtbParams.getApp().bundle);
        assertNotEquals(mOriginalOpenRtbParams.getDevice().ifa, newOpenRtbParams.getDevice().ifa);
        assertNotEquals(mOriginalOpenRtbParams.getDevice().lmt, newOpenRtbParams.getDevice().lmt);
    }

    @Test
    public void overwrittenDeviceInfoParameterBuilder() {
        mOriginalOpenRtbParams.getDevice().dpidmd5 = "foo";
        mOriginalOpenRtbParams.getDevice().dpidsha1 = "foo";
        mOriginalOpenRtbParams.getDevice().w = 0;
        mOriginalOpenRtbParams.getDevice().h = 0;

        mParamBuilderArray.add(new DeviceInfoParameterBuilder(new AdConfiguration()));
        AdRequestInput newAdRequestInput = URLBuilder.buildParameters(mParamBuilderArray, mOriginalAdRequestInput);
        BidRequest newOpenRtbParams = newAdRequestInput.getBidRequest();

        assertNotEquals(mOriginalOpenRtbParams.getDevice().dpidmd5, newOpenRtbParams.getDevice().dpidmd5);
        assertNotEquals(mOriginalOpenRtbParams.getDevice().dpidsha1, newOpenRtbParams.getDevice().dpidsha1);
        assertNotEquals(mOriginalOpenRtbParams.getDevice().w, newOpenRtbParams.getDevice().w);
        assertNotEquals(mOriginalOpenRtbParams.getDevice().h, newOpenRtbParams.getDevice().h);
    }

    @Test
    public void overwrittenNetworkParameterBuilder() {
        mOriginalOpenRtbParams.getDevice().mccmnc = "foo";
        mOriginalOpenRtbParams.getDevice().carrier = "foo";
        mOriginalOpenRtbParams.getDevice().connectiontype = 0;

        mParamBuilderArray.add(new NetworkParameterBuilder());
        AdRequestInput newAdRequestInput = URLBuilder.buildParameters(mParamBuilderArray, mOriginalAdRequestInput);
        BidRequest newOpenRtbParams = newAdRequestInput.getBidRequest();

        assertNotEquals(mOriginalOpenRtbParams.getDevice().mccmnc, newOpenRtbParams.getDevice().mccmnc);
        assertNotEquals(mOriginalOpenRtbParams.getDevice().carrier, newOpenRtbParams.getDevice().carrier);
        assertNotEquals(mOriginalOpenRtbParams.getDevice().connectiontype, newOpenRtbParams.getDevice().connectiontype);
    }
}
