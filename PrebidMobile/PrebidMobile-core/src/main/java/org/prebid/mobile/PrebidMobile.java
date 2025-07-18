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
import android.util.Log;
import android.util.Patterns;
import android.webkit.URLUtil;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.prebid.mobile.LogUtil.PrebidLogger;
import org.prebid.mobile.api.data.InitializationStatus;
import org.prebid.mobile.api.rendering.pluginrenderer.PrebidMobilePluginRegister;
import org.prebid.mobile.api.rendering.pluginrenderer.PrebidMobilePluginRenderer;
import org.prebid.mobile.configuration.PBSConfig;
import org.prebid.mobile.core.BuildConfig;
import org.prebid.mobile.rendering.listeners.SdkInitializationListener;
import org.prebid.mobile.rendering.mraid.MraidEnv;
import org.prebid.mobile.rendering.sdk.InitializationNotifier;
import org.prebid.mobile.rendering.sdk.PrebidContextHolder;
import org.prebid.mobile.rendering.sdk.SdkInitializer;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Main class for managing Prebid SDK. It allows to initialize, set log level
 * and manage internal behaviour.
 */
public class PrebidMobile {

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
     * Tested Google SDK version.
     */
    public static final String TESTED_GOOGLE_SDK_VERSION = "24.4.0";

    private static LogLevel logLevel = LogLevel.NONE;
    @Nullable
    private static PrebidLogger customLogger = null;

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
    @Nullable
    private static String auctionSettingsId;
    @Nullable
    private static String customStatusEndpoint;

    private static Host host = Host.CUSTOM;

    private static final Map<String, String> storedBidResponses = new LinkedHashMap<>();
    private static HashMap<String, String> customHeaders = new HashMap<>();
    private static boolean includeWinners = false;
    private static boolean includeBidderKeys = false;

    private static final int DEFAULT_BANNER_TIMEOUT = 6 * 1000;
    private static final int DEFAULT_PRERENDER_TIMEOUT = 30 * 1000;

    private static PBSConfig pbsConfig;
    private static int creativeFactoryTimeout = DEFAULT_BANNER_TIMEOUT;
    private static int creativeFactoryTimeoutPreRenderContent = DEFAULT_PRERENDER_TIMEOUT;
    @NonNull
    private static WeakReference<PrebidEventDelegate> eventDelegateReference = new WeakReference<>(null);
    private static boolean disableStatusCheck = false;

    private PrebidMobile() {
    }


    /**
     * {@link #useCacheForReportingWithRenderingApi}
     */
    public static boolean isUseCacheForReportingWithRenderingApi() {
        return useCacheForReportingWithRenderingApi;
    }

    /**
     * Sets boolean for caching ad for rendering API.
     *
     * @param useCacheForReportingWithRenderingApi
     */
    public static void setUseCacheForReportingWithRenderingApi(boolean useCacheForReportingWithRenderingApi) {
        PrebidMobile.useCacheForReportingWithRenderingApi = useCacheForReportingWithRenderingApi;
    }

    /**
     * {@link #setTimeoutMillis(int)}.
     */
    public static int getTimeoutMillis() {
        return timeoutMillis;
    }

    /**
     * Sets connection timeout for bid request.
     */
    public static void setTimeoutMillis(int timeoutMillis) {
        PrebidMobile.timeoutMillis = timeoutMillis;
    }

    public static void setPrebidServerAccountId(String accountId) {
        PrebidMobile.accountId = accountId;
    }

    public static String getPrebidServerAccountId() {
        return accountId;
    }

    public static Host getPrebidServerHost() {
        return host;
    }

    public static void setAuctionSettingsId(String settingsId) {
        PrebidMobile.auctionSettingsId = settingsId;
    }

    @Nullable
    public static String getAuctionSettingsId() {
        return auctionSettingsId;
    }

    /**
     * Allows the SDK to share geolocation if permission is granted by the user.
     */
    public static void setShareGeoLocation(boolean share) {
        PrebidMobile.shareGeoLocation = share;
    }

    /**
     * {@link #setShareGeoLocation(boolean)}
     */
    public static boolean isShareGeoLocation() {
        return shareGeoLocation;
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
     * Initializes the main SDK classes and makes request to Prebid server to check its status.
     * If you use custom /status endpoint set it with ({@link PrebidMobile#setCustomStatusEndpoint(String)}) before starting initialization.
     * <p>
     * Calls SdkInitializationListener callback with enum initialization status parameter:
     * <p>
     * SUCCEEDED - Prebid SDK is initialized successfully and ready to work.
     * <p>
     * FAILED - Prebid SDK is failed to initialize and is not able to work.
     * <p>
     * SERVER_STATUS_WARNING - Prebid SDK failed to check the PBS status. The SDK is initialized and able to work, though.
     * <p>
     * To get the description of the problem you can call {@link InitializationStatus#getDescription()}
     *
     * @param context  any context (must be not null)
     * @param serverURL the Prebid Server URL
     * @param listener initialization listener (can be null).
     * <p>
     */
    @MainThread
    public static void initializeSdk(
            @Nullable Context context,
            String serverURL,
            @Nullable SdkInitializationListener listener
    ) {
        if (serverURL == null) {
            LogUtil.error(TAG, "initializeSdk: serverURL is null.");
            return;
        }
        PrebidMobile.host = Host.createCustomHost(serverURL);
        SdkInitializer.init(context, listener);
    }

    /**
     * Sets stored auction response for testing purposes.
     */
    public static void setStoredAuctionResponse(@Nullable String storedAuctionResponse) {
        PrebidMobile.storedAuctionResponse = storedAuctionResponse;
    }

    /**
     * {@link #setStoredAuctionResponse(String)}
     */
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
        return PrebidContextHolder.getContext() != null && InitializationNotifier.wereTasksCompletedSuccessfully();
    }

    /**
     * {@link #setLogLevel(LogLevel)}
     */
    public static LogLevel getLogLevel() {
        return PrebidMobile.logLevel;
    }

    /**
     * Sets log level for the SDK.
     */
    public static void setLogLevel(LogLevel logLevel) {
        PrebidMobile.logLevel = logLevel;
    }

    /**
     * {@link #setCustomLogger(PrebidLogger)}
     */
    @Nullable
    public static PrebidLogger getCustomLogger() {
        return PrebidMobile.customLogger;
    }

    /**
     * Sets custom logger for the SDK.
     */
    public static void setCustomLogger(@NonNull PrebidLogger logger) {
        PrebidMobile.customLogger = logger;
    }

    /**
     * Check Google Mobile Ads compatibility for original API.
     * Show logs if version is not compatible.
     *
     * @param googleAdsVersion - MobileAds.getVersion().toString()
     */
    public static void checkGoogleMobileAdsCompatibility(@NonNull String googleAdsVersion) {
        int[] prebidVersion = parseVersion(PrebidMobile.TESTED_GOOGLE_SDK_VERSION);
        int[] publisherVersion = parseVersion(googleAdsVersion);

        boolean prebidVersionBigger = false;
        boolean publisherVersionBigger = false;
        for (int i = 0; i < 3; i++) {
            if (prebidVersion[i] > publisherVersion[i]) {
                prebidVersionBigger = true;
                break;
            } else if (publisherVersion[i] > prebidVersion[i]) {
                publisherVersionBigger = true;
                break;
            }
        }

        if (prebidVersionBigger) {
            LogUtil.error("You should update GMA SDK version to " + PrebidMobile.TESTED_GOOGLE_SDK_VERSION + " version that was tested with Prebid SDK (current version " + googleAdsVersion + ")");
        } else if (publisherVersionBigger) {
            LogUtil.error("The current version of Prebid SDK is not validated with your version of GMA SDK " + googleAdsVersion + " (Prebid SDK tested on " + PrebidMobile.TESTED_GOOGLE_SDK_VERSION + "). Please update the Prebid SDK or post a ticket on the github.");
        }
    }

    /**
     * Sets full valid URL for the /status endpoint of the PBS.
     * Request to /status is sent when you call {@link PrebidMobile#initializeSdk(Context, String, SdkInitializationListener)} )}.
     *
     * @see <a href="https://docs.prebid.org/prebid-server/endpoints/pbs-endpoint-status.html">GET /status</a>
     */
    public static void setCustomStatusEndpoint(String url) {
        if (url == null) {
            return;
        }

        if (!Patterns.WEB_URL.matcher(url).matches()) {
            Log.e(TAG, "Can't set custom /status endpoint, it is not valid.");
            return;
        }

        if (url.startsWith("http")) {
            customStatusEndpoint = url;
        } else {
            customStatusEndpoint = URLUtil.guessUrl(url).replace("http", "https");
        }
    }

    /**
     * {@link #setCustomStatusEndpoint(String)}
     */
    @Nullable
    public static String getCustomStatusEndpoint() {
        return customStatusEndpoint;
    }

    /**
     * Sets 'includewinners' parameter for ad request to receive additional info about winners in response.
     */
    public static void setIncludeWinnersFlag(boolean includeWinners) {
        PrebidMobile.includeWinners = includeWinners;
    }

    /**
     * {@link #setIncludeWinnersFlag(boolean)}
     */
    public static boolean getIncludeWinnersFlag() {
        return PrebidMobile.includeWinners;
    }

    /**
     * Sets 'includebidderkeys' parameter for ad request to receive additional info about bidders.
     */
    public static boolean setIncludeBidderKeysFlag(boolean includeBidderKeys) {
        return PrebidMobile.includeBidderKeys = includeBidderKeys;
    }

    /**
     * {@link #setIncludeBidderKeysFlag(boolean)}
     */
    public static boolean getIncludeBidderKeysFlag() {
        return PrebidMobile.includeBidderKeys;
    }

    public static PBSConfig getPbsConfig() {
        return pbsConfig;
    }

    public static void setPbsConfig(PBSConfig pbsConfig) {
        PrebidMobile.pbsConfig = pbsConfig;
    }

    /**
     * {@link #setCreativeFactoryTimeout(int)}
     */
    public static int getCreativeFactoryTimeout() {
        if (pbsConfig != null){
            if (pbsConfig.getBannerTimeout() != 0) {
                return pbsConfig.getBannerTimeout();
            }
        }
        return creativeFactoryTimeout;
    }

    /**
     * Sets creative factory timeout. It's time to parse and render banner ads.
     *
     * @param creativeFactoryTimeout in ms (default 6000ms)
     */
    public static void setCreativeFactoryTimeout(int creativeFactoryTimeout) {
        PrebidMobile.creativeFactoryTimeout = creativeFactoryTimeout;
    }

    /**
     * Priority Policy: PBSConfig > SDKConfig > Default
     * @return creativeFactoryTimeoutPreRender in ms
     */
    public static int getCreativeFactoryTimeoutPreRenderContent() {
        if (pbsConfig != null) {
            if (pbsConfig.getPreRenderTimeout() != 0) {
                return pbsConfig.getPreRenderTimeout();
            }
        }
        return creativeFactoryTimeoutPreRenderContent;
    }


    /**
     * Sets creative factory timeout for prerender content. It's time to parse and render interstitial ads.
     *
     * @param creativeFactoryTimeoutPreRenderContent in ms (default 30000ms)
     */
    public static void setCreativeFactoryTimeoutPreRenderContent(int creativeFactoryTimeoutPreRenderContent) {
        PrebidMobile.creativeFactoryTimeoutPreRenderContent = creativeFactoryTimeoutPreRenderContent;
    }

    /**
     * Registers plugin renderer for displaying ad in custom wrapper.
     * Prebid SDK will choose this renderer only if the winning bid contains this renderer.
     */
    public static void registerPluginRenderer(PrebidMobilePluginRenderer prebidMobilePluginRenderer) {
        PrebidMobilePluginRegister.getInstance().registerPlugin(prebidMobilePluginRenderer);
    }

    /**
     * Remove plugin renderer. {@link #registerPluginRenderer(PrebidMobilePluginRenderer)}
     */
    public static void unregisterPluginRenderer(PrebidMobilePluginRenderer prebidMobilePluginRenderer) {
        PrebidMobilePluginRegister.getInstance().unregisterPlugin(prebidMobilePluginRenderer);
    }

    /**
     * Return whether SDK already registered this renderer.
     */
    public static Boolean containsPluginRenderer(PrebidMobilePluginRenderer prebidMobilePluginRenderer) {
        return PrebidMobilePluginRegister.getInstance().containsPlugin(prebidMobilePluginRenderer);
    }

    /**
     * {@link #setEventDelegate(PrebidEventDelegate)}
     */
    @Nullable
    public static PrebidEventDelegate getEventDelegate() {
        return eventDelegateReference.get();
    }

    /**
     * Sets the {@link PrebidEventDelegate} instance to handle the auction request and response from the SDK.
     * This allows the SDK to collect some statistical data.
     * The provided delegate will be stored as a weak reference so you need to store reference to it.
     *
     * @param eventDelegate the instance of {@link PrebidEventDelegate} to handle events from the SDK. 
     *                      Can be null to clear the existing delegate.
     */
    public static void setEventDelegate(@Nullable PrebidEventDelegate eventDelegate) {
        eventDelegateReference = new WeakReference<>(eventDelegate);
    }

    /**
     * @param disableStatusCheck boolean flag for skipping status check
     * If true, the SDK will not check the PBS status during initialization. This will save initialization time
     * if the PBS endpoint is always live and handled client side
     */
    public static void setDisableStatusCheck(boolean disableStatusCheck) {
        PrebidMobile.disableStatusCheck = disableStatusCheck;
    }

    /**
     * {@link #setDisableStatusCheck(boolean)}
     */
    public static boolean shouldDisableStatusCheck() {
        return disableStatusCheck;
    }

    /**
     * LogLevel for logging control.c
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


    private static int[] parseVersion(String version) {
        int[] versions = new int[]{0, 0, 0};
        String[] versionStrings = version.split("\\.");
        if (versionStrings.length >= 3) {
            for (int i = 0; i < 3; i++) {
                versions[i] = Integer.parseInt(versionStrings[i]);
            }
        }
        return versions;
    }

}
