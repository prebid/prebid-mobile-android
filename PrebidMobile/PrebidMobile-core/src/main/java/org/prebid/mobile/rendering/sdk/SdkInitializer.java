package org.prebid.mobile.rendering.sdk;

import android.app.Application;
import android.content.Context;
import androidx.annotation.Nullable;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.api.exceptions.InitError;
import org.prebid.mobile.rendering.listeners.SdkInitializationListener;
import org.prebid.mobile.rendering.session.manager.OmAdSessionManager;
import org.prebid.mobile.rendering.utils.helpers.AppInfoManager;

import java.util.concurrent.atomic.AtomicInteger;

public class SdkInitializer {

    private static final String TAG = SdkInitializer.class.getSimpleName();

    protected static boolean isSdkInitialized = false;

    private static final AtomicInteger INIT_SDK_TASK_COUNT = new AtomicInteger();
    private static final int MANDATORY_TASK_COUNT = 4;

    protected static SdkInitializationListener sdkInitListener;

    public static void init(
        @Nullable Context context,
        @Nullable SdkInitializationListener listener
    ) {
        sdkInitListener = listener;
        if (context == null) {
            String error = "Context must be not null!";
            LogUtil.error(error);
            if (listener != null) {
                listener.onSdkFailedToInit(new InitError(error));
            }
            return;
        }

        if (!(context instanceof Application)) {
            Context applicationContext = context.getApplicationContext();
            if (applicationContext != null) {
                context = applicationContext;
            } else {
                LogUtil.warning(TAG, "Can't get application context, SDK will use context: " + context.getClass());
            }
        }

        if (isSdkInitialized && ManagersResolver.getInstance().getContext() != null) {
            return;
        }
        isSdkInitialized = false;

        LogUtil.debug(TAG, "Initializing Prebid Rendering SDK");

        INIT_SDK_TASK_COUNT.set(0);

        if (PrebidMobile.logLevel != null) {
            initializeLogging();
        }
        AppInfoManager.init(context);
        initOpenMeasurementSDK(context);
        ManagersResolver.getInstance().prepare(context);
        StatusRequester.makeRequest(listener);
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
            LogUtil.debug(TAG, "Prebid SDK " + PrebidMobile.SDK_VERSION + " initialized");
            if (sdkInitListener != null) {
                sdkInitListener.onSdkInit();
            }
        }
    }

    public static boolean isIsSdkInitialized() {
        return isSdkInitialized;
    }

}
