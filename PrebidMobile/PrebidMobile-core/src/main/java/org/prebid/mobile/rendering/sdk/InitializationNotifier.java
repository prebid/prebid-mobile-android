package org.prebid.mobile.rendering.sdk;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.api.data.InitializationStatus;
import org.prebid.mobile.api.exceptions.InitError;
import org.prebid.mobile.rendering.listeners.SdkInitializationListener;


public class InitializationNotifier {

    private static final String TAG = "InitializationNotifier";

    private static boolean tasksCompletedSuccessfully = false;
    private static boolean initializationInProgress = false;

    @Nullable
    private SdkInitializationListener listener;

    public InitializationNotifier(@Nullable SdkInitializationListener listener) {
        this.listener = listener;
        initializationInProgress = true;
    }

    /**
     * @param statusRequesterError must be null, if status requester completed successfully.
     */
    public void initializationCompleted(@Nullable String statusRequesterError) {
        postOnMainThread(() -> {
            boolean statusRequestSuccessful = statusRequesterError == null;
            if (statusRequestSuccessful) {
                LogUtil.debug(TAG, "Prebid SDK " + PrebidMobile.SDK_VERSION + " initialized");

                if (listener != null) {
                    listener.onInitializationComplete(InitializationStatus.SUCCEEDED);

                    listener.onSdkInit();
                }
            } else {
                LogUtil.error(TAG, statusRequesterError);

                if (listener != null) {
                    InitializationStatus serverStatusWarning = InitializationStatus.SERVER_STATUS_WARNING;
                    serverStatusWarning.setDescription(statusRequesterError);
                    listener.onInitializationComplete(serverStatusWarning);

                    listener.onSdkFailedToInit(new InitError(statusRequesterError));
                }
            }

            tasksCompletedSuccessfully = true;
            initializationInProgress = false;
            listener = null;
        });
    }

    public void initializationFailed(@NotNull String error) {
        postOnMainThread(() -> {
            LogUtil.error(error);

            if (listener != null) {
                InitializationStatus status = InitializationStatus.FAILED;
                status.setDescription(error);
                listener.onInitializationComplete(status);

                listener.onSdkFailedToInit(new InitError(error));
            }

            PrebidContextHolder.clearContext();
            listener = null;
            initializationInProgress = false;
        });
    }


    private static void postOnMainThread(Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }

    public static boolean wereTasksCompletedSuccessfully() {
        return tasksCompletedSuccessfully;
    }

    public static boolean isInitializationInProgress() {
        return initializationInProgress;
    }

}
