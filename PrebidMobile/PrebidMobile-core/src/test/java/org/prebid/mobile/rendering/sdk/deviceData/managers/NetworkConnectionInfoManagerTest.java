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

package org.prebid.mobile.rendering.sdk.deviceData.managers;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class NetworkConnectionInfoManagerTest {

    private NetworkConnectionInfoManager networkConnectionManager;
    private Context mockContext;
    private ConnectivityManager connectivityManager;

    @Before
    public void setUp() throws Exception {
        mockContext = mock(Context.class);
        connectivityManager = mock(ConnectivityManager.class);
        when(mockContext.getApplicationContext()).thenReturn(mockContext);
        when(mockContext.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(connectivityManager);

        networkConnectionManager = new NetworkConnectionInfoManager(mockContext);
    }

    @Test
    public void getConnectionTypeTest() {
        NetworkInfo mockInfo = mock(NetworkInfo.class);

        when(mockContext.checkCallingOrSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE)).thenReturn(
                PackageManager.PERMISSION_GRANTED);
        when(mockInfo.isConnected()).thenReturn(true);
        when(mockInfo.getType()).thenReturn(ConnectivityManager.TYPE_MOBILE);
        when(connectivityManager.getActiveNetworkInfo()).thenReturn(mockInfo);

        assertEquals(UserParameters.ConnectionType.CELL, networkConnectionManager.getConnectionType());
    }
}