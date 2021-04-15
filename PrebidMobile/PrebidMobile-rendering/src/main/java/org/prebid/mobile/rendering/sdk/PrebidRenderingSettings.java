/*
 *    Copyright 2018-2021 Prebid.org, Inc.
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

package org.prebid.mobile.rendering.sdk;

import android.content.Context;
import android.util.Log;

import org.prebid.mobile.rendering.BuildConfig;
import org.prebid.mobile.rendering.bidding.enums.Host;
import org.prebid.mobile.rendering.errors.AdException;
import org.prebid.mobile.rendering.mraid.MraidEnv;
import org.prebid.mobile.rendering.networking.BaseNetworkTask;
import org.prebid.mobile.rendering.sdk.deviceData.listeners.SdkInitListener;
import org.prebid.mobile.rendering.session.manager.OmAdSessionManager;
import org.prebid.mobile.rendering.utils.helpers.AppInfoManager;
import org.prebid.mobile.rendering.utils.logger.OXLog;

import java.util.concurrent.atomic.AtomicInteger;

public class PrebidRenderingSettings {
    private static final String TAG = PrebidRenderingSettings.class.getSimpleName();

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
    public static final int AUTO_REFRESH_DELAY_MAX = 120000;

    /**
     * Default refresh interval. 60 seconds
     * Used when the refresh interval is not in the AUTO_REFRESH_DELAY_MIN & AUTO_REFRESH_DELAY_MAX range.
     */
    public static final int AUTO_REFRESH_DELAY_DEFAULT = 60000;

    /**
     * Minimum refresh interval allowed. 15 seconds
     */
    public static final int AUTO_REFRESH_DELAY_MIN = 15000;

    private static final AtomicInteger INIT_SDK_TASK_COUNT = new AtomicInteger();
    private static final int MANDATORY_TASK_COUNT = 3;

    /**
     * Loglevels for easy development
     * Default - No sdks logs
     * Refer - LogLevel
     */
    public static LogLevel logLevel = LogLevel.NONE;

    //If true, the SDK sends "af=3,5", indicating support for MRAID
    public static boolean sendMraidSupportParams = true;
    public static boolean isCoppaEnabled = false;
    public static boolean useExternalBrowser = false;

    private static SdkInitListener sInitSdkListener;

    private static Host sBidServerHost = Host.CUSTOM;
    private static String sAccountId;

    private static int sConnectionTimeout = BaseNetworkTask.TIMEOUT_DEFAULT;

    private static boolean sIsSdkInitialized = false;
    private static boolean sUseHttps = false;

    /**
     * First call, before using any adview APIs
     * To initialize the SDK, before making an adRequest
     *
     * @param context         application OR activity context
     * @param sdkInitListener will be notified as soon as SDK finishes initializing
     */

    public static void initializeSDK(Context context, final SdkInitListener sdkInitListener)
            throws AdException {
        Log.d(TAG, "Initializing Prebid Rendering SDK");
        if (context == null) {
            throw new AdException(AdException.INIT_ERROR, "Prebid Rendering SDK initialization failed. Context is null");
        }

        if (sIsSdkInitialized) {
            return;
        }

        sInitSdkListener = sdkInitListener;
        INIT_SDK_TASK_COUNT.set(0);

        if (logLevel != null) {
            //1
            initializeLogging();
        }
        AppInfoManager.init(context);
        //2
        initOpenMeasurementSDK(context);

        //3
        ManagersResolver.getInstance().prepare(context);
    }

    /**
     * Return 'true' if Prebid Rendering SDK is initialized completely
     */
    public static boolean isSdkInitialized() {
        return sIsSdkInitialized;
    }

    /**
     * Helper to know the last commit hash of Prebid Rendering SDK
     *
     * @return
     */
    public static String getSDKCommitHash() {
        return BuildConfig.GitHash;
    }

    public static int getTimeoutMillis() {
        return sConnectionTimeout;
    }

    public static void setTimeoutMillis(int millis) {
        sConnectionTimeout = millis;
    }

    public static void setBidServerHost(Host host) {
        if (host == null) {
            OXLog.error(TAG, "setBidServerHost: Error. Can't assign a null host.");
            return;
        }

        sBidServerHost = host;
    }

    public static Host getBidServerHost() {
        return sBidServerHost;
    }

    public static String getAccountId() {
        return sAccountId;
    }

    public static void setAccountId(String accountId) {
        sAccountId = accountId;
    }

    /**
     * Set whether to use SCHEME_HTTP or SCHEME_HTTPS for WebView base urls.
     */
    public static void useHttpsWebViewBaseUrl(boolean useHttps) {
        sUseHttps = useHttps;
    }

    /**
     * Allow the publisher to use either http or https. This only affects WebView base urls.
     *
     * @return "https://" if sUseHttps is true; "http://" otherwise.
     */
    public static String getWebViewBaseUrlScheme() {
        return sUseHttps ? SCHEME_HTTPS : SCHEME_HTTP;
    }

    private static void initOpenMeasurementSDK(Context context) {
        OmAdSessionManager.activateOmSdk(context.getApplicationContext());
        increaseTaskCount();
    }

    private static void initializeLogging() {
        OXLog.setLogLevel(logLevel.getValue());//set to the publisher set value
        increaseTaskCount();
    }

    static void increaseTaskCount() {
        if (INIT_SDK_TASK_COUNT.incrementAndGet() >= MANDATORY_TASK_COUNT) {
            sIsSdkInitialized = true;
            OXLog.debug(TAG, "Prebid Rendering SDK " + SDK_VERSION + " Initialized");

            if (sInitSdkListener != null) {
                sInitSdkListener.onSDKInit();
            }
        }
    }

    /*
     * Loglevels for easy development
     * NONE - no sdk logs
     * DEBUG - sdk logs with debug level only. Noisy level
     * WARN - sdk logs with warn level only
     * ERROR - sdk logs with error level only
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
