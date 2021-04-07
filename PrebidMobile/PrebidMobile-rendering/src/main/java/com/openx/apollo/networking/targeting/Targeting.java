package com.openx.apollo.networking.targeting;

import android.util.Pair;

import androidx.annotation.VisibleForTesting;

import com.openx.apollo.models.openrtb.bidRequests.Ext;
import com.openx.apollo.networking.parameters.UserParameters;
import com.openx.apollo.utils.helpers.Utils;

import org.json.JSONArray;

import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

public class Targeting {
    static final String KEY_AGE = "age";
    static final String KEY_GENDER = "gen";
    static final String KEY_APP_STORE_URL = "url";
    static final String KEY_CARRIER = "crr";
    static final String KEY_IP_ADDRESS = "ip";
    static final String KEY_USER_ID = "xid";
    static final String KEY_PUBLISHER_NAME = "pub_name";
    static final String KEY_BUYER_ID = "buyid";
    static final String KEY_USER_KEY_WORDS = "usr_kw";
    static final String KEY_USER_CUSTOM_DATA = "usr_cd";

    private static final Hashtable<String, String> sTargetingHashtable = new Hashtable<>();

    private static final Set<String> sAccessControlList = new HashSet<>();
    private static final Map<String, Set<String>> sUserDataMap = new HashMap<>();
    private static final Map<String, Set<String>> sContextDataMap = new HashMap<>();

    private static Integer sUserYob;

    private static Pair<Float, Float> sUserLatLon;

    private static Ext sUserExt;

    private static JSONArray sEids;

    private Targeting() {

    }

    /**
     * Set a user age.
     *
     * @param age the new user age
     */
    public static void setUserAge(Integer age) {
        if (age == null) {
            sUserYob = null;
            putTargetingValue(KEY_AGE, null);
            return;
        }

        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        int yearOfBirth = currentYear - age;

        putTargetingValue(KEY_AGE, String.valueOf(age));
        sUserYob = yearOfBirth;
    }

    public static String getUserAge() {
        return getTargetingValue(KEY_AGE);
    }

    public static Integer getUserYob() {
        return sUserYob;
    }

    /**
     * Set user keywords
     *
     * @param keywords Comma separated list of keywords, interests, or intent.
     */
    public static void setUserKeywords(String keywords) {
        putTargetingValue(KEY_USER_KEY_WORDS, keywords);
    }

    public static String getUserKeyWords() {
        return getTargetingValue(KEY_USER_KEY_WORDS);
    }

    /**
     * Optional feature to pass bidder data that was set in the
     * exchange’s cookie. The string must be in base85 cookie safe
     * characters and be in any format. Proper JSON encoding must
     * be used to include “escaped” quotation marks.
     *
     * @param data Custom data to be passed
     */
    public static void setUserCustomData(String data) {
        putTargetingValue(KEY_USER_CUSTOM_DATA, data);
    }

    public static String getUserCustomData() {
        return getTargetingValue(KEY_USER_CUSTOM_DATA);
    }

    /**
     * Set a user gender.
     *
     * @param gender the new user gender
     */
    public static void setUserGender(UserParameters.OXMGender gender) {
        putTargetingValue(KEY_GENDER, UserParameters.getGenderDescription(gender));
    }

    public static String getUserGender() {
        return getTargetingValue(KEY_GENDER);
    }

    /**
     * Sets user latitude and longitude
     *
     * @param latitude  User latitude
     * @param longitude User longitude
     */
    public static void setUserLatLng(Float latitude, Float longitude) {
        sUserLatLon = new Pair<>(latitude, longitude);
    }

    public static Pair<Float, Float> getUserLatLng() {
        return sUserLatLon;
    }

    /**
     * Set a network carrier.
     *
     * @param networkCarrier the new user network carrier
     */
    public static void setCarrier(String networkCarrier) {
        putTargetingValue(KEY_CARRIER, networkCarrier);
    }

    public static String getCarrier() {
        return getTargetingValue(KEY_CARRIER);
    }

    /**
     * Set an IP address.
     *
     * @param ipAddress - ip address
     */
    public static void setDeviceIpAddress(String ipAddress) {
        putTargetingValue(KEY_IP_ADDRESS, ipAddress);
    }

    public static String getDeviceIpAddress() {
        return getTargetingValue(KEY_IP_ADDRESS);
    }

    /**
     * Set the application store market URL.
     *
     * @param appStoreMarketUrl the new application store market URL
     */
    public static void setAppStoreMarketUrl(String appStoreMarketUrl) {
        putTargetingValue(KEY_APP_STORE_URL, appStoreMarketUrl);
    }

    public static String getAppStoreMarketUrl() {
        return getTargetingValue(KEY_APP_STORE_URL);
    }

    /**
     * Sets user Ext
     *
     * @param ext Placeholder for exchange-specific extensions to OpenRTB.
     */
    public static void setUserExt(Ext ext) {
        sUserExt = ext;
    }

    public static Ext getUserExt() {
        return sUserExt;
    }

    public static void setEids(JSONArray eids) {
        sEids = eids;
    }

    public static JSONArray getEids() {
        return sEids;
    }

    /**
     * Set the user identifier.
     *
     * @param userId the new user identifier
     */
    public static void setUserId(String userId) {
        putTargetingValue(KEY_USER_ID, userId);
    }

    public static String getUserId() {
        return getTargetingValue(KEY_USER_ID);
    }

    /**
     * Sets buyerId
     *
     * @param buyerId Buyer-specific ID for the user as mapped by the exchange for
     *                the buyer. At least one of buyeruid or id is recommended.
     */
    public static void setBuyerUid(String buyerId) {
        putTargetingValue(KEY_BUYER_ID, buyerId);
    }

    public static String getBuyerUid() {
        return getTargetingValue(KEY_BUYER_ID);
    }

    /**
     * Sets publisher name
     *
     * @param publisherName Publisher name
     */
    public static void setPublisherName(String publisherName) {
        putTargetingValue(KEY_PUBLISHER_NAME, publisherName);
    }

    public static String getPublisherName() {
        return getTargetingValue(KEY_PUBLISHER_NAME);
    }

    /// ext.prebid.data

    /**
     * Obtains a bidder name allowed to receive global targeting
     */
    public static void addBidderToAccessControlList(String bidderName) {
        sAccessControlList.add(bidderName);
    }

    /**
     * Removes specific bidder name
     */
    public static void removeBidderFromAccessControlList(String bidderName) {
        sAccessControlList.remove(bidderName);
    }

    /**
     * Removes all the bidder name set
     */
    public static void clearAccessControlList() {
        sAccessControlList.clear();
    }

    public static Set<String> getAccessControlList() {
        return new HashSet<>(sAccessControlList);
    }

    /// user.ext.data

    /**
     * Obtains the user data keyword & value for global user targeting
     * if the key already exists the value will be appended to the list. No duplicates will be added
     */
    public static void addUserData(String key, String value) {
        Utils.addValue(sUserDataMap, key, value);
    }

    /**
     * Obtains the user data keyword & values set for global user targeting
     * the values if the key already exist will be replaced with the new set of values
     */
    public static void updateUserData(String key, Set<String> value) {
        sUserDataMap.put(key, value);
    }

    /**
     * Removes specific user data keyword & value set from global user targeting
     */
    public static void removeUserData(String key) {
        sUserDataMap.remove(key);
    }

    /**
     * Removes all user data set from global user targeting
     */
    public static void clearUserData() {
        sUserDataMap.clear();
    }

    public static Map<String, Set<String>> getUserDataDictionary() {
        return new HashMap<>(sUserDataMap);
    }
    /// app.ext.data

    /**
     * Obtains the context data keyword & value context for global context targeting
     * if the key already exists the value will be appended to the list. No duplicates will be added
     */
    public static void addContextData(String key, String value) {
        Utils.addValue(sContextDataMap, key, value);
    }

    /**
     * Obtains the context data keyword & values set for global context targeting.
     * the values if the key already exist will be replaced with the new set of values
     */
    public static void updateContextData(String key, Set<String> value) {
        sContextDataMap.put(key, value);
    }

    /**
     * Allows to remove specific context data keyword & values set from global context targeting
     */
    public static void removeContextData(String key) {
        sContextDataMap.remove(key);
    }

    /**
     * Allows to remove all context data set from global context targeting
     */
    public static void clearContextData() {
        sContextDataMap.clear();
    }

    public static Map<String, Set<String>> getContextDataDictionary() {
        return new HashMap<>(sContextDataMap);
    }

    /**
     * Set a parameter key-value pair.
     *
     * @param key   the parameter name
     * @param value the parameter value
     */
    private static void putTargetingValue(String key, String value) {
        getTargetingMap().put(key, value);
    }


    /**
     * Deletes all parameters from parameters list.
     */
    private static void clearTargetingMap() {
        getTargetingMap().clear();
    }

    private static String getTargetingValue(String key) {
        return getTargetingMap().get(key);
    }

    /**
     * Deletes all parameters and nullifies all assigned variables.
     */
    @VisibleForTesting
    static void clear() {
        clearTargetingMap();

        sUserYob = null;
        sUserLatLon = null;
        sUserExt = null;
        sEids = null;

        sAccessControlList.clear();
        sContextDataMap.clear();
        sUserDataMap.clear();
    }

    @VisibleForTesting
    static Hashtable<String, String> getTargetingMap() {
        return sTargetingHashtable;
    }
}
