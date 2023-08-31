package org.prebid.mobile.rendering.sdk;

import org.jetbrains.annotations.NotNull;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.rendering.networking.BaseNetworkTask;
import org.prebid.mobile.rendering.networking.ResponseHandler;
import org.prebid.mobile.rendering.networking.tracking.ServerConnection;

public class StatusRequester {

    private static InitializationManager initializationManager;

    public static void makeRequest(@NotNull InitializationManager manager) {
        initializationManager = manager;

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
                initializationManager.statusRequesterTaskCompleted(null);
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
                    initializationManager.statusRequesterTaskCompleted(null);
                    return;
                }
                initializationManager.statusRequesterTaskCompleted(
                        "Server status is not ok!"
                );
            }

            @Override
            public void onError(
                String msg,
                long responseTime
            ) {
                initializationManager.statusRequesterTaskCompleted(
                        "Prebid Server is not responding: " + msg
                );
            }

            @Override
            public void onErrorWithException(
                Exception exception,
                long responseTime
            ) {
                initializationManager.statusRequesterTaskCompleted(
                        "Prebid Server is not responding: " + exception.getMessage()
                );
            }
        };
    }

}
