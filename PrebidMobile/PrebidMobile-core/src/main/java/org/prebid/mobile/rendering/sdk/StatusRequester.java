package org.prebid.mobile.rendering.sdk;

import android.util.Log;

import org.jetbrains.annotations.Nullable;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.rendering.networking.BaseNetworkTask;
import org.prebid.mobile.rendering.networking.ResponseHandler;
import org.prebid.mobile.rendering.networking.tracking.ServerConnection;

public class StatusRequester {

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
        ServerConnection.fireStatusRequest(
                statusUrl,
                getResponseHandler(resultHolder)
        );

        try {
            while (resultHolder.isResultUnavailableYet()) {
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
        private Boolean resultReceived;

        public ResultHolder() {
        }

        public Boolean isResultUnavailableYet() {
            return resultReceived == null;
        }

        public void resultReceived(String statusRequesterError) {
            this.resultReceived = true;
            this.statusRequesterError = statusRequesterError;
        }

        public String getStatusRequesterError() {
            return statusRequesterError;
        }
    }

}
