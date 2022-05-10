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

public class Cache {

    private String key;
    private String url;
    private Bids bids;

    protected Cache() {
    }

    public String getKey() {
        return key;
    }

    public String getUrl() {
        return url;
    }

    public Bids getBids() {
        if (bids == null) {
            bids = new Bids();
        }
        return bids;
    }

    public static Cache fromJSONObject(JSONObject jsonObject) {
        Cache cache = new Cache();
        if (jsonObject == null) {
            return cache;
        }
        cache.key = jsonObject.optString("key");
        cache.url = jsonObject.optString("url");
        cache.bids = Bids.fromJSONObject(jsonObject.optJSONObject("bids"));

        return cache;
    }
}
