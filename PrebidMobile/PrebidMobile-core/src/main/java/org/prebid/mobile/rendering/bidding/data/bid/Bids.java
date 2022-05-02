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

package org.prebid.mobile.rendering.bidding.data.bid;

import org.json.JSONObject;

public class Bids {

    private String url;
    private String cacheId;

    protected Bids() {
    }

    public String getUrl() {
        return url;
    }

    public String getCacheId() {
        return cacheId;
    }

    public static Bids fromJSONObject(JSONObject jsonObject) {
        Bids bids = new Bids();
        if (jsonObject == null) {
            return bids;
        }
        bids.url = jsonObject.optString("url");
        bids.cacheId = jsonObject.optString("cacheId");
        return bids;
    }
}
