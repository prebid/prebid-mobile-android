package com.openx.apollo.networking.parameters;

import com.openx.apollo.models.openrtb.bidRequests.geo.Geo;
import com.openx.apollo.sdk.ManagersResolver;
import com.openx.apollo.sdk.deviceData.managers.DeviceInfoManager;
import com.openx.apollo.sdk.deviceData.managers.LocationInfoManager;

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
