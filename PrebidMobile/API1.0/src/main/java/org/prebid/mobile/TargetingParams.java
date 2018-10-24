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
package org.prebid.mobile;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import java.util.Calendar;


/**
 * TargetingParams class sets the Targeting parameters like yob, gender, location
 * and other custom parameters for the adUnits to be made available in the auction.
 */
public class TargetingParams {

    //region Static Variables
    private static final String TAG;
    private static boolean locationEnabled = false; // default location access is disabled
    private static int locationDecimalDigits = -1;
    private static Location location;
    private static String domain = "";
    private static String storeUrl = "";
    private static int privacyPolicy = 0;
    private static String bundleName = null;
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
    static final String PREBID_CONSENT_STRING_KEY = "Prebid_GDPR_consent_strings";
    static final String IABConsent_ConsentString = "IABConsent_ConsentString";

    public static void setGDPRConsentString(Context context, String string) {
        if (!TextUtils.isEmpty(string) && context != null) {
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString(PREBID_CONSENT_STRING_KEY, string);
            editor.apply();
        }
    }

    public static String getGDPRConsentString(Context context) {
        if (context != null) {
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
            if (pref.contains(PREBID_CONSENT_STRING_KEY)) {
                return pref.getString(PREBID_CONSENT_STRING_KEY, "");
            } else if (pref.contains(IABConsent_ConsentString)) {
                return pref.getString(IABConsent_ConsentString, "");
            }
        }
        return null;
    }

    static final String PREBID_GDPR_KEY = "Prebid_GDPR";
    static final String IABConsent_SubjectToGDPR = "IABConsent_SubjectToGDPR";

    public static void setSubjectToGDPR(Context context, boolean consent) {
        if (context != null) {
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean(PREBID_GDPR_KEY, consent);
            editor.apply();
        }
    }

    public static Boolean isSubjectToGDPR(Context context) {
        if (context != null) {
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
            if (pref.contains(PREBID_GDPR_KEY)) {
                return pref.getBoolean(PREBID_GDPR_KEY, false);
            } else if (pref.contains(IABConsent_SubjectToGDPR)) {
                String value = pref.getString(IABConsent_SubjectToGDPR, "");
                if ("1".equals(value)) {
                    return true;
                } else if ("0".equals(value)) {
                    return false;
                }
            }
        }
        return null;
    }

    /**
     * Get the year of birth for targeting
     *
     * @return yob
     */
    public static int getYearOfBirth() {
        return yob;
    }

    /**
     * Set the year of birth for targeting
     *
     * @param yob yob of the user
     */
    public static void setYearOfBirth(int yob) {
        if (yob > 0 && yob <= Calendar.getInstance().get(Calendar.YEAR)) {
            TargetingParams.yob = yob;
        }
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
     * shared with 3rd party networks. True means PrebidMobile SDK will try to get the latest
     * location data from either developer or location provider and pass it to demand sources.
     *
     * @param enabled default is false.
     */
    public static void setLocationEnabled(boolean enabled) {
        locationEnabled = enabled;
    }

    /**
     * Returns true if the PrebidMobile sdk is allowed to use location information
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
     * Get the platform-specific identifier, should be bundle/package name
     */
    public static synchronized String getBundleName() {
        return bundleName;
    }

    /**
     * Set the platform-specific identifier for targeting purpose
     * Should be bundle/package name
     */
    public static synchronized void setBundleName(String bundleName) {
        TargetingParams.bundleName = bundleName;
    }

    /**
     * Set the domain of your app for targeting purpose
     *
     * @param domain domain of your app
     */
    public static synchronized void setDomain(String domain) {
        TargetingParams.domain = domain;
    }

    /**
     * Get the domain of your app
     *
     * @return domain of your app
     */
    public static synchronized String getDomain() {
        return domain;
    }

    /**
     * Set the store url of your app
     *
     * @param storeUrl store url
     */
    public static synchronized void setStoreUrl(String storeUrl) {
        TargetingParams.storeUrl = storeUrl;
    }

    /**
     * Get the store url of your app
     *
     * @return store url
     */
    public static synchronized String getStoreUrl() {
        return storeUrl;
    }

    /**
     * Set whether the app has a privacy policy, where 0 = no, 1 = yes
     *
     * @param privacyPolicy default value is 0
     */
    public static synchronized void setPrivacyPolicy(int privacyPolicy) {
        if (privacyPolicy == 0 || privacyPolicy == 1) {
            TargetingParams.privacyPolicy = privacyPolicy;
        }
    }

    /**
     * Get whether the app has a privacy policy
     *
     * @return 1 if it has one
     */
    public static synchronized int getPrivacyPolicy() {
        return privacyPolicy;
    }
    static {
        TAG = LogUtil.getTagWithBase("TP");
    }

    private static int yob = 0;
    private static GENDER gender = GENDER.UNKNOWN;



//endregion
}
