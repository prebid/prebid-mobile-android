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

import org.prebid.mobile.rendering.models.openrtb.bidRequests.geo.Geo;
import org.prebid.mobile.rendering.sdk.ManagersResolver;
import org.prebid.mobile.rendering.sdk.deviceData.managers.DeviceInfoManager;
import org.prebid.mobile.rendering.sdk.deviceData.managers.LocationInfoManager;

public class GeoLocationParameterBuilder extends ParameterBuilder {

    public static final int LOCATION_SOURCE_GPS = 1;

    @Override
    public void appendBuilderParameters(AdRequestInput adRequestInput) {
        LocationInfoManager locationInfoManager = ManagersResolver.getInstance().getLocationManager();
        DeviceInfoManager deviceManager = ManagersResolver.getInstance().getDeviceManager();

        // Strictly ignore publisher geo values
        adRequestInput.getBidRequest().getDevice().setGeo(null);

        if (locationInfoManager != null) {
            if (deviceManager != null && deviceManager.isPermissionGranted("android.permission.ACCESS_FINE_LOCATION")) {
                setLocation(adRequestInput, locationInfoManager);
            }
        }
    }

    private void setLocation(AdRequestInput adRequestInput, LocationInfoManager locationInfoManager) {
        Double latitude = locationInfoManager.getLatitude();
        Double longitude = locationInfoManager.getLongitude();
        if (latitude == null || longitude == null) {
            locationInfoManager.resetLocation();
            latitude = locationInfoManager.getLatitude();
            longitude = locationInfoManager.getLongitude();
        }

        Geo geo = adRequestInput.getBidRequest().getDevice().getGeo();
        if (latitude != null && longitude != null) {
            geo.lat = latitude.floatValue();
            geo.lon = longitude.floatValue();
            geo.type = LOCATION_SOURCE_GPS;
        }
    }
}
