package org.prebid.mobile.rendering.sdk;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

import org.prebid.mobile.LogUtil;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.api.exceptions.InitError;
import org.prebid.mobile.rendering.listeners.SdkInitializationListener;
import org.prebid.mobile.rendering.session.manager.OmAdSessionManager;
import org.prebid.mobile.rendering.utils.helpers.AppInfoManager;

public class SdkInitializer {

    private static final String TAG = SdkInitializer.class.getSimpleName();

    protected static boolean isSdkInitialized = false;
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

        try {
            LogUtil.debug(TAG, "Initializing Prebid Rendering SDK");
            if (PrebidMobile.logLevel != null) {
                LogUtil.setLogLevel(PrebidMobile.getLogLevel().getValue());
            }
            AppInfoManager.init(context);
            OmAdSessionManager.activateOmSdk(context);
            ManagersResolver.getInstance().prepare(context);
            StatusRequester.makeRequest(listener);
        } catch (Throwable throwable) {
            if (sdkInitListener != null) {
                sdkInitListener.onSdkFailedToInit(new InitError(throwable.getMessage() + "\n" + Log.getStackTraceString(throwable)));
            }
            ManagersResolver.getInstance().clearContext();
            return;
        }

        LogUtil.debug(TAG, "Prebid SDK " + PrebidMobile.SDK_VERSION + " initialized");
        isSdkInitialized = true;
        if (sdkInitListener != null) {
            sdkInitListener.onSdkInit();
        }
    }

    public static boolean isIsSdkInitialized() {
        return isSdkInitialized;
    }

}
