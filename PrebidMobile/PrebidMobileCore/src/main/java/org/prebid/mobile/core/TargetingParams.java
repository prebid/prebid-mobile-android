/*
 *    Copyright 2016 Prebid.org, Inc.
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
package org.prebid.mobile.core;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;


/**
 * TargetingParams class sets the Targeting parameters like age, gender, location
 * and other custom parameters for the adUnits to be made available in the auction.
 */
public class TargetingParams {

    //region Static Variables
    private static final String TAG;

    static {
        TAG = LogUtil.getTagWithBase("TP");
    }

    private static int age;
    private static GENDER gender = GENDER.UNKNOWN;
    private static boolean locationEnabled = false; // default location access is disabled
    private static int locationDecimalDigits = -1;
    private static HashMap<String, ArrayList<String>> customKeywords = new HashMap<String, ArrayList<String>>();
    private static Location location;
    //endregion

    //region Private Constructor
    private TargetingParams() {
    }
    //endregion

    //region Internal Helper Methods
    @SuppressLint("MissingPermission")
    static void fetchLocationUpdates(Context appContext) {
        if (appContext != null) {
            Location lastLocation = null;
            // Does app developer allow us to use location?
            if (locationEnabled) {
                LogUtil.d(TAG, "Updating location.");
                // Save last location
                if (location != null) {
                    lastLocation = location;
                }
                // fetch latest location info from location provider
                if (appContext != null
                        && (appContext.checkCallingOrSelfPermission("android.permission.ACCESS_FINE_LOCATION") == PackageManager.PERMISSION_GRANTED
                        || appContext.checkCallingOrSelfPermission("android.permission.ACCESS_COARSE_LOCATION") == PackageManager.PERMISSION_GRANTED)) {
                    // Get lat, long from any GPS information that might be currently
                    // available
                    LocationManager lm = (LocationManager) appContext
                            .getSystemService(Context.LOCATION_SERVICE);

                    for (String provider_name : lm.getProviders(true)) {
                        LogUtil.v(TAG, "Location provider_name::" + provider_name);
                        Location l = lm.getLastKnownLocation(provider_name);
                        if (l == null) {
                            continue;
                        }

                        if (lastLocation == null) {
                            lastLocation = l;
                        } else {
                            if (l.getTime() > 0 && lastLocation.getTime() > 0) {
                                if (l.getTime() > lastLocation.getTime()) {
                                    lastLocation = l;
                                }
                            }
                        }
                    }
                } else {
                    LogUtil.d(TAG,
                            "Location permissions ACCESS_COARSE_LOCATION and/or ACCESS_FINE_LOCATION aren\\'t set in the host app. Unable to update location data.");
                }
            }

            // Set the location info back to the application
            // This will set the saved location to null if location was not enabled
            if (location != lastLocation) {
                location = lastLocation;
            }
        }
    }
    //endregion

    //region Public APIs

    /**
     * Get the age for targeting
     *
     * @return age
     */
    public static int getAge() {
        return age;
    }

    /**
     * Set the age for targeting
     *
     * @param age age of the user
     */
    public static void setAge(int age) {
        TargetingParams.age = age;
    }

    public enum GENDER {
        FEMALE,
        MALE,
        UNKNOWN
    }

    /**
     * Get the current user's gender, if it's available.  The default value is UNKNOWN.
     *
     * @return The user's gender.
     */
    public static GENDER getGender() {
        return gender;
    }

    /**
     * Set the user's gender.  This should be set if the user's gender is known, as it
     * can help make buying the ad space more attractive to advertisers.  The default
     * value is UNKNOWN.
     *
     * @param gender The user's gender.
     */
    public static void setGender(GENDER gender) {
        TargetingParams.gender = gender;
    }

    /**
     * Sets whether or not location (latitude, longitude) is retrieved through provider and
     * shared with 3rd party networks. True means Prebid SDK will try to get the latest
     * location data from either developer or location provider and pass it to demand sources.
     *
     * @param enabled default is false.
     */
    public static void setLocationEnabled(boolean enabled) {
        locationEnabled = enabled;
    }

    /**
     * Returns true if the Prebid sdk is allowed to use location information
     * or false otherwise.
     */
    public static boolean getLocationEnabled() {
        return locationEnabled;
    }

    /**
     * Retrieve the location that's set by setLocation()
     *
     * @return null if location was not set
     */
    public static Location getLocation() {
        return location;
    }

    /**
     * Set the location for demand sources to target
     *
     * @param locationData location of the user
     */
    public static void setLocation(Location locationData) {
        location = locationData;
    }

    /**
     * Get the digits after the decimal of the latitude and longitude.
     *
     * @return the current value of the digits
     */

    public static synchronized int getLocationDecimalDigits() {
        return locationDecimalDigits;
    }

    /**
     * Sets the number of digits after the decimal of the latitude and longitude.
     * It will only be applied if {@link #getLocationEnabled()}.
     * Maximum of precision is 6, which means less than a foot.
     *
     * @param digitsAfterDecimal The digits
     */
    public static void setLocationDecimalDigits(int digitsAfterDecimal) {
        if (digitsAfterDecimal > 6) {
            locationDecimalDigits = 6;
            LogUtil.w(TAG, "Out of range input " + digitsAfterDecimal + ", set location digits after decimal to maximum 6");
        } else if (digitsAfterDecimal >= -1) {
            locationDecimalDigits = digitsAfterDecimal;
        } else {
            locationDecimalDigits = -1;
            LogUtil.w(TAG, "Invalid input " + digitsAfterDecimal + ", set location digits after decimal to default");
        }
    }

    /**
     * Retrieve the array of custom keywords that will passed to demand sources.
     *
     * @return The current list of key-value pairs of custom keywords.
     */
    public synchronized static HashMap<String, ArrayList<String>> getCustomKeywords() {
        HashMap<String, ArrayList<String>> result = new HashMap<String, ArrayList<String>>();
        Set<String> keys = customKeywords.keySet();
        for (String key : keys) {
            ArrayList<String> values = new ArrayList<>();
            ArrayList<String> originalValues = customKeywords.get(key);
            if (originalValues != null) {
                for (String value : originalValues) {
                    values.add(value);
                }
            }
            result.put(key, values);
        }
        return result;
    }

    /**
     * Remove a custom keyword from the targeting params. Use this to remove a keyword
     * previously set using addCustomKeywords.
     *
     * @param key The key to remove; this should not be null or empty.
     */
    public synchronized static void removeCustomKeyword(String key) {
        if (TextUtils.isEmpty(key))
            return;
        customKeywords.remove(key);
    }

    /**
     * Set a custom key/value pair for customized targeting.
     * Note this will override existing values for the same key.
     *
     * @param key   The key to add; this should not be null or empty.
     * @param value The value to add; this should not be null or empty.
     */
    public synchronized static void setCustomTargeting(String key, String value) {
        if (TextUtils.isEmpty(key) || TextUtils.isEmpty(value)) {
            LogUtil.w("Null/empty values passed in for custom targeting.");
        } else {
            ArrayList<String> values = new ArrayList<String>();
            values.add(value);
            customKeywords.put(key, values);
        }
    }

    /**
     * Set a custom key/values pair for customized targeting.
     * Note this will override existing values for the same key.
     *
     * @param key    The key to add; this should not be null or empty.
     * @param values The values to add; this should not be null or empty.
     */
    public synchronized static void setCustomTargeting(String key, ArrayList<String> values) {
        if (TextUtils.isEmpty(key) || values == null || values.isEmpty()) {
            LogUtil.w("Null/empty values passed in for custom targeting.");
        } else {
            customKeywords.put(key, values);
        }
    }

    /**
     * Clear all custom keywords.
     */
    public synchronized static void clearCustomKeywords() {
        customKeywords.clear();
    }

    //endregion
}
