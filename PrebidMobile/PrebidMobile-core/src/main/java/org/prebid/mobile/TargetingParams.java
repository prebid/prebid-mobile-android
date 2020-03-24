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
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


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

    public static final String BIDDER_NAME_APP_NEXUS = "appnexus";
    public static final String BIDDER_NAME_RUBICON_PROJECT = "rubicon";

    private static final Set<String> accessControlList = new HashSet<>();
    private static final Map<String, Set<String>> userDataMap = new HashMap<>();
    private static final Set<String> userKeywordsSet = new HashSet<>();
    private static final Map<String, Set<String>> contextDataDictionary = new HashMap<>();
    private static final Set<String> contextKeywordsSet = new HashSet<>();

    //endregion

    //region Private Constructor
    private TargetingParams() {
    }
    //endregion

    //region Public APIs

    //COPPA
    public static boolean isSubjectToCOPPA() {

        try {
            return StorageUtils.getPbCoppa();
        } catch (PbContextNullException ex) {
            LogUtil.e("Targeting", "can not get COPPA", ex);
            return false;
        }

    }

    public static void setSubjectToCOPPA(boolean isCoppa) {

        try {
            StorageUtils.setPbCoppa(isCoppa);
        } catch (PbContextNullException ex) {
            LogUtil.e("Targeting", "Coppa was not updated", ex);
        }
    }

    //GDPR Subject
    @Nullable
    public static Boolean isSubjectToGDPR() {

        Boolean gdprSubject = null;

        try {
            Boolean pbGdpr = StorageUtils.getPbGdprSubject();
            if (pbGdpr != null) {
                gdprSubject = pbGdpr;
            } else {
                Boolean iabGdpr = StorageUtils.getIabGdprSubject();

                if (iabGdpr != null) {
                    gdprSubject = iabGdpr;
                }
            }
        } catch (PbContextNullException ex) {
            LogUtil.e("Targeting", "can not get GDPR Subject", ex);
        }

        return gdprSubject;
    }

    public static void setSubjectToGDPR(@Nullable Boolean consent) {

        try {
            StorageUtils.setPbGdprSubject(consent);
        } catch (PbContextNullException ex) {
            LogUtil.e("Targeting", "GDPR Subject was not updated", ex);
        }
    }

    //GDPR Consent
    @Nullable
    public static String getGDPRConsentString() {

        String gdprConsent = null;

        try {

            String pbGdprConsent = StorageUtils.getPbGdprConsent();
            if (!TextUtils.isEmpty(pbGdprConsent)) {
                gdprConsent = pbGdprConsent;
            } else {
                String iabGdprConsent = StorageUtils.getIabGdprConsent();
                if (!TextUtils.isEmpty(iabGdprConsent)) {
                    gdprConsent = iabGdprConsent;
                }
            }
        } catch (PbContextNullException ex) {
            LogUtil.e("Targeting", "can not get GDPR Consent", ex);
        }

        return gdprConsent;
    }

    public static void setGDPRConsentString(@Nullable String string) {
        try {
            StorageUtils.setPbGdprConsent(string);
        } catch (PbContextNullException ex) {
            LogUtil.e("Targeting", "GDPR Consent was not updated", ex);
        }
    }

    //TCF 2.0 device access consent
    public static void setPurposeConsents(@Nullable String purposeConsents) {
        try {
            StorageUtils.setPbPurposeConsents(purposeConsents);
        } catch (PbContextNullException ex) {
            LogUtil.e("Targeting", "GDPR Device access Consent was not updated", ex);
        }
    }

    public static String getPurposeConsents() {

        String savedPurposeConsents = null;

        try {

            String pbPurposeConsentsString = StorageUtils.getPbPurposeConsents();
            if (pbPurposeConsentsString != null) {
                savedPurposeConsents = pbPurposeConsentsString;
            } else {

                String iabPurposeConsentsString = StorageUtils.getIabPurposeConsents();

                if (iabPurposeConsentsString != null) {
                    savedPurposeConsents = iabPurposeConsentsString;
                }

            }

        } catch (PbContextNullException ex) {
            LogUtil.e("Targeting", "GDPR Device access Consent was not updated", ex);
        }

        return savedPurposeConsents;
    }

    /**
     * Get the device access Consent set by the publisher.
     *
     * @return A valid Base64 encode consent string as per https://github.com/InteractiveAdvertisingBureau/GDPR-Transparency-and-Consent-Framework
     * or null if not set
     */
    @Nullable
    public static Boolean getDeviceAccessConsent() {

        Boolean deviceAccessConsent = null;
        try {
            int deviceAccessConsentIndex = 0;

            deviceAccessConsent = getPurposeConsent(deviceAccessConsentIndex);

        } catch (PbContextNullException ex) {
            LogUtil.e("Targeting", "cannot get Device access Consent", ex);
        }

        return deviceAccessConsent;
    }

    static Boolean getPurposeConsent(int index) {

        Boolean purposeConsent = null;
        String purposeConsents = getPurposeConsents();

        if (purposeConsents != null) {
            char purposeConsentChar = purposeConsents.charAt(index);

            if (purposeConsentChar == '1') {
                purposeConsent = true;
            } else if (purposeConsentChar == '0') {
                purposeConsent = false;
            } else {
                LogUtil.w("invalid char:" + purposeConsent);
            }
        }

        return purposeConsent;
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


    // MARK: - access control list (ext.prebid.data)

    /**
     * This method obtains a bidder name allowed to receive global targeting
     */
    public static void addBidderToAccessControlList(String bidderName) {
        accessControlList.add(bidderName);
    }

    /**
     * This method allows to remove specific bidder name
     */
    public static void removeBidderFromAccessControlList(String bidderName) {
        accessControlList.remove(bidderName);
    }

    /**
     * This method allows to remove all the bidder name set
     */
    public static void clearAccessControlList() {
        accessControlList.clear();
    }

    static Set<String> getAccessControlList() {
        return accessControlList;
    }

    // MARK: - global user data aka visitor data (user.ext.data)

    /**
     * This method obtains the user data keyword & value for global user targeting
     * if the key already exists the value will be appended to the list. No duplicates will be added
     */
    public static void addUserData(String key, String value) {

        Util.addValue(userDataMap, key, value);

    }

    /**
     * This method obtains the user data keyword & values set for global user targeting
     * the values if the key already exist will be replaced with the new set of values
     */
    public static void updateUserData(String key, Set<String> value) {
        userDataMap.put(key, value);
    }

    /**
     * This method allows to remove specific user data keyword & value set from global user targeting
     */
    public static void removeUserData(String key) {
        userDataMap.remove(key);
    }

    /**
     * This method allows to remove all user data set from global user targeting
     */
    public static void clearUserData() {
        userDataMap.clear();
    }

    static Map<String, Set<String>> getUserDataDictionary() {
        return userDataMap;
    }

    // MARK: - global user keywords (user.keywords)

    /**
     * This method obtains the user keyword for global user targeting
     * Inserts the given element in the set if it is not already present.
     */
    public static void addUserKeyword(String keyword) {
        userKeywordsSet.add(keyword);
    }

    /**
     * This method obtains the user keyword set for global user targeting
     * Adds the elements of the given set to the set.
     */
    public static void addUserKeywords(Set<String> keywords) {
        userKeywordsSet.addAll(keywords);
    }

    /**
     * This method allows to remove specific user keyword from global user targeting
     */
    public static void removeUserKeyword(String keyword) {
        userKeywordsSet.remove(keyword);
    }

    /**
     * This method allows to remove all keywords from the set of global user targeting
     */
    public static void clearUserKeywords() {
        userKeywordsSet.clear();
    }

    static Set<String> getUserKeywordsSet() {
        return userKeywordsSet;
    }

    // MARK: - global context data aka inventory data (app.ext.data)

    /**
     * This method obtains the context data keyword & value context for global context targeting
     * if the key already exists the value will be appended to the list. No duplicates will be added
     */
    public static void addContextData(String key, String value) {
        Util.addValue(contextDataDictionary, key, value);
    }

    /**
     * This method obtains the context data keyword & values set for global context targeting.
     * the values if the key already exist will be replaced with the new set of values
     */
    public static void updateContextData(String key, Set<String> value) {
        contextDataDictionary.put(key, value);
    }

    /**
     * This method allows to remove specific context data keyword & values set from global context targeting
     */
    public static void removeContextData(String key) {
        contextDataDictionary.remove(key);
    }

    /**
     * This method allows to remove all context data set from global context targeting
     */
    public static void clearContextData() {
        contextDataDictionary.clear();
    }

    static Map<String, Set<String>> getContextDataDictionary() {
        return contextDataDictionary;
    }

    // MARK: - adunit context keywords (imp[].ext.context.keywords)

    /**
     * This method obtains the context keyword for adunit context targeting
     * Inserts the given element in the set if it is not already present.
     */
    public static void addContextKeyword(String keyword) {
        contextKeywordsSet.add(keyword);
    }

    /**
     * This method obtains the context keyword set for adunit context targeting
     * Adds the elements of the given set to the set.
     */
    public static void addContextKeywords(Set<String> keywords) {
        contextKeywordsSet.addAll(keywords);
    }

    /**
     * This method allows to remove specific context keyword from adunit context targeting
     */
    public static void removeContextKeyword(String keyword) {
        contextKeywordsSet.remove(keyword);
    }

    /**
     * This method allows to remove all keywords from the set of adunit context targeting
     */
    public static void clearContextKeywords() {
        contextKeywordsSet.clear();
    }

    static Set<String> getContextKeywordsSet()  {
        return contextKeywordsSet;
    }

//endregion
}
