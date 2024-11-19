package org.prebid.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

public class OpenRtbMergerTest {

    @Test
    public void test() throws JSONException {
        String request = "{}";
        String openRtb = """
                        {
                            "field_new": "new"
                        }
                """;

        JSONObject mergedJson = merge(request, openRtb);

        assertTrue(mergedJson.has("field_new"));
        assertEquals("new", mergedJson.get("field_new"));
    }

    @Test
    public void mergeIgnoreValues() throws JSONException {
        String request = "{}";
        String openRtb = """
                    {
                        "geo": {
                            "lat": 1.0,
                            "lon": 2.0
                        }
                    }
                """;

        JSONObject mergedJson = merge(request, openRtb);

        assertEquals(0, mergedJson.length());
    }

    private JSONObject merge(String request, String openRtb) throws JSONException {
        JSONObject requestJson = new JSONObject(request);
        return OpenRtbMerger.globalMerge(requestJson, openRtb);
    }

}