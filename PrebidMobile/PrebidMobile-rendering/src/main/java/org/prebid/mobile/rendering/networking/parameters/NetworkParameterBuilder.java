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
            UserParameters.OXMConnectionType autoDetectedValue = connectionInfoManager.getConnectionType();
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
