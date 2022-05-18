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
import org.prebid.mobile.api.exceptions.InitError;
import org.prebid.mobile.core.BuildConfig;
import org.prebid.mobile.rendering.listeners.SdkInitializationListener;
import org.prebid.mobile.rendering.mraid.MraidEnv;
import org.prebid.mobile.rendering.sdk.ManagersResolver;
import org.prebid.mobile.rendering.sdk.SdkInitializer;
import org.prebid.mobile.rendering.sdk.deviceData.listeners.SdkInitListener;

import java.util.*;

public class PrebidMobile {

    public static boolean isCoppaEnabled = false;
    public static boolean useExternalBrowser = false;

    /**
     * If true, the SDK sends "af=3,5", indicating support for MRAID
     */
    public static boolean sendMraidSupportParams = true;

    /**
     * Minimum refresh interval allowed. 30 seconds
     */
    public static final int AUTO_REFRESH_DELAY_MIN = 30_000;

    /**
     * Maximum refresh interval allowed. 120 seconds
     */
    public static final int AUTO_REFRESH_DELAY_MAX = 120_000;

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
     * Open measurement SDK version
     */
    public static final String OMSDK_VERSION = BuildConfig.OMSDK_VERSION;

    /**
     * Please use {@link PrebidMobile#setLogLevel(LogLevel)}, this field will become private in next releases.
     */
    @Deprecated
    public static LogLevel logLevel = LogLevel.NONE;


    private static boolean pbsDebug = false;
    private static boolean shareGeoLocation = false;
    private static boolean assignNativeAssetID = false;

    /**
     * Indicates whether the PBS should cache the bid for the rendering API.
     * If the value is true the SDK will make the cache request in order to report
     * the impression event respectively to the legacy analytic setup.
     */
    private static boolean useCacheForReportingWithRenderingApi = false;

    private static int timeoutMillis = 2_000;

    private static final String TAG = PrebidMobile.class.getSimpleName();

    private static String accountId = "";
    private static String storedAuctionResponse = "";

    private static Host host = Host.CUSTOM;

    private static final Map<String, String> storedBidResponses = new LinkedHashMap<>();
    private static List<ExternalUserId> externalUserIds = new ArrayList<>();
    private static HashMap<String, String> customHeaders = new HashMap<>();

    private PrebidMobile() {}

    public static boolean isUseCacheForReportingWithRenderingApi() {
        return useCacheForReportingWithRenderingApi;
    }

    public static void setUseCacheForReportingWithRenderingApi(boolean useCacheForReportingWithRenderingApi) {
        PrebidMobile.useCacheForReportingWithRenderingApi = useCacheForReportingWithRenderingApi;
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
    public static void setCustomHeaders(@Nullable HashMap<String, String> customHeaders) {
        if (customHeaders != null) {
            PrebidMobile.customHeaders = customHeaders;
        }
    }

    /**
     * Returns the HashMap containing a list of custom headers to add to requests
     *
     * @return externalUserIds as Array.
     */
    @NonNull
    public static HashMap<String, String> getCustomHeaders() {
        return PrebidMobile.customHeaders;
    }


    /**
     * Initializes the main SDK classes. Makes request to Prebid server to check its status.
     * You have to set host url ({@link PrebidMobile#setPrebidServerHost(Host)}) before calling this method.
     *
     * @param context  any context (must be not null)
     * @param listener initialization listener (can be null)
     * @see <a href="https://docs.prebid.org/prebid-server/endpoints/pbs-endpoint-status.html">GET /status</a>
     */
    public static void initializeSdk(
        @Nullable Context context,
        @Nullable SdkInitializationListener listener
    ) {
        SdkInitializer.init(context, listener);
    }

    /**
     * Please use {@link PrebidMobile#initializeSdk(Context, SdkInitializationListener)}.
     */
    @Deprecated
    public static void setApplicationContext(@Nullable Context context) {
        SdkInitializer.init(context, null);
    }

    /**
     * Please use {@link PrebidMobile#initializeSdk(Context, SdkInitializationListener)}.
     */
    @Deprecated
    public static void setApplicationContext(
        @Nullable Context context,
        @Nullable SdkInitListener listener
    ) {
        SdkInitializer.init(context, new SdkInitializationListener() {
            @Override
            public void onSdkInit() {
                if (listener != null) {
                    listener.onSDKInit();
                }
            }

            @Override
            public void onSdkFailedToInit(InitError error) {
                LogUtil.error(TAG, error.getError());
            }
        });
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

    public static void addStoredBidResponse(
        String bidder,
        String responseId
    ) {
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
        return SdkInitializer.isIsSdkInitialized();
    }

    public static LogLevel getLogLevel() {
        return PrebidMobile.logLevel;
    }

    public static void setLogLevel(LogLevel logLevel) {
        PrebidMobile.logLevel = logLevel;
    }


    /**
     * LogLevel for logging control.
     * NONE - no sdk logs.
     * ERROR - sdk logs with error level only.
     * WARN - sdk logs with warn level only.
     * DEBUG - sdk logs with debug level only. Noisy level.
     */
    public enum LogLevel {
        NONE(-1),
        DEBUG(3),
        WARN(5),
        ERROR(6);

        private final int value;

        LogLevel(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

}
