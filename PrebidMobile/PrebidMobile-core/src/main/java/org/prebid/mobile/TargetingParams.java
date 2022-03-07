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
import android.text.TextUtils;
import android.util.Pair;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import org.json.JSONArray;

import java.util.*;

/**
 * TargetingParams class sets the Targeting parameters like yob, gender, location
 * and other custom parameters for the adUnits to be made available in the auction.
 */
public class TargetingParams {

    public static final String BIDDER_NAME_APP_NEXUS = "appnexus";
    public static final String BIDDER_NAME_RUBICON_PROJECT = "rubicon";
    private static final String TAG = "TargetingParams";

    private static Integer yearOfBirth = null;
    private static Integer userAge = null;
    private static GENDER gender = GENDER.UNKNOWN;
    private static String userId;
    private static String publisherName;
    private static String buyerUserId;
    private static String domain = "";
    private static String storeUrl = "";
    private static String bundleName = null;
    private static String omidPartnerName;
    private static String omidPartnerVersion;
    private static String userCustomData;
    private static Pair<Float, Float> userLatLon;
    private static ExtObject userExt;
    private static JSONArray extendedUserIds;


    private static final Map<String, Set<String>> userDataMap = new HashMap<>();
    private static final Set<String> accessControlList = new HashSet<>();
    private static final Set<String> userKeywordsSet = new HashSet<>();
    private static final Map<String, Set<String>> contextDataDictionary = new HashMap<>();
    private static final Set<String> contextKeywordsSet = new HashSet<>();

    private TargetingParams() {
    }


    /* -------------------- User data -------------------- */

    public static void setUserAge(@Nullable Integer age) {
        if (age == null) {
            yearOfBirth = null;
            userAge = null;
            return;
        }

        if (age < 0 || age > 120) {
            LogUtil.e(TAG, "Can't set age, it must be in range from 0 to 120");
            return;
        }

        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        int yearOfBirth = currentYear - age;

        TargetingParams.userAge = age;
        TargetingParams.yearOfBirth = yearOfBirth;
    }

    @Nullable
    public static Integer getUserAge() {
        return userAge;
    }

    /**
     * Get the year of birth for targeting
     *
     * @return yob
     */
    public static int getYearOfBirth() {
        return yearOfBirth;
    }

    /**
     * Set the year of birth and user age for targeting
     *
     * @param yob yob of the user
     */
    public static void setYearOfBirth(int yob) throws Exception {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        if (yob >= 1900 && yob < currentYear) {
            TargetingParams.yearOfBirth = yob;
            TargetingParams.userAge = currentYear - yearOfBirth;
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
     * Sets user latitude and longitude
     *
     * @param latitude  User latitude
     * @param longitude User longitude
     */
    public static void setUserLatLng(Float latitude, Float longitude) {
        userLatLon = new Pair<>(latitude, longitude);
    }

    public static Pair<Float, Float> getUserLatLng() {
        return userLatLon;
    }

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

    /**
     * Optional feature to pass bidder data that was set in the
     * exchange’s cookie. The string must be in base85 cookie safe
     * characters and be in any format. Proper JSON encoding must
     * be used to include “escaped” quotation marks.
     *
     * @param data Custom data to be passed
     */
    public static void setUserCustomData(@Nullable String data) {
        userCustomData = data;
    }

    @Nullable
    public static String getUserCustomData() {
        return userCustomData;
    }


    /* -------------------- Ids -------------------- */

    /**
     * Set the user identifier.
     *
     * @param userId the new user identifier
     */
    public static void setUserId(String userId) {
        TargetingParams.userId = userId;
    }

    public static String getUserId() {
        return TargetingParams.userId;
    }

    /**
     * Sets buyerId
     *
     * @param buyerId Buyer-specific ID for the user as mapped by the exchange for
     *                the buyer. At least one of buyeruid or id is recommended.
     */
    public static void setBuyerId(@Nullable String buyerId) {
        buyerUserId = buyerId;
    }

    @Nullable
    public static String getBuyerId() {
        return buyerUserId;
    }

    /**
     * Use this API for storing the externalUserId in the SharedPreference
     *
     * @param externalUserId the externalUserId instance to be stored in the SharedPreference
     */
    public static void storeExternalUserId(ExternalUserId externalUserId) {
        if (externalUserId != null) {
            StorageUtils.storeExternalUserId(externalUserId);
        } else {
            LogUtil.e("Targeting", "External User ID can't be set as null");

        }
    }

    /**
     * Returns the stored (in the SharedPreference) ExternalUserId instance for a given source
     */
    public static ExternalUserId fetchStoredExternalUserId(@NonNull String source) {
        if (!TextUtils.isEmpty(source)) {
            return StorageUtils.fetchStoredExternalUserId(source);
        }
        return null;
    }

    /**
     * Returns the stored (in the SharedPreferences) External User Id list
     */
    public static List<ExternalUserId> fetchStoredExternalUserIds() {
        return StorageUtils.fetchStoredExternalUserIds();
    }

    /**
     * Removes the stored (in the SharedPreference) ExternalUserId instance for a given source
     */
    public static void removeStoredExternalUserId(@NonNull String source) {
        if (!TextUtils.isEmpty(source)) {
            StorageUtils.removeStoredExternalUserId(source);
        }
    }

    /**
     * Clear the Stored ExternalUserId list from the SharedPreference
     */
    public static void clearStoredExternalUserIds() {
        StorageUtils.clearStoredExternalUserIds();
    }

    /**
     * Sets extended user ids. Prebid server provide them
     * to participating server-side bid adapters.
     */
    public static void setExtendedUserIds(JSONArray ids) {
        extendedUserIds = ids;
    }

    public static JSONArray getExtendedUserIds() {
        return extendedUserIds;
    }


    /* -------------------- Context and application data -------------------- */

    /**
     * Sets publisher name
     *
     * @param publisherName Publisher name
     */
    public static void setPublisherName(String publisherName) {
        TargetingParams.publisherName = publisherName;
    }

    public static String getPublisherName() {
        return TargetingParams.publisherName;
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
     * This method obtains the context data keyword & value context for global context targeting
     * if the key already exists the value will be appended to the list. No duplicates will be added
     * (app.ext.data)
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

    /**
     * This method obtains the context keyword for adunit context targeting
     * Inserts the given element in the set if it is not already present.
     * (imp[].ext.context.keywords)
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

    static Set<String> getContextKeywordsSet() {
        return contextKeywordsSet;
    }


    /* -------------------- Publishers -------------------- */

    /**
     * This method obtains a bidder name allowed to receive global targeting
     * (ext.prebid.data)
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

    /**
     * OMID signaling
     */
    @Nullable
    public static String getOmidPartnerName() {
        return omidPartnerName;
    }

    public static void setOmidPartnerName(@Nullable String omidPartnerName) {
        TargetingParams.omidPartnerName = omidPartnerName;
    }

    @Nullable
    public static String getOmidPartnerVersion() {
        return omidPartnerVersion;
    }

    public static void setOmidPartnerVersion(@Nullable String omidPartnerVersion) {
        TargetingParams.omidPartnerVersion = omidPartnerVersion;
    }


    /* -------------------- Consents -------------------- */

    public static void setSubjectToCOPPA(boolean isCoppa) {
        try {
            StorageUtils.setPbCoppa(isCoppa);
        } catch (PbContextNullException ex) {
            LogUtil.e("Targeting", "Coppa was not updated", ex);
        }
    }

    public static boolean isSubjectToCOPPA() {
        try {
            return StorageUtils.getPbCoppa();
        } catch (PbContextNullException ex) {
            LogUtil.e("Targeting", "Can't get COPPA", ex);
            return false;
        }
    }

    public static void setSubjectToGDPR(@Nullable Boolean consent) {
        try {
            StorageUtils.setPbGdprSubject(consent);
        } catch (PbContextNullException ex) {
            LogUtil.e("Targeting", "GDPR Subject was not updated", ex);
        }
    }

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
            LogUtil.e("Targeting", "Can't get GDPR subject", ex);
        }

        return gdprSubject;
    }

    public static void setGDPRConsentString(@Nullable String string) {
        try {
            StorageUtils.setPbGdprConsent(string);
        } catch (PbContextNullException ex) {
            LogUtil.e("Targeting", "GDPR Consent was not updated", ex);
        }
    }

    @Nullable
    public static String getGDPRConsentString() {
        String gdprConsent = null;
        try {
            // TCF consent string
            String iabGdprConsent = StorageUtils.getIabGdprConsent();
            if (!TextUtils.isEmpty(iabGdprConsent)) {
                gdprConsent = iabGdprConsent;
            } else {
                // GDPR consent string
                String pbGdprConsent = StorageUtils.getPbGdprConsent();
                if (!TextUtils.isEmpty(pbGdprConsent)) {
                    gdprConsent = pbGdprConsent;
                }
            }
        } catch (PbContextNullException ex) {
            LogUtil.e("Targeting", "can not get GDPR Consent", ex);
        }

        return gdprConsent;
    }

    /**
     * TCF 2.0 device access consent
     */
    public static void setPurposeConsents(@Nullable String purposeConsents) {
        try {
            StorageUtils.setPbPurposeConsents(purposeConsents);
        } catch (PbContextNullException ex) {
            LogUtil.e("Targeting", "GDPR Device access Consent was not updated", ex);
        }
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

    public static String getPurposeConsents() {
        String savedPurposeConsents = null;
        try {
            // TCF purpose consent
            String iabPurposeConsentsString = StorageUtils.getIabPurposeConsents();
            if (iabPurposeConsentsString != null) {
                savedPurposeConsents = iabPurposeConsentsString;
            } else {
                // GDPR purpose consent
                String pbPurposeConsentsString = StorageUtils.getPbPurposeConsents();
                if (pbPurposeConsentsString != null) {
                    savedPurposeConsents = pbPurposeConsentsString;
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
     * @return A valid Base64 encode consent string as per
     * https://github.com/InteractiveAdvertisingBureau/GDPR-Transparency-and-Consent-Framework
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


    /* -------------------- Ext -------------------- */

    /**
     * Sets user Ext
     *
     * @param ext Placeholder for exchange-specific extensions to OpenRTB.
     */
    public static void setUserExt(ExtObject ext) {
        userExt = ext;
    }

    public static ExtObject getUserExt() {
        return userExt;
    }

}
