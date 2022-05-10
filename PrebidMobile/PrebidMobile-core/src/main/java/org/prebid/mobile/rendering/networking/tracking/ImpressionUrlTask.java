/*
 *    Copyright 2018-2021 Prebid.org, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.prebid.mobile.rendering.networking.tracking;

import org.prebid.mobile.LogUtil;
import org.prebid.mobile.api.exceptions.AdException;
import org.prebid.mobile.rendering.networking.BaseNetworkTask;
import org.prebid.mobile.rendering.networking.BaseResponseHandler;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

class ImpressionUrlTask extends BaseNetworkTask {
    private static final String TAG = ImpressionUrlTask.class.getSimpleName();

    private final static int MAX_REDIRECTS = 5;

    public ImpressionUrlTask(BaseResponseHandler handler) {
        super(handler);
    }

    @Override
    public GetUrlResult customParser(int code, URLConnection urlConnection) {
        GetUrlResult result;
        try {
            result = openConnectionCheckRedirects(urlConnection);
        }
        catch (Exception e) {
            LogUtil.error(TAG, "Redirection failed");
            result = new GetUrlResult();
        }
        return result;
    }

    private GetUrlResult openConnectionCheckRedirects(URLConnection urlConnection)
    throws IOException, AdException {
        GetUrlResult result = new GetUrlResult();
        boolean redir = true;
        int redirects = 0;
        String response = "";

        while (redir) {

            if (!(urlConnection instanceof HttpURLConnection)) {
                LogUtil.error(TAG, "Redirect fail for impression event");
                return null;
            }

            ((HttpURLConnection) urlConnection).setInstanceFollowRedirects(false);

            HttpURLConnection http = (HttpURLConnection) urlConnection;
            int httpResponseCode = http.getResponseCode();

            if (httpResponseCode >= 300 && httpResponseCode <= 307 && httpResponseCode != 306 && httpResponseCode != HttpURLConnection.HTTP_NOT_MODIFIED) {
                //Base url
                URL base = http.getURL();
                //new url to forward to
                String loc = http.getHeaderField("Location");
                URL target = null;
                if (loc != null) {
                    target = new URL(base, loc);
                }
                http.disconnect();
                // Redirection should be allowed only for HTTP and HTTPS
                // and should be limited to 5 redirections at most.
                //TODO: check with iOS on the limitation
                if (target == null || !(target.getProtocol().equals("http")
                                        || target.getProtocol().equals("https")) || redirects >= MAX_REDIRECTS) {
                    throw new SecurityException("illegal URL redirect");
                }
                redir = true;
                urlConnection = target.openConnection();
                redirects++;
            }
            //We probably do not need to worry abt other status codes for tracking events, as we do not do any retry of these events, if it's !=200
            else if (httpResponseCode == 200) {
                response = readResponse(urlConnection.getInputStream());
                redir = false;
            }
            else {
                String error = String.format("Redirect error - Bad server response - [HTTP Response code of %s]", httpResponseCode);
                LogUtil.error(TAG, error);
                //Don't set exception on result. But instead just bail out with an error log
                throw new AdException(AdException.SERVER_ERROR, error);
            }
        }

        //We do not even need to handle this response for tracking events. It's fire & forget.
        result.responseString = response;

        return result;
    }
}
