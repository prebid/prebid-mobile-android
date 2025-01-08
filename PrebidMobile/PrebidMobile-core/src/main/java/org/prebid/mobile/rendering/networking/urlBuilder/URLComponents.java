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

package org.prebid.mobile.rendering.networking.urlBuilder;

import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.rendering.networking.parameters.AdRequestInput;
import org.prebid.mobile.rendering.utils.helpers.Utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Hashtable;

public class URLComponents {

    private static final String TAG = URLComponents.class.getSimpleName();
    private static final String MISC_OPENRTB = "openrtb";

    private final String baseUrl;
    public final AdRequestInput adRequestInput;

    public URLComponents(
            String baseUrl,
            AdRequestInput adRequestInput
    ) {
        this.baseUrl = baseUrl;
        this.adRequestInput = adRequestInput;
    }

    public String getFullUrl() {
        String fullUrl = baseUrl;
        String queryArgString = getQueryArgString();
        if (Utils.isNotBlank(queryArgString)) {
            fullUrl += "?" + queryArgString;
        }

        return (fullUrl);
    }

    @Nullable
    public JSONObject getRequestJsonObject() {
        try {
            return adRequestInput.getBidRequest().getJsonObject();
        } catch (JSONException exception) {
            return null;
        }
    }

    public String getQueryArgString() {
        Hashtable<String, String> tempQueryArgs = new Hashtable<>();

        // If BidRequest object available, put into query arg hashtable
        try {
            JSONObject bidRequestJson = adRequestInput.getBidRequest().getJsonObject();

            if (bidRequestJson.length() > 0) {
                tempQueryArgs.put(MISC_OPENRTB, bidRequestJson.toString());
            }
        }
        catch (JSONException e) {
            LogUtil.error(TAG, "Failed to add OpenRTB query arg");
        }

        StringBuilder queryArgString = new StringBuilder();
        for (String key : tempQueryArgs.keySet()) {
            String value = tempQueryArgs.get(key);
            value = value.trim();
            try {
                value = URLEncoder.encode(value, "UTF-8");
            }
            catch (UnsupportedEncodingException e) {
                LogUtil.error(TAG, "Failed to encode value: " + value + " from key: " + key);
                continue;
            }
            //URL encoder turns spaces to +. SDK to convert + to %20
            value = value.replace("+", "%20");
            queryArgString.append(key)
                          .append("=")
                          .append(value)
                          .append("&");
        }

        // Strip off the last "&"
        if (Utils.isNotBlank(queryArgString.toString())) {
            queryArgString = new StringBuilder(queryArgString.substring(0, queryArgString.length() - 1));
        }

        return queryArgString.toString();
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public AdRequestInput getAdRequestInput() {
        return adRequestInput;
    }
}
