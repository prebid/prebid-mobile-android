package org.prebid.mobile;

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
    public void merge_emptyOpenRtb() throws JSONException {
        String request = "{}";
        String openRtb = "";

        JSONObject mergedJson = merge(request, openRtb);

        assertJsonEquals("{}", mergedJson.toString());
    }

    @Test
    public void merge_wrongJson() throws JSONException {
        String request = "{}";
        String openRtb = "not a json string";

        JSONObject mergedJson = merge(request, openRtb);

        assertJsonEquals("{}", mergedJson.toString());
    }

    @Test
    public void mergeSensitiveData_emptyRequest_emptyResult() throws JSONException {
        String request = "{}";
        String openRtb = fromResources("sensitive_data_fake.json");

        JSONObject mergedJson = merge(request, openRtb);

        assertJsonEquals(fromResources("sensitive_data_empty.json"), mergedJson.toString());
    }

    @Test
    public void mergeSensitiveData_fullRequest_requestFieldsAreNotChanged() throws JSONException {
        String request = fromResources("sensitive_data_real.json");
        String openRtb = fromResources("sensitive_data_fake.json");

        JSONObject mergedJson = merge(request, openRtb);

        assertJsonEquals(request, mergedJson.toString());
    }

    @Test
    public void merge_differentTypes() throws JSONException {
        String request = "{}";
        String openRtb = fromResources("merge_all_types.json");

        JSONObject mergedJson = merge(request, openRtb);

        assertJsonEquals(openRtb, mergedJson.toString());
    }

    @Test
    public void mergeComplex_withReplace() throws JSONException {
        String request = fromResources("merge_with_replace_request.json");
        String openRtb = fromResources("merge_with_replace_openrtb.json");

        JSONObject mergedJson = merge(request, openRtb);

        String result = fromResources("merge_with_replace_result.json");
        assertJsonEquals(result, mergedJson.toString());
    }

    @Test
    public void merge_replaceWithNewType() throws JSONException {
        String request = fromResources("merge_replace_with_new_type_request.json");
        String openRtb = fromResources("merge_replace_with_new_type_openrtb.json");

        JSONObject mergedJson = merge(request, openRtb);

        assertJsonEquals(openRtb, mergedJson.toString());
    }

    @Test
    public void merge_arrayPrimitives() throws JSONException {
        String request = fromResources("merge_arrays_primitives_request.json");
        String openRtb = fromResources("merge_arrays_primitives_openrtb.json");

        JSONObject mergedJson = merge(request, openRtb);

        String result = fromResources("merge_arrays_primitives_result.json");
        assertJsonEquals(result, mergedJson.toString());
    }

    @Test
    public void merge_arrayObjects() throws JSONException {
        String request = fromResources("merge_arrays_objects_request.json");
        String openRtb = fromResources("merge_arrays_objects_openrtb.json");

        JSONObject mergedJson = merge(request, openRtb);

        String result = fromResources("merge_arrays_objects_result.json");
        assertJsonEquals(result, mergedJson.toString());
    }

    private JSONObject merge(String request, String openRtb) throws JSONException {
        JSONObject requestJson = new JSONObject(request);
        return OpenRtbMerger.globalMerge(requestJson, openRtb);
    }

    private String fromResources(String path) {
        return ResourceUtils.convertResourceToString("OpenRtbMerger/" + path);
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