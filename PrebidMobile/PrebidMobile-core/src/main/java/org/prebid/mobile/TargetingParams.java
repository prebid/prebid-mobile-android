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
import org.prebid.mobile.rendering.listeners.SdkInitializationListener;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.Ext;
import org.prebid.mobile.rendering.sdk.UserConsentUtils;

import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * TargetingParams class sets the Targeting parameters like yob, gender, location
 * and other custom parameters for the adUnits to be made available in the auction.
 */
public class TargetingParams {

    public static final String BIDDER_NAME_APP_NEXUS = "appnexus";
    public static final String BIDDER_NAME_RUBICON_PROJECT = "rubicon";
    private static final String TAG = "TargetingParams";

    private static int yearOfBirth = 0;
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
    private static Ext userExt;
    private static JSONArray extendedUserIds;


    private static final Map<String, Set<String>> userDataMap = new HashMap<>();
    private static final Set<String> accessControlList = new HashSet<>();
    private static final Set<String> userKeywordsSet = new HashSet<>();
    private static final Map<String, Set<String>> extDataDictionary = new HashMap<>();
    private static final Set<String> extKeywordsSet = new HashSet<>();

    private TargetingParams() {
    }


    /* -------------------- User data -------------------- */

    public static void setUserAge(@Nullable Integer age) {
        if (age == null) {
            yearOfBirth = 0;
            userAge = null;
            return;
        }

        if (age <= 0 || age > 120) {
            LogUtil.error(TAG, "Can't set age, it must be in range from 0 to 120");
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
        if (yob == 0) {
            TargetingParams.yearOfBirth = 0;
            TargetingParams.userAge = null;
            return;
        }

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
        UNKNOWN;

        public String getKey() {
            switch (this) {
                case MALE:
                    return "M";
                case FEMALE:
                    return "F";
                default:
                    return "O";
            }
        }

        public static GENDER genderByKey(String key) {
            switch (key) {
                case "M":
                    return MALE;
                case "F":
                    return FEMALE;
                default:
                    return UNKNOWN;
            }
        }
    }

    /**
     * Get the current user's gender, if it's available.  The default value is UNKNOWN.
     *
     * @return The user's gender.
     */
    @NonNull
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
    public static void setGender(@Nullable GENDER gender) {
        if (gender != null) {
            TargetingParams.gender = gender;
        }
    }

    /**
     * Sets user latitude and longitude
     *
     * @param latitude  User latitude
     * @param longitude User longitude
     */
    public static void setUserLatLng(
        Float latitude,
        Float longitude
    ) {
        if (latitude == null || longitude == null) {
            userLatLon = null;
            return;
        }
        userLatLon = new Pair<>(latitude, longitude);
    }

    public static Pair<Float, Float> getUserLatLng() {
        return userLatLon;
    }

    /**
     * This method obtains the user data keyword & value for global user targeting
     * if the key already exists the value will be appended to the list. No duplicates will be added
     */
    public static void addUserData(
        String key,
        String value
    ) {
        Util.addValue(userDataMap, key, value);
    }

    /**
     * This method obtains the user data keyword & values set for global user targeting
     * the values if the key already exist will be replaced with the new set of values
     */
    public static void updateUserData(
        String key,
        Set<String> value
    ) {
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

    public static Map<String, Set<String>> getUserDataDictionary() {
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

    public static String getUserKeywords() {
        String result = TextUtils.join(",", userKeywordsSet);
        if (result.isEmpty()) return null;
        return result;
    }

    public static Set<String> getUserKeywordsSet() {
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
     * Use this API for storing the externalUserId in the SharedPreference.
     * Prebid server provide them participating server-side bid adapters.
     *
     * @param externalUserId the externalUserId instance to be stored in the SharedPreference
     */
    public static void storeExternalUserId(ExternalUserId externalUserId) {
        if (externalUserId != null) {
            StorageUtils.storeExternalUserId(externalUserId);
        } else {
            LogUtil.error("Targeting", "External User ID can't be set as null");
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
     *
     * @deprecated use addExtData
     */
    @Deprecated
    public static void addContextData(
        String key,
        String value
    ) {
        Util.addValue(extDataDictionary, key, value);
    }

    /**
     * This method obtains the context data keyword & values set for global context targeting.
     * the values if the key already exist will be replaced with the new set of values
     * @deprecated use updateExtData
     */
    @Deprecated
    public static void updateContextData(
        String key,
        Set<String> value
    ) {
        extDataDictionary.put(key, value);
    }

    /**
     * This method allows to remove specific context data keyword & values set from global context targeting
     * @deprecated use removeExtData
     */
    @Deprecated
    public static void removeContextData(String key) {
        extDataDictionary.remove(key);
    }

    /**
     * This method allows to remove all context data set from global context targeting
     *
     * @deprecated use clearExtData
     */
    @Deprecated
    public static void clearContextData() {
        extDataDictionary.clear();
    }

    /**
     * @deprecated use getExtDataDictionary
     */
    @Deprecated
    public static Map<String, Set<String>> getContextDataDictionary() {
        return extDataDictionary;
    }

    /**
     * This method obtains the context keyword for adunit context targeting
     * Inserts the given element in the set if it is not already present.
     * (imp[].ext.context.keywords)
     * @deprecated use addExtKeyword
     */
    @Deprecated
    public static void addContextKeyword(String keyword) {
        extKeywordsSet.add(keyword);
    }

    /**
     * This method obtains the context keyword set for adunit context targeting
     * Adds the elements of the given set to the set.
     * @deprecated use addExtKeywords
     */
    @Deprecated
    public static void addContextKeywords(Set<String> keywords) {
        extKeywordsSet.addAll(keywords);
    }

    /**
     * This method allows to remove specific context keyword from adunit context targeting
     * @deprecated use removeExtKeyword
     */
    @Deprecated
    public static void removeContextKeyword(String keyword) {
        extKeywordsSet.remove(keyword);
    }

    /**
     * This method allows to remove all keywords from the set of adunit context targeting
     *
     * @deprecated use clearExtKeywords
     */
    @Deprecated
    public static void clearContextKeywords() {
        extKeywordsSet.clear();
    }

    /**
     * @deprecated use getExtKeywordsSet
     */
    @Deprecated
    public static Set<String> getContextKeywordsSet() {
        return extKeywordsSet;
    }


    /**
     * This method obtains the context data keyword & value context for global context targeting
     * if the key already exists the value will be appended to the list. No duplicates will be added
     * (app.ext.data)
     */
    public static void addExtData(
        String key,
        String value
    ) {
        Util.addValue(extDataDictionary, key, value);
    }

    /**
     * This method obtains the context data keyword & values set for global context targeting.
     * the values if the key already exist will be replaced with the new set of values
     */
    public static void updateExtData(
        String key,
        Set<String> value
    ) {
        extDataDictionary.put(key, value);
    }

    /**
     * This method allows to remove specific context data keyword & values set from global context targeting
     */
    public static void removeExtData(String key) {
        extDataDictionary.remove(key);
    }

    /**
     * This method allows to remove all context data set from global context targeting
     */
    public static void clearExtData() {
        extDataDictionary.clear();
    }

    public static Map<String, Set<String>> getExtDataDictionary() {
        return extDataDictionary;
    }

    /**
     * This method obtains the context keyword for adunit context targeting
     * Inserts the given element in the set if it is not already present.
     * (imp[].ext.context.keywords)
     */
    public static void addExtKeyword(String keyword) {
        extKeywordsSet.add(keyword);
    }

    /**
     * This method obtains the context keyword set for adunit context targeting
     * Adds the elements of the given set to the set.
     */
    public static void addExtKeywords(Set<String> keywords) {
        extKeywordsSet.addAll(keywords);
    }

    /**
     * This method allows to remove specific context keyword from adunit context targeting
     */
    public static void removeExtKeyword(String keyword) {
        extKeywordsSet.remove(keyword);
    }

    /**
     * This method allows to remove all keywords from the set of adunit context targeting
     */
    public static void clearExtKeywords() {
        extKeywordsSet.clear();
    }

    public static Set<String> getExtKeywordsSet() {
        return extKeywordsSet;
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

    public static Set<String> getAccessControlList() {
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

    /**
     * Sets subject to COPPA. Null to set undefined. <br><br>
     * <p>
     * Must be called only after {@link PrebidMobile#initializeSdk(Context, SdkInitializationListener)}.
     */
    public static void setSubjectToCOPPA(@Nullable Boolean value) {
        UserConsentUtils.tryToSetSubjectToCoppa(value);
    }

    /**
     * Gets subject to COPPA. Null is undefined. <br><br>
     * <p>
     * Must be called only after {@link PrebidMobile#initializeSdk(Context, SdkInitializationListener)}.
     */
    @Nullable
    public static Boolean isSubjectToCOPPA() {
        return UserConsentUtils.tryToGetSubjectToCoppa();
    }

    /**
     * Sets subject to GDPR for Prebid. It uses custom static field, not IAB. <br><br>
     * <p>
     * Must be called only after {@link PrebidMobile#initializeSdk(Context, SdkInitializationListener)}.
     */
    public static void setSubjectToGDPR(@Nullable Boolean value) {
        UserConsentUtils.tryToSetPrebidSubjectToGdpr(value);
    }

    /**
     * Gets any given subject to GDPR in that order. <br>
     * 1) Prebid subject to GDPR custom value, if present. <br>
     * 2) IAB subject to GDPR TCF 2.0. <br>
     * Otherwise, null. <br><br>
     * <p>
     * Must be called only after {@link PrebidMobile#initializeSdk(Context, SdkInitializationListener)}.
     */
    @Nullable
    public static Boolean isSubjectToGDPR() {
        return UserConsentUtils.tryToGetSubjectToGdpr();
    }

    /**
     * Sets GDPR consent for Prebid. It uses custom static field, not IAB. <br><br>
     * <p>
     * Must be called only after {@link PrebidMobile#initializeSdk(Context, SdkInitializationListener)}.
     */
    public static void setGDPRConsentString(@Nullable String consent) {
        UserConsentUtils.tryToSetPrebidGdprConsent(consent);
    }

    /**
     * Gets any given GDPR consent in that order. <br>
     * 1) Prebid GDPR consent custom value, if present. <br>
     * 2) IAB GDPR consent TCF 2.0. <br>
     * Otherwise, null. <br><br>
     * <p>
     * Must be called only after {@link PrebidMobile#initializeSdk(Context, SdkInitializationListener)}.
     */
    @Nullable
    public static String getGDPRConsentString() {
        return UserConsentUtils.tryToGetGdprConsent();
    }

    /**
     * Sets Prebid custom GDPR purpose consents (device access consent). <br><br>
     * <p>
     * Must be called only after {@link PrebidMobile#initializeSdk(Context, SdkInitializationListener)}.
     */
    public static void setPurposeConsents(@Nullable String purposeConsents) {
        UserConsentUtils.tryToSetPrebidGdprPurposeConsents(purposeConsents);
    }

    /**
     * Gets any given purpose consent for set index in that order. <br>
     * 1) Prebid GDPR purpose consent custom value, if present. <br>
     * 2) IAB GDPR TCF 2.0 purpose consent. <br>
     * Returns null if purpose consent isn't set or index is out of bounds. <br><br>
     * <p>
     * Must be called only after {@link PrebidMobile#initializeSdk(Context, SdkInitializationListener)}.
     */
    @Nullable
    public static Boolean getPurposeConsent(int index) {
        return UserConsentUtils.tryToGetGdprPurposeConsent(index);
    }

    /**
     * Gets any given purpose consent for set index in that order. <br>
     * 1) Prebid GDPR purpose consent custom value, if present. <br>
     * 2) IAB GDPR TCF 2.0 purpose consent. <br>
     * Otherwise, null.
     * <p>
     * Must be called only after {@link PrebidMobile#initializeSdk(Context, SdkInitializationListener)}.
     */
    @Nullable
    public static String getPurposeConsents() {
        return UserConsentUtils.tryToGetGdprPurposeConsents();
    }

    /**
     * Gets the device access consent set by the publisher.<br><br>
     * If custom Prebid subject and purpose consent set, gets device access from them.
     * Otherwise, from IAB standard.
     * <p>
     * Must be called only after {@link PrebidMobile#initializeSdk(Context, SdkInitializationListener)}.
     */
    @Nullable
    public static Boolean getDeviceAccessConsent() {
        return UserConsentUtils.tryToGetDeviceAccessConsent();
    }


    /* -------------------- Ext -------------------- */

    /**
     * Sets user Ext
     *
     * @param ext Placeholder for exchange-specific extensions to OpenRTB.
     */
    public static void setUserExt(Ext ext) {
        userExt = ext;
    }

    public static Ext getUserExt() {
        return userExt;
    }

}
