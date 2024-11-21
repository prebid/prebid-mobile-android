package org.prebid.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.diff.JsonDiff;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.prebid.mobile.test.utils.ResourceUtils;

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
    public void mergeSensitiveData_emptyRequest() throws JSONException {
        String request = "{}";
        String openRtb = fromResources("OpenRtbMerger/sensitive_data_fake.json");

        JSONObject mergedJson = merge(request, openRtb);

        assertJsonEquals(fromResources("OpenRtbMerger/sensitive_data_empty.json"), mergedJson.toString());
    }

    @Test
    public void mergeSensitiveData_fullRequest() throws JSONException {
        String request = fromResources("OpenRtbMerger/sensitive_data_real.json");
        String openRtb = fromResources("OpenRtbMerger/sensitive_data_fake.json");

        JSONObject mergedJson = merge(request, openRtb);

        assertJsonEquals(request, mergedJson.toString());
    }

    private JSONObject merge(String request, String openRtb) throws JSONException {
        JSONObject requestJson = new JSONObject(request);
        return OpenRtbMerger.globalMerge(requestJson, openRtb);
    }

    private String fromResources(String path) {
        return ResourceUtils.convertResourceToString(path);
    }

    private void assertJsonEquals(String json1, String json2) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode beforeNode = mapper.readTree(json1);
            JsonNode afterNode = mapper.readTree(json2);

            String difference = JsonDiff.asJson(beforeNode, afterNode).toString();
            if (difference.equals("[]")) {
                return;
            }

            throw new AssertionError("Json values are different: " + difference);
        } catch (JsonProcessingException e) {
            throw new NullPointerException("Can't compare: " + e.getMessage());
        }
    }

}