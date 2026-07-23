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

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;

import org.prebid.mobile.rendering.networking.parameters.UserParameters;
import org.prebid.mobile.rendering.sdk.BaseManager;

public final class NetworkConnectionInfoManager extends BaseManager implements ConnectionInfoManager {
    private ConnectivityManager connectivityManager;

    public NetworkConnectionInfoManager(Context context) {
        super(context);

        if (getContext() != null) {
            connectivityManager = (ConnectivityManager) getContext().getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public UserParameters.ConnectionType getConnectionType() {
        if (getContext() == null || connectivityManager == null || !isNetworkStatePermissionGranted()) {
            return UserParameters.ConnectionType.OFFLINE;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return getConnectionTypeFromNetworkCapabilities();
        }

        return getConnectionTypeFromNetworkInfo();
    }

    private boolean isNetworkStatePermissionGranted() {
        return getContext().checkCallingOrSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED;
    }

    private UserParameters.ConnectionType getConnectionTypeFromNetworkCapabilities() {
        Network activeNetwork = connectivityManager.getActiveNetwork();
        if (activeNetwork == null) {
            return UserParameters.ConnectionType.OFFLINE;
        }

        NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork);
        if (networkCapabilities == null
            || !networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            || !networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
            return UserParameters.ConnectionType.OFFLINE;
        }

        return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
               ? UserParameters.ConnectionType.CELL
               : UserParameters.ConnectionType.WIFI;
    }

    @SuppressWarnings("deprecation")
    private UserParameters.ConnectionType getConnectionTypeFromNetworkInfo() {
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        UserParameters.ConnectionType result = UserParameters.ConnectionType.OFFLINE;
        if (info != null && info.isConnected()) {
            int netType = info.getType();
            boolean isMobile = netType == ConnectivityManager.TYPE_MOBILE
                               || netType == ConnectivityManager.TYPE_MOBILE_DUN
                               || netType == ConnectivityManager.TYPE_MOBILE_HIPRI
                               || netType == ConnectivityManager.TYPE_MOBILE_MMS
                               || netType == ConnectivityManager.TYPE_MOBILE_SUPL;
            result = isMobile ? UserParameters.ConnectionType.CELL : UserParameters.ConnectionType.WIFI;
        }

        return result;
    }
}