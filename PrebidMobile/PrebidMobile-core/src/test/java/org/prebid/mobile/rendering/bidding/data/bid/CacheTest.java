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

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.prebid.mobile.test.utils.ResourceUtils;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class CacheTest {

    @Test
    public void whenFromJSONObjectAndJSONObjectPassed_ReturnParsedCache()
    throws IOException, JSONException {
        JSONObject jsonCache = new JSONObject(ResourceUtils.convertResourceToString("bidding_cache_obj.json"));
        Cache cache = Cache.fromJSONObject(jsonCache);
        assertNotNull(cache);
        assertEquals("cacheKey", cache.getKey());
        assertEquals("cacheUrl", cache.getUrl());
        assertNotNull(cache.getBids());
    }

    @Test
    public void whenFromJSONObjectAndNullPassed_ReturnNotNull() {
        assertNotNull(Cache.fromJSONObject(null));
    }

    @Test
    public void whenFromJSONObjectWithLowercaseVastXml_ReturnParsedVastXmlCache()
    throws JSONException {
        JSONObject jsonCache = new JSONObject();
        jsonCache.put("vastxml", new JSONObject()
                .put("url", "vastUrl")
                .put("cacheId", "vastCacheId")
        );

        Cache cache = Cache.fromJSONObject(jsonCache);

        assertEquals("vastUrl", cache.getVastXml().getUrl());
        assertEquals("vastCacheId", cache.getVastXml().getCacheId());
    }

    @Test
    public void whenBidsHaveCacheId_HasSuccessfulServerCache() throws JSONException {
        JSONObject jsonCache = new JSONObject();
        jsonCache.put("bids", new JSONObject().put("cacheId", "cache-id"));

        assertTrue(Cache.fromJSONObject(jsonCache).hasSuccessfulServerCache());
    }

    @Test
    public void whenVastXmlHasCacheId_HasSuccessfulServerCache() throws JSONException {
        JSONObject jsonCache = new JSONObject();
        jsonCache.put("vastXml", new JSONObject().put("cacheId", "vast-cache-id"));

        assertTrue(Cache.fromJSONObject(jsonCache).hasSuccessfulServerCache());
    }

    @Test
    public void whenBidsHaveUrlButNoCacheId_NoSuccessfulServerCache() throws JSONException {
        // url is only cacheId pre-assembled with the cache host and path; without a cacheId the
        // creative cannot be retrieved from Prebid Cache.
        JSONObject jsonCache = new JSONObject();
        jsonCache.put("bids", new JSONObject().put("url", "https://prebid-cache/cache?uuid=cache-id"));

        assertFalse(Cache.fromJSONObject(jsonCache).hasSuccessfulServerCache());
    }

    @Test
    public void whenNoCacheEntries_NoSuccessfulServerCache() {
        assertFalse(Cache.fromJSONObject(new JSONObject()).hasSuccessfulServerCache());
    }
}
