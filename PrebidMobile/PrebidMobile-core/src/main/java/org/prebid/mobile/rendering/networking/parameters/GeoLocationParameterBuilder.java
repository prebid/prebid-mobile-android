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

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.telephony.TelephonyManager;

import org.prebid.mobile.LogUtil;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.geo.Geo;
import org.prebid.mobile.rendering.sdk.ManagersResolver;
import org.prebid.mobile.rendering.sdk.PrebidContextHolder;
import org.prebid.mobile.rendering.sdk.deviceData.managers.DeviceInfoManager;
import org.prebid.mobile.rendering.sdk.deviceData.managers.LocationInfoManager;

import java.util.List;
import java.util.Locale;

public class GeoLocationParameterBuilder extends ParameterBuilder {

    public static final int LOCATION_SOURCE_GPS = 1;

    @Override
    public void appendBuilderParameters(AdRequestInput adRequestInput) {
        LocationInfoManager locationInfoManager = ManagersResolver.getInstance().getLocationManager();
        DeviceInfoManager deviceManager = ManagersResolver.getInstance().getDeviceManager();

        // Strictly ignore publisher geo values
        adRequestInput.getBidRequest().getDevice().setGeo(null);

        if (locationInfoManager != null && PrebidMobile.isShareGeoLocation()) {
            if (deviceManager != null && (deviceManager.isPermissionGranted("android.permission.ACCESS_FINE_LOCATION")
                    || deviceManager.isPermissionGranted("android.permission.ACCESS_COARSE_LOCATION"))) {
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
            try {

                geo.country = getTelephonyCountry(PrebidContextHolder.getContext());

                if(geo.country.equals("")){
                    Locale locale = PrebidContextHolder.getContext().getResources().getConfiguration().locale;
                    geo.country = locale.getISO3Country();
                }

                if(geo.country.equals("")){
                    Geocoder geoCoder = new Geocoder(PrebidContextHolder.getContext());
                    List<Address> list = geoCoder.getFromLocation(locationInfoManager.getLatitude(), locationInfoManager.getLongitude(), 1);
                    geo.country = list.get(0).getCountryCode();
                }

            }catch(Throwable thr){
                LogUtil.debug("Error getting country code");
            }
        }
    }

    private String getTelephonyCountry(Context ctx){
        TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);

        if(tm != null) {
            String simCountry = tm.getSimCountryIso().toUpperCase();
            String networkCountry = tm.getNetworkCountryIso().toUpperCase();

            if (!simCountry.equals("")) {
                return simCountry;
            } else if (!networkCountry.equals("")) {
                return networkCountry;
            }
        }
        return "";
    }
}
