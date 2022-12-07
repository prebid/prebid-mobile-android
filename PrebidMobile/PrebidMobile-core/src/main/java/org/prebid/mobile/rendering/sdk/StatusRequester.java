package org.prebid.mobile.rendering.sdk;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.PrebidMobile;
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

        String url = PrebidMobile.getPrebidServerHost().getHostUrl();
        if (url.contains("/openrtb2/auction")) {
            String statusUrl = url.replace("/openrtb2/auction", "/status");
            ServerConnection.fireWithResult(
                statusUrl,
                getResponseHandler()
            );
        } else {
            LogUtil.info("SDK doesn't support host urls without `/openrtb2/auction` part for now");
            onSuccess();
        }
    }

    private static ResponseHandler getResponseHandler() {
        return new ResponseHandler() {
            @Override
            public void onResponse(BaseNetworkTask.GetUrlResult response) {
                if (response.statusCode == 200) {
                    try {
                        JSONObject responseJson = new JSONObject(response.responseString);
                        JSONObject applicationJson = responseJson.optJSONObject("application");
                        if (applicationJson != null) {
                            onSuccess();
                            return;
                        }
                    } catch (JSONException exception) {
                        onInitializationError("Wrong `/status` response: " + exception.getMessage());
                        return;
                    }
                }
                onInitializationError("Server status is not ok!");
            }

            @Override
            public void onError(
                String msg,
                long responseTime
            ) {
                onInitializationError("Prebid Server is not responding: " + msg);
            }

            @Override
            public void onErrorWithException(
                Exception exception,
                long responseTime
            ) {
                onInitializationError("Prebid Server is not responding: " + exception.getMessage());
            }
        };
    }

    private static void onSuccess() {
        LogUtil.debug(TAG, "Prebid SDK " + PrebidMobile.SDK_VERSION + " initialized");
        if (listener != null) {
            SdkInitializer.postOnMainThread(() -> listener.onSdkInit());
        }
    }

    private static void onInitializationError(@NonNull String message) {
        LogUtil.error(TAG, message);
        if (listener != null) {
            SdkInitializer.postOnMainThread(() -> listener.onSdkFailedToInit(new InitError(message)));
        }
    }

}
