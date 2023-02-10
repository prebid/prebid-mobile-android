package org.prebid.mobile.rendering.sdk;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.prebid.mobile.LogUtil;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.api.data.InitializationStatus;
import org.prebid.mobile.api.exceptions.InitError;
import org.prebid.mobile.rendering.listeners.SdkInitializationListener;
import org.prebid.mobile.rendering.networking.BaseNetworkTask;
import org.prebid.mobile.rendering.networking.ResponseHandler;
import org.prebid.mobile.rendering.networking.tracking.ServerConnection;

public class StatusRequester {

    private static final String TAG = StatusRequester.class.getSimpleName();

    @Nullable
    private static SdkInitializationListener listener;

    public static void makeRequest(@Nullable SdkInitializationListener initializationListener) {
        listener = initializationListener;

        String statusUrl;

        String customStatusEndpointUrl = PrebidMobile.getCustomStatusEndpoint();
        if (customStatusEndpointUrl != null) {
            statusUrl = customStatusEndpointUrl;
        } else {
            String url = PrebidMobile.getPrebidServerHost().getHostUrl();
            if (url.contains("/openrtb2/auction")) {
                statusUrl = url.replace("/openrtb2/auction", "/status");
            } else {
                LogUtil.info("Prebid SDK can't build the /status endpoint. Please, provide the custom /status endpoint using PrebidMobile.setCustomStatusEndpoint().");
                postOnMainThread(StatusRequester::onInitializationCompleted);
                return;
            }
        }

        ServerConnection.fireStatusRequest(
            statusUrl,
            getResponseHandler()
        );
    }

    private static ResponseHandler getResponseHandler() {
        return new ResponseHandler() {
            @Override
            public void onResponse(BaseNetworkTask.GetUrlResult response) {
                if (response.isOkStatusCode()) {
                    onInitializationCompleted();
                    return;
                }
                onStatusRequestFailed("Server status is not ok!");
            }

            @Override
            public void onError(
                String msg,
                long responseTime
            ) {
                onStatusRequestFailed("Prebid Server is not responding: " + msg);
            }

            @Override
            public void onErrorWithException(
                Exception exception,
                long responseTime
            ) {
                onStatusRequestFailed("Prebid Server is not responding: " + exception.getMessage());
            }
        };
    }

    private static void onInitializationCompleted() {
        LogUtil.debug(TAG, "Prebid SDK " + PrebidMobile.SDK_VERSION + " initialized");
        if (listener != null) {
            listener.onInitializationComplete(InitializationStatus.SUCCEEDED);

            listener.onSdkInit();
        }
    }

    private static void onStatusRequestFailed(@NonNull String message) {
        LogUtil.error(TAG, message);
        if (listener != null) {
            InitializationStatus status = InitializationStatus.SERVER_STATUS_WARNING;
            status.setDescription(message);
            listener.onInitializationComplete(status);

            listener.onSdkFailedToInit(new InitError(message));
        }
    }

    private static void postOnMainThread(Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }

}
