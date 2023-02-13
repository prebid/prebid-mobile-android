package org.prebid.mobile.rendering.sdk;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;

import org.prebid.mobile.LogUtil;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.api.data.InitializationStatus;
import org.prebid.mobile.api.exceptions.InitError;
import org.prebid.mobile.rendering.listeners.SdkInitializationListener;
import org.prebid.mobile.rendering.session.manager.OmAdSessionManager;
import org.prebid.mobile.rendering.utils.helpers.AppInfoManager;

public class SdkInitializer {

    private static final String TAG = SdkInitializer.class.getSimpleName();

    public static void init(
        @Nullable Context context,
        @Nullable SdkInitializationListener listener
    ) {
        if (PrebidContextHolder.getContext() != null) {
            return;
        }

        Context applicationContext = getApplicationContext(context);
        if (applicationContext == null) {
            onInitializationFailed("Context must be not null!", listener);
            return;
        }

        LogUtil.debug(TAG, "Initializing Prebid SDK");
        PrebidContextHolder.setContext(applicationContext);

        if (PrebidMobile.logLevel != null) {
            LogUtil.setLogLevel(PrebidMobile.getLogLevel().getValue());
        }

        try {
            AppInfoManager.init(applicationContext);

            OmAdSessionManager.activateOmSdk(applicationContext);

            ManagersResolver.getInstance().prepare(applicationContext);
        } catch (Throwable throwable) {
            onInitializationFailed("Exception during initialization: " + throwable.getMessage() + "\n" + Log.getStackTraceString(throwable), listener);
            return;
        }

        StatusRequester.makeRequest(listener);
    }

    @Nullable
    private static Context getApplicationContext(
        @Nullable Context context
    ) {
        if (context instanceof Application) {
            return context;
        } else if (context != null) {
            return context.getApplicationContext();
        }
        return null;
    }

    private static void onInitializationFailed(
        String error,
        @Nullable SdkInitializationListener listener
    ) {
        LogUtil.error(error);
        if (listener != null) {
            postOnMainThread(() -> {
                InitializationStatus status = InitializationStatus.FAILED;
                status.setDescription(error);
                listener.onInitializationComplete(status);

                listener.onSdkFailedToInit(new InitError(error));
            });
        }
        PrebidContextHolder.clearContext();
    }

    private static void postOnMainThread(Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }

}
