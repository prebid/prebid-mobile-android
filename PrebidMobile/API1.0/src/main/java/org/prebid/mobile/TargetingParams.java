/*
 *    Copyright 2018-2019 Prebid.org, Inc.
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

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import java.util.Calendar;


/**
 * TargetingParams class sets the Targeting parameters like yob, gender, location
 * and other custom parameters for the adUnits to be made available in the auction.
 */
public class TargetingParams {
    //region Static Variables
    private static int yob = 0;
    private static GENDER gender = GENDER.UNKNOWN;
    private static String domain = "";
    private static String storeUrl = "";
    private static String bundleName = null;
    private static final String PREBID_CONSENT_STRING_KEY = "Prebid_GDPR_consent_strings";
    private static final String IABConsent_ConsentString = "IABConsent_ConsentString";
    private static final String PREBID_GDPR_KEY = "Prebid_GDPR";
    private static final String IABConsent_SubjectToGDPR = "IABConsent_SubjectToGDPR";
    //endregion

    //region Private Constructor
    private TargetingParams() {
    }
    //endregion

    //region Public APIs

    public static void setGDPRConsentString(String string) {
        Context context = PrebidMobile.getApplicationContext();
        if (!TextUtils.isEmpty(string) && context != null) {
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString(PREBID_CONSENT_STRING_KEY, string);
            editor.apply();
        }
    }

    public static String getGDPRConsentString() {
        Context context = PrebidMobile.getApplicationContext();
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

    public static void setSubjectToGDPR(boolean consent) {
        Context context = PrebidMobile.getApplicationContext();
        if (context != null) {
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean(PREBID_GDPR_KEY, consent);
            editor.apply();
        }
    }

    public static Boolean isSubjectToGDPR() {
        Context context = PrebidMobile.getApplicationContext();
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
    public static void setYearOfBirth(int yob) throws Exception {
        if (yob >= 1900 && yob < Calendar.getInstance().get(Calendar.YEAR)) {
            TargetingParams.yob = yob;
        } else {
            throw new Exception("Year of birth must be between 1900 and " + Calendar.getInstance().get(Calendar.YEAR));
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
     * Get the platform-specific identifier, should be bundle/package name
     */
    public static synchronized String getBundleName() {
        if (TextUtils.isEmpty(bundleName)) {
            Context context = PrebidMobile.getApplicationContext();
            if (context != null) {
                return context.getPackageName();
            }
        }
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


//endregion
}
