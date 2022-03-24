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

import org.prebid.mobile.rendering.sdk.ManagersResolver;
import org.prebid.mobile.rendering.sdk.deviceData.managers.ConnectionInfoManager;
import org.prebid.mobile.rendering.sdk.deviceData.managers.DeviceInfoManager;
import org.prebid.mobile.rendering.utils.helpers.Utils;

public class NetworkParameterBuilder extends ParameterBuilder {

    static int CONNECTION_TYPE_WIFI = 2;
    static int CONNECTION_TYPE_CELL_UNKNOWN_G = 3;

    @Override
    public void appendBuilderParameters(AdRequestInput adRequestInput) {
        DeviceInfoManager deviceManager = ManagersResolver.getInstance().getDeviceManager();
        if (deviceManager != null) {
            String detectedMccMnc = deviceManager.getMccMnc();
            if (Utils.isNotBlank(detectedMccMnc)) {
                adRequestInput.getBidRequest().getDevice().mccmnc = detectedMccMnc;
            }

            String detectedCarrier = deviceManager.getCarrier();
            if (Utils.isNotBlank(detectedCarrier)) {
                adRequestInput.getBidRequest().getDevice().carrier = detectedCarrier;
            }
        }

        ConnectionInfoManager connectionInfoManager = ManagersResolver.getInstance().getNetworkManager();
        if (connectionInfoManager != null && deviceManager != null) {
            setNetworkParams(adRequestInput, deviceManager, connectionInfoManager);
        }
    }

    private void setNetworkParams(AdRequestInput adRequestInput, DeviceInfoManager deviceManager, ConnectionInfoManager connectionInfoManager) {
        if (deviceManager.isPermissionGranted("android.permission.ACCESS_NETWORK_STATE")) {
            UserParameters.ConnectionType autoDetectedValue = connectionInfoManager.getConnectionType();
            switch (autoDetectedValue) {
                case WIFI:
                    adRequestInput.getBidRequest().getDevice().connectiontype = CONNECTION_TYPE_WIFI;
                    break;
                case CELL:
                    adRequestInput.getBidRequest().getDevice().connectiontype = CONNECTION_TYPE_CELL_UNKNOWN_G;
                    break;
            }
        }
    }
}
