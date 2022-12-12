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

import android.content.Context;

import org.prebid.mobile.rendering.sdk.deviceData.managers.ConnectionInfoManager;
import org.prebid.mobile.rendering.sdk.deviceData.managers.DeviceInfoImpl;
import org.prebid.mobile.rendering.sdk.deviceData.managers.DeviceInfoManager;
import org.prebid.mobile.rendering.sdk.deviceData.managers.LastKnownLocationInfoManager;
import org.prebid.mobile.rendering.sdk.deviceData.managers.LocationInfoManager;
import org.prebid.mobile.rendering.sdk.deviceData.managers.NetworkConnectionInfoManager;
import org.prebid.mobile.rendering.sdk.deviceData.managers.UserConsentManager;
import org.prebid.mobile.rendering.utils.helpers.Utils;

/**
 * Managers resolver supply ability to obtain a registered manager and use it
 * respectively.
 */
public class ManagersResolver {

    private DeviceInfoManager deviceManager;
    private LocationInfoManager locationManager;
    private ConnectionInfoManager connectionManager;
    private UserConsentManager userConsentManager;


    private ManagersResolver() {
    }

    /**
     * Gets the singleton instance of ManagersResolver.
     */
    public static ManagersResolver getInstance() {
        return ManagersResolverHolder.instance;
    }

    private static class ManagersResolverHolder {
        public static final ManagersResolver instance = new ManagersResolver();
    }


    /**
     * Prepare managers.
     */
    public void prepare(Context context) {
        //Try with application context or activity context
        //MOB-2205 [Research] on how we can eliminate activity context from Native ads.
        Utils.DENSITY = context.getResources().getDisplayMetrics().density;

        if (deviceManager == null) {
            deviceManager = new DeviceInfoImpl(context);
        }

        if (locationManager == null) {
            locationManager = new LastKnownLocationInfoManager(context);
        }

        if (connectionManager == null) {
            connectionManager = new NetworkConnectionInfoManager(context);
        }

        if (userConsentManager == null) {
            userConsentManager = new UserConsentManager(context);
        }
    }

    /**
     * Obtains the device manager.
     */
    public DeviceInfoManager getDeviceManager() {
        return deviceManager;
    }

    /**
     * Obtains the location manager.
     */
    public LocationInfoManager getLocationManager() {
        return locationManager;
    }

    /**
     * Obtains the network manager.
     */
    public ConnectionInfoManager getNetworkManager() {
        return connectionManager;
    }

    /**
     * Obtains the UserConsent manager.
     */
    public UserConsentManager getUserConsentManager() {
        return userConsentManager;
    }

}