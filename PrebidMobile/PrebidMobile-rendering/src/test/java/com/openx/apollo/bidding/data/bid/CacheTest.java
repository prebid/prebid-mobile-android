package com.openx.apollo.bidding.data.bid;

import com.apollo.test.utils.ResourceUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
}