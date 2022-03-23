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
import org.prebid.mobile.core.BuildConfig;
import org.prebid.mobile.rendering.mraid.MraidEnv;
import org.prebid.mobile.rendering.sdk.ManagersResolver;
import org.prebid.mobile.rendering.sdk.deviceData.listeners.SdkInitListener;
import org.prebid.mobile.rendering.session.manager.OmAdSessionManager;
import org.prebid.mobile.rendering.utils.helpers.AppInfoManager;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class PrebidMobile {

    public static final String SCHEME_HTTPS = "https";
    public static final String SCHEME_HTTP = "http";

    /**
     * SDK version
     */
    public static final String SDK_VERSION = BuildConfig.VERSION;

    /**
     * SDK name provided for MRAID_ENV in {@link MraidEnv}
     */
    public static final String SDK_NAME = "prebid-mobile-sdk-rendering";
    /**
     * Currently implemented MRAID version.
     */
    public static final String MRAID_VERSION = "3.0";

    /**
     * Currently implemented Native Ads version.
     */
    public static final String NATIVE_VERSION = "1.2";

    /**
     * Maximum refresh interval allowed. 120 seconds
     */
    public static final int AUTO_REFRESH_DELAY_MAX = 120_000;

    /**
     * Default refresh interval. 60 seconds
     * Used when the refresh interval is not in the AUTO_REFRESH_DELAY_MIN & AUTO_REFRESH_DELAY_MAX range.
     */
    public static final int AUTO_REFRESH_DELAY_DEFAULT = 0;

    /**
     * Minimum refresh interval allowed. 30 seconds
     */
    public static final int AUTO_REFRESH_DELAY_MIN = 30_000;

    /**
     * Open measurement SDK version
     */
    public static final String OMSDK_VERSION = BuildConfig.OMSDK_VERSION;

    private static final AtomicInteger INIT_SDK_TASK_COUNT = new AtomicInteger();
    private static final int MANDATORY_TASK_COUNT = 3;

    /**
     * Log levels for easy development
     * Default - No sdks logs
     * Refer - LogLevel
     */
    public static LogLevel logLevel = LogLevel.NONE;

    /**
     * If true, the SDK sends "af=3,5", indicating support for MRAID
     */
    public static boolean sendMraidSupportParams = true;
    public static boolean isCoppaEnabled = false;
    public static boolean useExternalBrowser = false;

    private static SdkInitListener sdkInitListener;

    private static boolean isSdkInitialized = false;
    private static boolean useHttps = false;


    private static final String TAG = PrebidMobile.class.getSimpleName();

    static boolean timeoutMillisUpdated = false;
    private static boolean pbsDebug = false;
    private static boolean shareGeoLocation = false;
    private static boolean assignNativeAssetID = false;
    private static int timeoutMillis = 2_000;

    private static String accountId = "";
    private static String storedAuctionResponse = "";

    private static Host host = Host.CUSTOM;
    private static final Map<String, String> storedBidResponses = new LinkedHashMap<>();
    private static List<ExternalUserId> externalUserIds = new ArrayList<>();
    private static HashMap<String, String> customHeaders = new HashMap<>();

    private PrebidMobile() {
    }

    public static int getTimeoutMillis() {
        return timeoutMillis;
    }

    public static void setTimeoutMillis(int timeoutMillis) {
        PrebidMobile.timeoutMillis = timeoutMillis;
    }

    public static void setPrebidServerAccountId(String accountId) {
        PrebidMobile.accountId = accountId;
    }

    public static String getPrebidServerAccountId() {
        return accountId;
    }

    public static void setPrebidServerHost(Host host) {
        if (host == null) {
            LogUtil.error(TAG, "setPrebidServerHost: Can't set null.");
            return;
        }
        PrebidMobile.host = host;
    }

    public static Host getPrebidServerHost() {
        return host;
    }

    public static void setShareGeoLocation(boolean share) {
        PrebidMobile.shareGeoLocation = share;
    }

    public static boolean isShareGeoLocation() {
        return shareGeoLocation;
    }

    /**
     * List containing objects that hold External User Id parameters for the current application user.
     */
    public static void setExternalUserIds(List<ExternalUserId> externalUserIds) {
        PrebidMobile.externalUserIds = externalUserIds;
    }

    /**
     * Returns the List that hold External UserId parameters for the current application user
     *
     * @@return externalUserIds as Array.
     */
    public static List<ExternalUserId> getExternalUserIds() {
        return PrebidMobile.externalUserIds;
    }

    /**
     * HashMap containing a list of custom headers to add to requests
     */
    public static void setCustomHeaders(HashMap<String, String> customHeaders) {
        PrebidMobile.customHeaders = customHeaders;
    }

    /**
     * Returns the HashMap containing a list of custom headers to add to requests
     *
     * @return externalUserIds as Array.
     */
    public static HashMap<String, String> getCustomHeaders() {
        return PrebidMobile.customHeaders;
    }

    public static void setApplicationContext(@Nullable Context context) {
        setApplicationContext(context, null);
    }

    public static void setApplicationContext(@Nullable Context context, @Nullable SdkInitListener listener) {
        if (context == null) {
            LogUtil.error("Context must be not null!");
            return;
        }

        if (isSdkInitialized) {
            return;
        }
        LogUtil.debug(TAG, "Initializing Prebid Rendering SDK");

        sdkInitListener = listener;
        INIT_SDK_TASK_COUNT.set(0);

        if (logLevel != null) {
            initializeLogging();
        }
        AppInfoManager.init(context);
        initOpenMeasurementSDK(context);
        ManagersResolver.getInstance().prepare(context);
    }

    public static Context getApplicationContext() {
        return ManagersResolver.getInstance().getContext();
    }

    public static void setStoredAuctionResponse(@Nullable String storedAuctionResponse) {
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
    public static Map<String, String> getStoredBidResponses() {
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
     */
    public static boolean shouldAssignNativeAssetID() {
        return assignNativeAssetID;
    }

    /**
     * For assigning ID to the Assets in the Asset array (in Native Ad Request)
     */
    public static void assignNativeAssetID(boolean assignNativeAssetID) {
        PrebidMobile.assignNativeAssetID = assignNativeAssetID;
    }

    /**
     * Return 'true' if Prebid Rendering SDK is initialized completely
     */
    public static boolean isSdkInitialized() {
        return isSdkInitialized;
    }

    private static void initOpenMeasurementSDK(Context context) {
        OmAdSessionManager.activateOmSdk(context.getApplicationContext());
        increaseTaskCount();
    }

    private static void initializeLogging() {
        LogUtil.setLogLevel(logLevel.getValue());//set to the publisher set value
        increaseTaskCount();
    }

    /**
     * Notifies SDK initializer that one task was completed.
     * Only for internal use!
     */
    public static void increaseTaskCount() {
        if (INIT_SDK_TASK_COUNT.incrementAndGet() >= MANDATORY_TASK_COUNT) {
            isSdkInitialized = true;
            LogUtil.debug(TAG, "Prebid Rendering SDK " + SDK_VERSION + " Initialized");

            if (sdkInitListener != null) {
                sdkInitListener.onSDKInit();
            }
        }
    }


    /**
     * LogLevel for logging control.
     * NONE - no sdk logs.
     * ERROR - sdk logs with error level only.
     * WARN - sdk logs with warn level only.
     * DEBUG - sdk logs with debug level only. Noisy level.
     */
    public enum LogLevel {
        NONE(-1), DEBUG(3), WARN(5), ERROR(6);

        private final int value;

        LogLevel(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

}
