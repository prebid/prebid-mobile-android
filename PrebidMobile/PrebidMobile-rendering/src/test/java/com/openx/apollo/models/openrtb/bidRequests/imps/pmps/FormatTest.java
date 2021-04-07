package com.openx.apollo.models.openrtb.bidRequests.imps.pmps;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FormatTest {

    @Test
    public void whenGetJsonObject_JsonObjectWithValuesReturned() throws JSONException {
        Format format = new Format(1, 2);
        JSONObject expected = new JSONObject();
        expected.put("w", 1);
        expected.put("h", 2);

        assertEquals(expected.toString(), format.getJsonObject().toString());
    }
}