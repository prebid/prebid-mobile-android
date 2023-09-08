package org.prebid.mobile.rendering.sdk;

import android.util.Log;

import org.jetbrains.annotations.Nullable;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.rendering.networking.BaseNetworkTask;
import org.prebid.mobile.rendering.networking.ResponseHandler;
import org.prebid.mobile.rendering.networking.tracking.ServerConnection;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

public class StatusRequester implements Callable<String> {

    @Override
    public String call() throws Exception {
        return makeRequest();
    }

    /**
     * @return status request error - must be null if there is no error.
     */
    @Nullable
    public static String makeRequest() {
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
                return null;
            }
        }

        ResultHolder resultHolder = new ResultHolder();

        try {
            ServerConnection.fireStatusRequest(
                    statusUrl,
                    getResponseHandler(resultHolder)
            );

            while (resultHolder.isResultNotAvailableYet()) {
                Thread.sleep(25);
            }
        } catch (InterruptedException e) {
            LogUtil.debug("StatusRequester", "InterruptedException: " + Log.getStackTraceString(e));
        }
        return resultHolder.getStatusRequesterError();
    }

    private static ResponseHandler getResponseHandler(ResultHolder resultHolder) {
        return new ResponseHandler() {
            @Override
            public void onResponse(BaseNetworkTask.GetUrlResult response) {
                if (response.isOkStatusCode()) {
                    resultHolder.resultReceived(null);
                    return;
                }
                resultHolder.resultReceived("Server status is not ok!");
            }

            @Override
            public void onError(
                String msg,
                long responseTime
            ) {
                resultHolder.resultReceived("Prebid Server is not responding: " + msg);
            }

            @Override
            public void onErrorWithException(
                Exception exception,
                long responseTime
            ) {
                resultHolder.resultReceived("Prebid Server is not responding: " + exception.getMessage());
            }
        };
    }

    private static class ResultHolder {

        private String statusRequesterError;
        private final AtomicBoolean resultReceived = new AtomicBoolean(false);

        public ResultHolder() {
        }

        public Boolean isResultNotAvailableYet() {
            return !resultReceived.get();
        }

        public void resultReceived(String statusRequesterError) {
            this.resultReceived.set(true);
            this.statusRequesterError = statusRequesterError;
        }

        public String getStatusRequesterError() {
            return statusRequesterError;
        }
    }

}
