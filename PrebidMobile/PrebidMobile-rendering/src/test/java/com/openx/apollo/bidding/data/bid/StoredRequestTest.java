package com.openx.apollo.bidding.data.bid;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StoredRequestTest {

    @Test
    public void whenToJSONObject_ReturnValidJSONObject() throws JSONException {
        JSONObject expectedJson = new JSONObject();
        expectedJson.put("id", "test");
        assertEquals(expectedJson.toString(), new StoredRequest("test").toJSONObject().toString());
    }
}