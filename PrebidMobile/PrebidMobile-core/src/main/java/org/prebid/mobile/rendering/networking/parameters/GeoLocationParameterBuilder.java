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

import org.prebid.mobile.GeoCountryFormat;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.TargetingParams;
import org.prebid.mobile.Util;
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
            Integer precision = TargetingParams.getLocationDecimalPrecision();
            geo.lat = Util.applyLocationPrecision(latitude.floatValue(), precision);
            geo.lon = Util.applyLocationPrecision(longitude.floatValue(), precision);
            geo.type = LOCATION_SOURCE_GPS;
            try {

                geo.country = getTelephonyCountry(PrebidContextHolder.getContext());

                if(geo.country.equals("")){
                    // getISO3Country() throws MissingResourceException for a locale
                    // country with no alpha-3 mapping. Catch it here so the Geocoder
                    // fallback and the alpha-3 conversion below still run.
                    try {
                        Locale locale = PrebidContextHolder.getContext().getResources().getConfiguration().locale;
                        geo.country = locale.getISO3Country();
                    } catch (Throwable thr) {
                        geo.country = "";
                    }
                }

                if(geo.country.equals("")){
                    Geocoder geoCoder = new Geocoder(PrebidContextHolder.getContext());
                    List<Address> list = geoCoder.getFromLocation(locationInfoManager.getLatitude(), locationInfoManager.getLongitude(), 1);
                    geo.country = list.get(0).getCountryCode();
                }

                // OpenRTB device.geo.country is ISO-3166-1 alpha-3, but the
                // telephony (getSimCountryIso / getNetworkCountryIso) and Geocoder
                // (Address.getCountryCode) sources return alpha-2 (e.g. "US"); only
                // the Locale.getISO3Country() fallback was already alpha-3.
                // Opt-in via PrebidMobile.setGeoCountryFormat(ALPHA3) so we don't
                // silently change existing behavior; defaults to alpha-2 (planned
                // to default to alpha-3 in 4.0). Idempotent for values already alpha-3.
                if (PrebidMobile.getGeoCountryFormat() == GeoCountryFormat.ALPHA3) {
                    geo.country = toAlpha3(geo.country);
                }

            }catch(Throwable thr){
                LogUtil.debug("Error getting country code");
            }

            // Never send an empty device.geo.country; omit the field instead.
            if ("".equals(geo.country)) {
                geo.country = null;
            }
        }
    }

    // The valid ISO-3166-1 alpha-3 set, derived from the JDK's own ISO tables.
    // Used to reject inputs that are 3 chars but not real alpha-3 (e.g. the UN
    // M.49 code "419" a Latin-American-Spanish locale can produce).
    private static final java.util.Set<String> ISO3_COUNTRIES = buildIso3Countries();

    private static java.util.Set<String> buildIso3Countries() {
        java.util.Set<String> set = new java.util.HashSet<>();
        for (String cc : Locale.getISOCountries()) {
            try {
                String iso3 = new Locale("", cc).getISO3Country();
                if (iso3 != null && iso3.length() == 3) {
                    set.add(iso3);
                }
            } catch (Throwable ignored) { }
        }
        return set;
    }

    /**
     * Convert an ISO-3166-1 alpha-2 country code to alpha-3 (e.g. "US" -> "USA",
     * "GB" -> "GBR") via the JDK's own ISO tables — full coverage. Returns a value
     * that is already valid alpha-3 unchanged, and {@code null} for empty /
     * unknown / unconvertible input (including 3-char non-ISO codes like "419" and
     * codes the JDK can't map such as "XK") so the caller omits the field rather
     * than sending a malformed or empty one.
     */
    static String toAlpha3(String country) {
        if (country == null) {
            return null;
        }
        String c = country.trim().toUpperCase(Locale.ROOT);
        if (c.length() == 3) {
            return ISO3_COUNTRIES.contains(c) ? c : null; // already alpha-3, but validate
        }
        if (c.length() != 2) {
            return null;
        }
        try {
            String iso3 = new Locale("", c).getISO3Country();
            return (iso3 != null && ISO3_COUNTRIES.contains(iso3)) ? iso3 : null;
        } catch (Throwable thr) {
            return null; // MissingResourceException for an unknown alpha-2
        }
    }

    private String getTelephonyCountry(Context ctx){
        TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);

        if(tm != null) {
            // Locale.ROOT: default-locale upper-casing corrupts codes on Turkish/
            // Azerbaijani devices ("it" -> "İT"), breaking IT/IN/ID/IE/IL/... .
            String simCountry = tm.getSimCountryIso().toUpperCase(Locale.ROOT);
            String networkCountry = tm.getNetworkCountryIso().toUpperCase(Locale.ROOT);

            if (!simCountry.equals("")) {
                return simCountry;
            } else if (!networkCountry.equals("")) {
                return networkCountry;
            }
        }
        return "";
    }
}
