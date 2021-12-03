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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PrebidMobile {

    private static final int TIMEOUT_MILLIS = 2_000;

    private static int timeoutMillis = TIMEOUT_MILLIS; // by default use 2000 milliseconds as timeout
    static boolean timeoutMillisUpdated = false;

    @Nullable
    private static String storedAuctionResponse = "";

    private static boolean pbsDebug = false;

    @NonNull
    private static final Map<String, String> storedBidResponses = new LinkedHashMap<>();

    public static int getTimeoutMillis() {
        return timeoutMillis;
    }

    public static void setTimeoutMillis(int timeoutMillis) {
        PrebidMobile.timeoutMillis = timeoutMillis;
    }

    private PrebidMobile() {
    }

    private static String accountId = "";

    public static void setPrebidServerAccountId(String accountId) {
        PrebidMobile.accountId = accountId;
    }

    public static String getPrebidServerAccountId() {
        return accountId;
    }

    private static Host host = Host.CUSTOM;

    public static void setPrebidServerHost(Host host) {
        PrebidMobile.host = host;
        timeoutMillisUpdated = false; // each time a developer sets a new Host for the SDK, we should re-calculate the time out millis
        timeoutMillis = TIMEOUT_MILLIS;
    }

    public static Host getPrebidServerHost() {
        return host;
    }

    private static boolean shareGeoLocation = false;

    public static void setShareGeoLocation(boolean share) {
        PrebidMobile.shareGeoLocation = share;
    }

    public static boolean isShareGeoLocation() {
        return shareGeoLocation;
    }

    private static List<ExternalUserId> externalUserIds = new ArrayList<>();

    private static boolean assignNativeAssetID = false;
    /**
     * List containing objects that hold External UserId parameters for the current application user.
     * @param externalUserIds
     */
    public static void setExternalUserIds(List<ExternalUserId> externalUserIds){
        PrebidMobile.externalUserIds = externalUserIds;
    }

    /**
     * Returns the List that hold External UserId parameters for the current application user
     * @@return externalUserIds as Array.
     */
    public static List<ExternalUserId> getExternalUserIds() {
        return PrebidMobile.externalUserIds;
    }

    private static WeakReference<Context> applicationContextWeak;

    public static void setApplicationContext(Context context) {
        applicationContextWeak = new WeakReference<>(context);

        if (context != null) {
            AdvertisingIDUtil.retrieveAndSetAAID(context);
            PrebidServerSettings.update(context);
        }
    }

    public static Context getApplicationContext() {
        if (applicationContextWeak != null) {
            return applicationContextWeak.get();
        }
        return null;
    }

    public static void setStoredAuctionResponse(@NonNull String storedAuctionResponse) {
        PrebidMobile.storedAuctionResponse = storedAuctionResponse;
    }

    @Nullable
    public static String getStoredAuctionResponse() {
        return storedAuctionResponse;
    }

    public static void addStoredBidResponse(String bidder, String responseId) {
        storedBidResponses.put(bidder, responseId);
    }

    public static void clearStoredBidResponses() {
        storedBidResponses.clear();
    }

    @NonNull
    static Map<String, String> getStoredBidResponses() {
        return storedBidResponses;
    }

    public static boolean getPbsDebug() {
        return pbsDebug;
    }

    public static void setPbsDebug(boolean pbsDebug) {
        PrebidMobile.pbsDebug = pbsDebug;
    }

    /**
     * @return boolean that states if the ID will be set to the Asset array (in the Native Ad Request)
     * This value can be set using the {@link #assignNativeAssetID(boolean)}
     * */
    public static boolean shouldAssignNativeAssetID() {
        return assignNativeAssetID;
    }

    /**
     * For assigning ID to the Assets in the Asset array (in Native Ad Request)
     * */
    public static void assignNativeAssetID(boolean assignNativeAssetID) {
        PrebidMobile.assignNativeAssetID = assignNativeAssetID;
    }
}
