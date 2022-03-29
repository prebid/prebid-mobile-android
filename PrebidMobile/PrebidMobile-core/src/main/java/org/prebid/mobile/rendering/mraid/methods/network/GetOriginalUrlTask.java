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

package org.prebid.mobile.rendering.mraid.methods.network;

import android.text.TextUtils;
import org.prebid.mobile.rendering.networking.BaseNetworkTask;
import org.prebid.mobile.rendering.networking.ResponseHandler;
import org.prebid.mobile.rendering.utils.helpers.Utils;

import java.net.URLConnection;
import java.util.Arrays;

/* This class is an AsyncTask that currently handles the MRAID user action requests of
 * Open and Expand which, on success, compel our SDK to launch new Views or Activities
 * or play videos.  The demand partner's MRAID ad supplies the call with a URL. At that
 * point the response is handled and displayed by the AsyncTask listeners.
 * Of note here is that this class handles the potential of the supplied URLs
 * having a chain of nested redirects. This is the reason why this class is named "GetOriginalUrlTask"
 */
public class GetOriginalUrlTask extends BaseNetworkTask {

    static final int MAX_REDIRECTS = 3;
    private String connectionURL;

    public GetOriginalUrlTask(ResponseHandler handler) {
        super(handler);
        result = new GetUrlResult();
    }

    private static boolean isRedirect(int code) {
        final int[] arrRedirectCodes = {
            301,
            302,
            303,
            307,
            308};//note this is sorted for binary search
        int index = Arrays.binarySearch(arrRedirectCodes, code);
        return index >= 0;
    }

    @Override
    public GetUrlResult customParser(int code, URLConnection urlConnection) {
        String[] retVal = new String[3];

        if (isRedirect(code)) {
            String location = urlConnection.getHeaderField("Location");
            if (location == null) {
                location = urlConnection.getRequestProperty("Location");
            }
            retVal[0] = !TextUtils.isEmpty(location) ? location : connectionURL;
        } else {
            retVal[0] = connectionURL;
            retVal[2] = "quit";
        }
        retVal[1] = urlConnection.getHeaderField("Content-Type");
        if (retVal[1] == null) {
            retVal[1] = urlConnection.getRequestProperty("Content-Type");
        }

        result.JSRedirectURI = retVal;
        return result;
    }

    private String[] getRedirectionUrlWithType(GetUrlParams param) {
        connectionURL = param.url;

        if (Utils.isMraidActionUrl(param.url) || TextUtils.isEmpty(param.url)) {
            // Avoid network connection creation
            return new String[]{param.url, null, null};
        }

        result = super.doInBackground(param);

        return result.JSRedirectURI;
    }

    @Override
    protected GetUrlResult doInBackground(GetUrlParams... params) {
        return getUrl(params);
    }

    private GetUrlResult getUrl(GetUrlParams... params) {

        if (isCancelled() || !validParams(params)) {
            return result;
        }

        GetUrlParams param = params[0];
        result.originalUrl = param != null ? param.url : null;

        processRedirects(param);

        return result;
    }

    /*
        iterates through a potential nested chain of redirects until no more redirects...
        sets the global result along the way, leaving the final result in place
    */
    private void processRedirects(GetUrlParams param) {

        String currentUrl;
        String[] currentResponse;
        //int i = 0;

        //while (i < MAX_REDIRECTS) {
        for (int i = 0; i < MAX_REDIRECTS; i++) {

            currentResponse = getRedirectionUrlWithType(param);

            if (currentResponse == null) {
                break;
            }

            currentUrl = currentResponse[0];

            if (TextUtils.isEmpty(currentUrl)) {
                // First call
                if (TextUtils.isEmpty(result.contentType)) {
                    result.contentType = currentResponse[1];
                }
                break;
            }

            /*
                This sets the originalUrl as any successive redirect urls in this loop...
                While this currently does not have any negative effect, but we may consider
                this an opportunity for clarity should we actually need to hold
                on to the originally requested url...
             */
            result.originalUrl = currentResponse[0];
            result.contentType = currentResponse[1];

            //no more redirects so we break, retval[2] was explicitly set to "quit"
            if (currentResponse[2] == "quit") {
                break;
            }
        }
    }
}