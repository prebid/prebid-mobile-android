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

    /**
     * A cache entry is usable only with a cacheId. Prebid Cache is read with
     * {@code GET /cache?uuid=<cacheId>}, and Prebid Universal Creative builds that request from the
     * hb_cache_id targeting key. {@code url} is the same cacheId pre-assembled with the cache host
     * and path, so it never counts on its own.
     *
     * @see <a href="https://docs.prebid.org/prebid-server/endpoints/pbs-endpoints-pbc.html">Prebid Cache endpoints</a>
     * @see <a href="https://docs.prebid.org/prebid-server/use-cases/pbs-sdk.html">Prebid Server with the Mobile SDK</a>
     */
    public boolean hasCacheData() {
        return cacheId != null && !cacheId.isEmpty();
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
