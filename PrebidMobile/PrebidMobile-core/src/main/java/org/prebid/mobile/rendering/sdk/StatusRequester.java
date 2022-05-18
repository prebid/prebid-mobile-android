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

    public static void makeRequest(@Nullable SdkInitializationListener listener) {
        String url = PrebidMobile.getPrebidServerHost().getHostUrl();
        if (url.contains("/openrtb2/auction")) {
            String statusUrl = url.replace("/openrtb2/auction", "/status");
            ServerConnection.fireWithResult(
                statusUrl,
                getResponseHandler(listener)
            );
        } else if (url.isEmpty()) {
            onInitError("Please set host url (PrebidMobile.setPrebidServerHost) and only then run SDK initialization.", listener);
        } else {
            onInitError("Error, url doesn't contain /openrtb2/auction part", listener);
        }
    }

    private static ResponseHandler getResponseHandler(@Nullable SdkInitializationListener listener) {
        return new ResponseHandler() {
            @Override
            public void onResponse(BaseNetworkTask.GetUrlResult response) {
                if (response.statusCode == 200) {
                    try {
                        JSONObject responseJson = new JSONObject(response.responseString);
                        JSONObject applicationJson = responseJson.optJSONObject("application");
                        if (applicationJson != null) {
                            String status = applicationJson.optString("status");
                            if (status.equalsIgnoreCase("ok")) {
                                onSuccess();
                                return;
                            }
                        }
                    } catch (JSONException exception) {
                        onInitError("JsonException: " + exception.getMessage(), listener);
                        return;
                    }
                }
                onInitError("Server status is not ok!", listener);
            }

            @Override
            public void onError(
                String msg,
                long responseTime
            ) {
                onInitError("Exception: " + msg, listener);
            }

            @Override
            public void onErrorWithException(
                Exception exception,
                long responseTime
            ) {
                onInitError("Exception: " + exception.getMessage(), listener);
            }
        };
    }

    private static void onSuccess() {
        SdkInitializer.increaseTaskCount();
    }

    private static void onInitError(
        @NonNull String message,
        @Nullable SdkInitializationListener listener
    ) {
        LogUtil.error(TAG, message);
        if (listener != null) {
            listener.onSdkFailedToInit(new InitError(message));
        }
    }

}
