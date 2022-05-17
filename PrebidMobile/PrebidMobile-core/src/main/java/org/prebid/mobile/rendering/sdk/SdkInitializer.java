package org.prebid.mobile.rendering.sdk;

import android.content.Context;
import androidx.annotation.Nullable;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.rendering.listeners.SdkInitializationListener;
import org.prebid.mobile.rendering.session.manager.OmAdSessionManager;
import org.prebid.mobile.rendering.utils.helpers.AppInfoManager;

import java.util.concurrent.atomic.AtomicInteger;

public class SdkInitializer {

    private static final String TAG = SdkInitializer.class.getSimpleName();

    private static boolean isSdkInitialized = false;

    private static final AtomicInteger INIT_SDK_TASK_COUNT = new AtomicInteger();
    private static final int MANDATORY_TASK_COUNT = 3;

    private static SdkInitializationListener sdkInitListener;

    public static void init(
        @Nullable Context context,
        @Nullable SdkInitializationListener listener
    ) {
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

        if (PrebidMobile.logLevel != null) {
            initializeLogging();
        }
        AppInfoManager.init(context);
        initOpenMeasurementSDK(context);
        ManagersResolver.getInstance().prepare(context);
    }

    private static void initializeLogging() {
        LogUtil.setLogLevel(PrebidMobile.getLogLevel().getValue());
        increaseTaskCount();
    }

    private static void initOpenMeasurementSDK(Context context) {
        OmAdSessionManager.activateOmSdk(context.getApplicationContext());
        increaseTaskCount();
    }

    /**
     * Notifies SDK initializer that one task was completed.
     * Only for internal use!
     */
    public static void increaseTaskCount() {
        if (INIT_SDK_TASK_COUNT.incrementAndGet() >= MANDATORY_TASK_COUNT) {
            isSdkInitialized = true;
            LogUtil.debug(TAG, "Prebid Rendering SDK " + PrebidMobile.SDK_VERSION + " Initialized");

            if (sdkInitListener != null) {
                sdkInitListener.onSdkInit();
            }
        }
    }

    public static boolean isIsSdkInitialized() {
        return isSdkInitialized;
    }

}
