package com.openx.apollo.networking.parameters;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

import com.openx.apollo.models.openrtb.BidRequest;
import com.openx.apollo.sdk.ManagersResolver;

import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowConnectivityManager;
import org.robolectric.shadows.ShadowTelephonyManager;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
public class NetworkParameterBuilderTest {

    private String NETWORK_CARRIER = "carrier";

    private ShadowTelephonyManager mShadowTelephonyManager;
    private ShadowConnectivityManager mShadowConnectivityManager;

    @Before
    public void setUp() throws Exception {
        Activity robolectricActivity = Robolectric.buildActivity(Activity.class).create().get();
        ShadowActivity shadowActivity = shadowOf(robolectricActivity);
        shadowActivity.grantPermissions("android.permission.ACCESS_NETWORK_STATE");
        mShadowConnectivityManager = shadowOf((ConnectivityManager) robolectricActivity.getSystemService(Context.CONNECTIVITY_SERVICE));
        mShadowTelephonyManager = shadowOf((TelephonyManager) robolectricActivity.getSystemService(Context.TELEPHONY_SERVICE));

        mShadowTelephonyManager.setNetworkOperatorName(NETWORK_CARRIER);
        mShadowTelephonyManager.setNetworkOperator(NETWORK_CARRIER);

        ManagersResolver.getInstance().prepare(robolectricActivity);
    }

    @Test
    public void whenTestAppendBuilderParametersAndWifi_WiFiType() throws JSONException {
        NetworkInfo mockInfo = mock(NetworkInfo.class);
        when(mockInfo.getType()).thenReturn(ConnectivityManager.TYPE_WIFI);
        when(mockInfo.isConnected()).thenReturn(true);
        mShadowConnectivityManager.setActiveNetworkInfo(mockInfo);

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
        mShadowConnectivityManager.setActiveNetworkInfo(mockInfo);

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