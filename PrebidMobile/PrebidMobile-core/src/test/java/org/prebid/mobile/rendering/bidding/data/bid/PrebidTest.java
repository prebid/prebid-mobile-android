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
import org.prebid.mobile.rendering.models.AdConfiguration;
import org.prebid.mobile.test.utils.ResourceUtils;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class PrebidTest {

    @Test
    public void whenFromJSONObjectAndJSONObjectPassed_ReturnParsedPrebid()
    throws IOException, JSONException {
        JSONObject jsonPrebid = new JSONObject(ResourceUtils.convertResourceToString("bidding_prebid_obj.json"));
        Prebid prebid = Prebid.fromJSONObject(jsonPrebid);
        assertNotNull(prebid);
        assertNotNull(prebid.getCache());
        assertNotNull(prebid.getTargeting());
        assertEquals(3, prebid.getTargeting().size());
        assertEquals("type", prebid.getType());
    }

    @Test
    public void whenFromJSONObjectAndNullPassed_ReturnNotNull() {
        assertNotNull(Prebid.fromJSONObject(null));
    }

    @Test
    public void whenGetJsonObjectForImp_EqualsExpected() throws JSONException {
        final String configId = "test";
        final AdConfiguration adUnitConfiguration = new AdConfiguration();

        adUnitConfiguration.setConfigId(configId);

        JSONObject expected = new JSONObject();
        StoredRequest storedRequest = new StoredRequest(configId);
        expected.put("storedrequest", storedRequest.toJSONObject());

        assertEquals(expected.toString(), Prebid.getJsonObjectForImp(adUnitConfiguration).toString());

        expected.put("is_rewarded_inventory", 1);
        adUnitConfiguration.setRewarded(true);
        assertEquals(expected.toString(), Prebid.getJsonObjectForImp(adUnitConfiguration).toString());
    }

    @Test
    public void whenGetJsonObjectForBidRequest_EqualsExpected() throws JSONException {
        JSONObject expected = new JSONObject();
        StoredRequest storedRequest = new StoredRequest("test");
        JSONObject cache = new JSONObject();
        cache.put("bids", new JSONObject());

        expected.put("storedrequest", storedRequest.toJSONObject());
        expected.put("cache", cache);
        expected.put("targeting", new JSONObject());

        assertEquals(expected.toString(), Prebid.getJsonObjectForBidRequest("test", false).toString());
    }
}