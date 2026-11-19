package org.prebid.mobile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.api.eid.ExtendedId;
import org.prebid.mobile.testutils.BaseSetup;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashMap;
import java.util.Map;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = BaseSetup.testSDK)
public class ExtendedIdTest {

    @Test
    public void wrap_validJson_keepsSourceAndJson() throws Exception {
        JSONObject json = createEidJson();

        ExtendedId eid = ExtendedId.wrap(json);

        assertEquals("id5-sync.com", eid.getSource());
        assertEquals(json.toString(), eid.getJson().toString());
    }

    @Test
    public void wrap_changingTheWrappedJsonDoesNotChangeTheEid() throws Exception {
        JSONObject json = createEidJson();
        String original = json.toString();
        ExtendedId eid = ExtendedId.wrap(json);

        json.put("source", "other.com");
        json.getJSONArray("uids").getJSONObject(0).put("id", "changed");

        assertEquals(original, eid.getJson().toString());
    }

    @Test
    public void getJson_returnsNewCopyOnEveryCall() throws Exception {
        ExtendedId eid = ExtendedId.wrap(createEidJson());
        String original = eid.getJson().toString();

        JSONObject first = eid.getJson();
        first.put("source", "other.com");

        JSONObject second = eid.getJson();
        assertNotSame(first, second);
        assertEquals(original, second.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void wrap_missingSource_throws() {
        ExtendedId.wrap(new JSONObject());
    }

    @Test(expected = IllegalArgumentException.class)
    public void wrap_emptySource_throws() throws Exception {
        ExtendedId.wrap(new JSONObject().put("source", ""));
    }

    @Test(expected = IllegalArgumentException.class)
    public void wrap_nullSource_throws() throws Exception {
        // optString would turn this into the string "null".
        ExtendedId.wrap(new JSONObject().put("source", JSONObject.NULL));
    }

    @Test(expected = IllegalArgumentException.class)
    public void wrap_numericSource_throws() throws Exception {
        ExtendedId.wrap(new JSONObject().put("source", 123));
    }

    @Test
    public void wrap_jsonThatCantBeEncoded_throws() {
        Map<String, Object> map = new HashMap<>();
        map.put("source", "id5-sync.com");
        map.put("uids", Double.NaN);
        JSONObject json = new JSONObject(map);

        try {
            ExtendedId.wrap(json);
        } catch (IllegalArgumentException exception) {
            assertThat(exception.getMessage()).contains("id5-sync.com");
            return;
        }
        throw new AssertionError("Expected IllegalArgumentException");
    }

    private static JSONObject createEidJson() throws Exception {
        return new JSONObject()
                .put("source", "id5-sync.com")
                .put("uids", new JSONArray().put(new JSONObject().put("id", "abc").put("atype", 1)));
    }
}
