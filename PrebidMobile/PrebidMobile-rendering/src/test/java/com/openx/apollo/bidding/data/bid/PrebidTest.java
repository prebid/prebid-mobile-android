package com.openx.apollo.bidding.data.bid;

import com.apollo.test.utils.ResourceUtils;
import com.openx.apollo.models.AdConfiguration;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

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