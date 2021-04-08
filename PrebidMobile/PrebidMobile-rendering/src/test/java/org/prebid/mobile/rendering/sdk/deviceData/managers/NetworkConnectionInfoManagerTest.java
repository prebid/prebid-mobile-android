package org.prebid.mobile.rendering.sdk.deviceData.managers;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.rendering.networking.parameters.UserParameters;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class NetworkConnectionInfoManagerTest {

    private NetworkConnectionInfoManager mNetworkConnectionManager;
    private Context mMockContext;
    private ConnectivityManager mConnectivityManager;

    @Before
    public void setUp() throws Exception {
        mNetworkConnectionManager = new NetworkConnectionInfoManager();
        mMockContext = mock(Context.class);
        mConnectivityManager = mock(ConnectivityManager.class);
        when(mMockContext.getApplicationContext()).thenReturn(mMockContext);
        when(mMockContext.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(mConnectivityManager);

        mNetworkConnectionManager.init(mMockContext);
    }

    @Test
    public void getConnectionTypeTest() {
        NetworkInfo mockInfo = mock(NetworkInfo.class);

        when(mMockContext.checkCallingOrSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE)).thenReturn(PackageManager.PERMISSION_GRANTED);
        when(mockInfo.isConnected()).thenReturn(true);
        when(mockInfo.getType()).thenReturn(ConnectivityManager.TYPE_MOBILE);
        when(mConnectivityManager.getActiveNetworkInfo()).thenReturn(mockInfo);

        assertEquals(UserParameters.OXMConnectionType.CELL, mNetworkConnectionManager.getConnectionType());
    }
}