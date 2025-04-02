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

import androidx.annotation.Nullable;

import org.prebid.mobile.rendering.listeners.SdkInitializationListener;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.Ext;
import org.prebid.mobile.rendering.sdk.PrebidContextHolder;
import org.prebid.mobile.rendering.sdk.UserConsentUtils;

import java.util.ArrayList;
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

    private static String publisherName;
    private static String domain = "";
    private static String storeUrl = "";
    private static String bundleName = null;
    private static String omidPartnerName;
    private static String omidPartnerVersion;
    @Nullable
    private static String openRtbConfig;
    private static Pair<Float, Float> userLatLon;
    private static Ext userExt;
    private static Boolean sendSharedId = false;


    private static final Map<String, ExternalUserId> externalUserIdMap = new HashMap<>();
    private static final Set<String> accessControlList = new HashSet<>();
    private static final Set<String> userKeywordsSet = new HashSet<>();
    private static final Map<String, Set<String>> extDataDictionary = new HashMap<>();

    private TargetingParams() {
    }


    /* -------------------- User data -------------------- */
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

    /* -------------------- Ids -------------------- */

    /**
     * Sets external user ids. Set null for clearing.
     * See: {@link ExternalUserId}.
     */
    public static void setExternalUserIds(@Nullable List<ExternalUserId> userIds) {
        externalUserIdMap.clear();

        if (userIds == null) return;

        for (ExternalUserId userId : userIds) {
            if (userId == null) continue;
            externalUserIdMap.put(userId.getSource(), userId);
        }
    }

    /**
     * Returns external user ids.
     */
    public static List<ExternalUserId> getExternalUserIds() {
        return new ArrayList<>(externalUserIdMap.values());
    }

    /**
     * When true, the SharedID external user id is added to outgoing auction requests.
     * App developers are encouraged to consult with their legal team before enabling this feature.
     *
     * See `TargetingParams.sharedId` for details.
     *
     * @param sendSharedId the Boolean flag to determine if the SharedID external user id
     *                     is to be added to outgoing auction requests
     */
    public static void setSendSharedId(Boolean sendSharedId) {
        TargetingParams.sendSharedId = sendSharedId;
    }

    public static Boolean getSendSharedId() { return sendSharedId; }

    /**
     * A randomly generated Prebid-owned first-party identifier
     *
     * Unless reset, SharedID remains consistent throughout the current app session. The same id may also persist
     * indefinitely across multiple app sessions if local storage access is allowed. SharedID values are NOT consistent
     * across different apps on the same device.
     *
     * Note: SharedId is only sent with auction requests if `TargetingParams.sendSharedId` is set to true.
     */
    public static ExternalUserId getSharedId() {
        return SharedId.getIdentifier();
    }

    /**
     * Resets and clears out of local storage the existing SharedID value, after which `TargetingParams.sharedId` will
     * return a new randomized value.
     */
    public static void resetSharedId() {
        SharedId.resetIdentifier();
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
            Context context = PrebidContextHolder.getContext();
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
     * Must be called only after {@link PrebidMobile#initializeSdk(Context, String, SdkInitializationListener)}.
     */
    public static void setSubjectToCOPPA(@Nullable Boolean value) {
        UserConsentUtils.tryToSetSubjectToCoppa(value);
    }

    /**
     * Gets subject to COPPA. Null is undefined. <br><br>
     * <p>
     * Must be called only after {@link PrebidMobile#initializeSdk(Context, String, SdkInitializationListener)}.
     */
    @Nullable
    public static Boolean isSubjectToCOPPA() {
        return UserConsentUtils.tryToGetSubjectToCoppa();
    }

    /**
     * Sets subject to GDPR for Prebid. It uses custom static field, not IAB. <br><br>
     * <p>
     * Must be called only after {@link PrebidMobile#initializeSdk(Context, String, SdkInitializationListener)}.
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
     * Must be called only after {@link PrebidMobile#initializeSdk(Context, String, SdkInitializationListener)}.
     */
    @Nullable
    public static Boolean isSubjectToGDPR() {
        return UserConsentUtils.tryToGetSubjectToGdpr();
    }

    /**
     * Sets GDPR consent for Prebid. It uses custom static field, not IAB. <br><br>
     * <p>
     * Must be called only after {@link PrebidMobile#initializeSdk(Context, String, SdkInitializationListener)}.
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
     * Must be called only after {@link PrebidMobile#initializeSdk(Context, String, SdkInitializationListener)}.
     */
    @Nullable
    public static String getGDPRConsentString() {
        return UserConsentUtils.tryToGetGdprConsent();
    }

    /**
     * Sets Prebid custom GDPR purpose consents (device access consent). <br><br>
     * <p>
     * Must be called only after {@link PrebidMobile#initializeSdk(Context, String, SdkInitializationListener)}.
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
     * Must be called only after {@link PrebidMobile#initializeSdk(Context, String, SdkInitializationListener)}.
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
     * Must be called only after {@link PrebidMobile#initializeSdk(Context, String, SdkInitializationListener)}.
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
     * Must be called only after {@link PrebidMobile#initializeSdk(Context, String, SdkInitializationListener)}.
     */
    @Nullable
    public static Boolean getDeviceAccessConsent() {
        return UserConsentUtils.tryToGetDeviceAccessConsent();
    }

    @Nullable
    public static String getGlobalOrtbConfig() {
        return openRtbConfig;
    }

    /**
     * Sets global OpenRTB JSON string for merging with the original request.
     * Expected format: {@code "{"new_field": "value"}"}.
     * @param config JSON OpenRTB string.
     */
    public static void setGlobalOrtbConfig(String config) {
        openRtbConfig = config;
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
